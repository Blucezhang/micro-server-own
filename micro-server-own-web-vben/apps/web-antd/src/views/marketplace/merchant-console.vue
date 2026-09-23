<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Button, Card, Form, Input, InputNumber, Space, Table, Tag } from 'ant-design-vue';
import { merchantApi } from '#/api/marketplace';

type Row = Record<string, any>;
const route = useRoute();
const loading = ref(false);
const rows = ref<Row[]>([]);
const balance = ref<Record<string, any>>();
const editingId = ref<number>();
const form = reactive<Record<string, any>>({ amount: undefined, categoryId: undefined, discountAmount: undefined, fixedAmount: undefined, freeThreshold: undefined, minimumAmount: undefined, name: '', originalPrice: undefined, partyId: undefined, priceChangeReason: '', promotionPrice: undefined, skuCode: '', totalQuantity: undefined });
const page = computed(() => ({
  MerchantProducts: { action: '发布商品', description: '创建或编辑自己的商品；价格调整由服务端审计。', title: '商品发布与编辑' },
  MerchantCoupons: { action: '创建优惠券', description: '优惠券的领取窗口和可用规则由服务端校验。', title: '优惠券管理' },
  MerchantFreight: { action: '保存运费规则', description: '固定运费与包邮门槛会参与订单结算试算。', title: '运费规则' },
  MerchantSettlement: { action: '申请提现', description: '可提现金额以结算服务实时返回的余额为准。', title: '结算与提现' },
}[String(route.name)] || { action: '提交', description: '', title: '商家操作' }));
const columns = computed(() => [
  ...Object.keys(rows.value[0] || {}).slice(0, 6).map((key) => ({ dataIndex: key, key, title: key })),
  ...(route.name === 'MerchantProducts' || route.name === 'MerchantCoupons' ? [{ key: 'actions', title: '操作' }] : []),
]);

function unwrap(data: any): Row[] { return Array.isArray(data) ? data : data?.items || []; }
async function load() {
  rows.value = []; balance.value = undefined; loading.value = true;
  try {
    const name = String(route.name);
    if (name === 'MerchantProducts') rows.value = unwrap(await merchantApi.products({ page: 0, size: 20 }));
    if (name === 'MerchantCoupons') rows.value = unwrap(await merchantApi.coupons());
    if (name === 'MerchantFreight') {
      const current = await merchantApi.freight() as Row;
      Object.assign(form, current); rows.value = [current];
    }
    if (name === 'MerchantSettlement') balance.value = await merchantApi.balance() as Row;
  } finally { loading.value = false; }
}
async function submit() {
  loading.value = true;
  try {
    const name = String(route.name);
    if (name === 'MerchantProducts') await merchantApi.saveProduct(form, editingId.value);
    if (name === 'MerchantCoupons') await merchantApi.saveCoupon(form);
    if (name === 'MerchantFreight') await merchantApi.saveFreight({ fixedAmount: form.fixedAmount, freeThreshold: form.freeThreshold });
    if (name === 'MerchantSettlement') await merchantApi.withdraw(String(form.amount));
    editingId.value = undefined; await load();
  } finally { loading.value = false; }
}
function edit(row: Row) {
  editingId.value = Number(row.id);
  Object.assign(form, row);
}
async function changeStatus(row: Row) {
  loading.value = true;
  try {
    if (route.name === 'MerchantProducts') await merchantApi.changeProductStatus(Number(row.id), row.saleStatus === 'AVAILABLE' ? 'OFF_SHELF' : 'AVAILABLE');
    if (route.name === 'MerchantCoupons') await merchantApi.changeCouponStatus(Number(row.id), row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE');
    await load();
  } finally { loading.value = false; }
}
onMounted(load); watch(() => route.name, load);
</script>

<template>
  <main class="console-page">
    <Card :bordered="false"><Tag color="purple">商家运营</Tag><h1>{{ page.title }}</h1><p>{{ page.description }}</p></Card>
    <Card v-if="balance" :bordered="false" title="可提现余额"><strong class="balance">¥{{ balance.availableAmount ?? '--' }}</strong></Card>
    <Card :bordered="false" :title="page.action">
      <Form layout="vertical" @finish="submit">
        <template v-if="route.name === 'MerchantProducts'">
          <Form.Item label="商家 ID" required><InputNumber v-model:value="form.partyId" class="full" /></Form.Item><Form.Item label="类目 ID" required><InputNumber v-model:value="form.categoryId" class="full" /></Form.Item><Form.Item label="商品名称" required><Input v-model:value="form.name" /></Form.Item><Form.Item label="SKU 编码" required><Input v-model:value="form.skuCode" /></Form.Item><Form.Item label="原价" required><InputNumber v-model:value="form.originalPrice" class="full" /></Form.Item><Form.Item label="促销价"><InputNumber v-model:value="form.promotionPrice" class="full" /></Form.Item><Form.Item v-if="editingId" label="改价原因"><Input v-model:value="form.priceChangeReason" /></Form.Item>
        </template>
        <template v-else-if="route.name === 'MerchantCoupons'">
          <Form.Item label="券名称" required><Input v-model:value="form.name" /></Form.Item><Form.Item label="满额" required><InputNumber v-model:value="form.minimumAmount" class="full" /></Form.Item><Form.Item label="优惠金额" required><InputNumber v-model:value="form.discountAmount" class="full" /></Form.Item><Form.Item label="发放总量" required><InputNumber v-model:value="form.totalQuantity" :min="1" class="full" /></Form.Item>
        </template>
        <template v-else-if="route.name === 'MerchantFreight'">
          <Form.Item label="固定运费" required><InputNumber v-model:value="form.fixedAmount" :min="0" class="full" /></Form.Item><Form.Item label="包邮门槛"><InputNumber v-model:value="form.freeThreshold" :min="0" class="full" /></Form.Item>
        </template>
        <Form.Item v-else label="提现金额" required><InputNumber v-model:value="form.amount" :min="0.01" :precision="2" class="full" /></Form.Item>
        <Space><Button :loading="loading" html-type="submit" type="primary">{{ editingId ? '保存编辑' : page.action }}</Button><Button v-if="editingId" @click="editingId = undefined">取消编辑</Button><Button @click="load">刷新</Button></Space>
      </Form>
    </Card>
    <Card v-if="columns.length" :bordered="false" title="当前记录"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="false" row-key="id"><template #bodyCell="{ column, record }"><Space v-if="column.key === 'actions'"><Button type="link" @click="edit(record)">编辑</Button><Button type="link" @click="changeStatus(record)">{{ route.name === 'MerchantProducts' ? (record.saleStatus === 'AVAILABLE' ? '下架' : '上架') : (record.status === 'ACTIVE' ? '停用' : '启用') }}</Button></Space></template></Table></Card>
  </main>
</template>

<style scoped>
.console-page { display: grid; gap: 16px; padding: 24px; } h1 { font-size: 24px; margin: 10px 0 6px; } p { color: #64748b; margin: 0; }.full { width: 100%; }.balance { color: #0f766e; font-size: 30px; }
@media (max-width: 640px) { .console-page { padding: 16px; } }
</style>
