<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Button, Card, DatePicker, Form, Input, InputNumber, Space, Table, Tag } from 'ant-design-vue';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { merchantApi } from '#/api/marketplace';
import { merchantCouponRows } from '#/api/marketplace-models';

type Row = Record<string, any>;
const route = useRoute();
const loading = ref(false);
const rows = ref<Row[]>([]);
const productPage = ref(0);
const productTotal = ref(0);
const pageSize = 20;
const balance = ref<Record<string, any>>();
const editingId = ref<number>();
const couponEditingId = ref<number>();
const form = reactive<Record<string, any>>({ amount: undefined, categoryId: undefined, discountAmount: undefined, fixedAmount: undefined, freeThreshold: undefined, minimumAmount: undefined, name: '', originalPrice: undefined, partyId: undefined, priceChangeReason: '', promotionPrice: undefined, skuCode: '', totalQuantity: undefined });
const couponForm = reactive<{ claimEndsAt?: Dayjs; claimStartsAt?: Dayjs; discountAmount?: number; expiresAt?: Dayjs; minimumAmount?: number; name: string; totalQuantity?: number }>({ name: '' });
function resetCouponForm() {
  couponEditingId.value = undefined;
  Object.assign(couponForm, { claimEndsAt: undefined, claimStartsAt: undefined, discountAmount: undefined, expiresAt: undefined, minimumAmount: undefined, name: '', totalQuantity: undefined });
}
const page = computed(() => ({
  MerchantProducts: { action: '发布商品', description: '创建或编辑自己的商品；价格调整由服务端审计。', title: '商品发布与编辑' },
  MerchantCoupons: { action: '创建优惠券', description: '未被领取的优惠券可编辑；有人领取后仅可启用或停用。', title: '优惠券管理' },
  MerchantFreight: { action: '保存运费规则', description: '固定运费与包邮门槛会参与订单结算试算。', title: '运费规则' },
  MerchantSettlement: { action: '申请提现', description: '可提现金额以结算服务实时返回的余额为准。', title: '结算与提现' },
}[String(route.name)] || { action: '提交', description: '', title: '商家操作' }));
const columns = computed(() => route.name === 'MerchantCoupons'
  ? [
      { dataIndex: 'name', key: 'name', title: '券名称' },
      { dataIndex: 'status', key: 'status', title: '状态' },
      { dataIndex: 'availableQuantity', key: 'availableQuantity', title: '剩余数量' },
      { dataIndex: 'expiresAt', key: 'expiresAt', title: '有效期至' },
      { key: 'actions', title: '操作' },
    ]
  : [
      ...Object.keys(rows.value[0] || {}).slice(0, 6).map((key) => ({ dataIndex: key, key, title: key })),
      ...(route.name === 'MerchantProducts' ? [{ key: 'actions', title: '操作' }] : []),
    ]);

async function load() {
  rows.value = []; balance.value = undefined; loading.value = true;
  try {
    const name = String(route.name);
    if (name === 'MerchantProducts') {
      const result = await merchantApi.products({ page: productPage.value, size: pageSize }) as { items: Row[]; total: number };
      rows.value = result.items;
      productTotal.value = result.total;
    }
    if (name === 'MerchantCoupons') {
      const result = await merchantApi.coupons() as Parameters<typeof merchantCouponRows>[0];
      rows.value = merchantCouponRows(result);
    }
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
    if (name === 'MerchantCoupons') {
      await merchantApi.saveCoupon({
        claimEndsAt: couponForm.claimEndsAt?.valueOf(),
        claimStartsAt: couponForm.claimStartsAt?.valueOf(),
        discountAmount: String(couponForm.discountAmount),
        expiresAt: couponForm.expiresAt?.valueOf(),
        minimumAmount: String(couponForm.minimumAmount),
        name: couponForm.name.trim(),
        totalQuantity: couponForm.totalQuantity,
      }, couponEditingId.value);
      resetCouponForm();
    }
    if (name === 'MerchantFreight') await merchantApi.saveFreight({ fixedAmount: form.fixedAmount, freeThreshold: form.freeThreshold });
    if (name === 'MerchantSettlement') await merchantApi.withdraw(String(form.amount));
    editingId.value = undefined; await load();
  } finally { loading.value = false; }
}
function edit(row: Row) {
  if (route.name === 'MerchantCoupons') {
    if (Number(row.availableQuantity) !== Number(row.totalQuantity)) return;
    couponEditingId.value = Number(row.id);
    Object.assign(couponForm, {
      claimEndsAt: row.claimEndsAt == null ? undefined : dayjs(row.claimEndsAt),
      claimStartsAt: row.claimStartsAt == null ? undefined : dayjs(row.claimStartsAt),
      discountAmount: Number(row.discountAmount),
      expiresAt: dayjs(row.expiresAt),
      minimumAmount: Number(row.minimumAmount),
      name: row.name,
      totalQuantity: Number(row.totalQuantity),
    });
    return;
  }
  if (route.name !== 'MerchantProducts') return;
  editingId.value = Number(row.id);
  Object.assign(form, row);
}
async function changeProductPage(pagination: { current?: number }) {
  if (route.name !== 'MerchantProducts') return;
  productPage.value = (pagination.current || 1) - 1;
  await load();
}
async function changeStatus(row: Row) {
  loading.value = true;
  try {
    if (route.name === 'MerchantProducts') await merchantApi.changeProductStatus(Number(row.id), row.saleStatus === 'AVAILABLE' ? 'OFF_SHELF' : 'AVAILABLE');
    if (route.name === 'MerchantCoupons') await merchantApi.changeCouponStatus(Number(row.id), row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE');
    await load();
  } finally { loading.value = false; }
}
onMounted(load);
watch(() => route.name, () => { editingId.value = undefined; resetCouponForm(); productPage.value = 0; load(); });
</script>

<template>
  <main class="console-page">
    <Card :bordered="false"><Tag color="purple">商家运营</Tag><h1>{{ page.title }}</h1><p>{{ page.description }}</p></Card>
    <Card v-if="balance" :bordered="false" title="可提现余额"><strong class="balance">¥{{ balance.availableAmount ?? '--' }}</strong></Card>
    <Card :bordered="false" :title="couponEditingId && route.name === 'MerchantCoupons' ? '编辑优惠券' : page.action">
      <Form :model="route.name === 'MerchantCoupons' ? couponForm : form" layout="vertical" @finish="submit">
        <template v-if="route.name === 'MerchantProducts'">
          <Form.Item label="商家 ID" required><InputNumber v-model:value="form.partyId" class="full" /></Form.Item><Form.Item label="类目 ID" required><InputNumber v-model:value="form.categoryId" class="full" /></Form.Item><Form.Item label="商品名称" required><Input v-model:value="form.name" /></Form.Item><Form.Item label="SKU 编码" required><Input v-model:value="form.skuCode" /></Form.Item><Form.Item label="原价" required><InputNumber v-model:value="form.originalPrice" class="full" /></Form.Item><Form.Item label="促销价"><InputNumber v-model:value="form.promotionPrice" class="full" /></Form.Item><Form.Item v-if="editingId" label="改价原因"><Input v-model:value="form.priceChangeReason" /></Form.Item>
        </template>
        <template v-else-if="route.name === 'MerchantCoupons'">
          <Form.Item label="券名称" name="name" :rules="[{ required: true, message: '请填写券名称' }]"><Input v-model:value="couponForm.name" /></Form.Item>
          <Form.Item label="满额" name="minimumAmount" :rules="[{ required: true, message: '请填写满额' }]"><InputNumber v-model:value="couponForm.minimumAmount" :min="0" class="full" /></Form.Item>
          <Form.Item label="优惠金额" name="discountAmount" :rules="[{ required: true, message: '请填写优惠金额' }]"><InputNumber v-model:value="couponForm.discountAmount" :min="0.01" class="full" /></Form.Item>
          <Form.Item label="发放总量" name="totalQuantity" :rules="[{ required: true, message: '请填写发放总量' }]"><InputNumber v-model:value="couponForm.totalQuantity" :min="1" class="full" /></Form.Item>
          <Form.Item label="领取开始时间"><DatePicker v-model:value="couponForm.claimStartsAt" show-time class="full" /></Form.Item>
          <Form.Item label="领取结束时间"><DatePicker v-model:value="couponForm.claimEndsAt" show-time class="full" /></Form.Item>
          <Form.Item label="有效期至" name="expiresAt" :rules="[{ required: true, message: '请选择有效期' }]"><DatePicker v-model:value="couponForm.expiresAt" show-time class="full" /></Form.Item>
        </template>
        <template v-else-if="route.name === 'MerchantFreight'">
          <Form.Item label="固定运费" required><InputNumber v-model:value="form.fixedAmount" :min="0" class="full" /></Form.Item><Form.Item label="包邮门槛"><InputNumber v-model:value="form.freeThreshold" :min="0" class="full" /></Form.Item>
        </template>
        <Form.Item v-else label="提现金额" required><InputNumber v-model:value="form.amount" :min="0.01" :precision="2" class="full" /></Form.Item>
        <Space><Button :loading="loading" html-type="submit" type="primary">{{ editingId && route.name === 'MerchantProducts' || couponEditingId && route.name === 'MerchantCoupons' ? '保存编辑' : page.action }}</Button><Button v-if="editingId && route.name === 'MerchantProducts'" @click="editingId = undefined">取消编辑</Button><Button v-if="couponEditingId && route.name === 'MerchantCoupons'" @click="resetCouponForm">取消编辑</Button><Button @click="load">刷新</Button></Space>
      </Form>
    </Card>
    <Card v-if="columns.length" :bordered="false" title="当前记录"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="route.name === 'MerchantProducts' ? { current: productPage + 1, pageSize, total: productTotal, showSizeChanger: false } : route.name === 'MerchantCoupons' ? { pageSize } : false" row-key="id" @change="changeProductPage"><template #bodyCell="{ column, record }"><Space v-if="column.key === 'actions'"><Button v-if="route.name === 'MerchantProducts' || route.name === 'MerchantCoupons' && Number(record.availableQuantity) === Number(record.totalQuantity)" type="link" @click="edit(record)">编辑</Button><Button type="link" @click="changeStatus(record)">{{ route.name === 'MerchantProducts' ? (record.saleStatus === 'AVAILABLE' ? '下架' : '上架') : (record.status === 'ACTIVE' ? '停用' : '启用') }}</Button></Space><span v-else-if="column.key === 'expiresAt'">{{ new Date(record.expiresAt).toLocaleString() }}</span></template></Table></Card>
  </main>
</template>

<style scoped>
.console-page { display: grid; gap: 16px; padding: 24px; } h1 { font-size: 24px; margin: 10px 0 6px; } p { color: #64748b; margin: 0; }.full { width: 100%; }.balance { color: #0f766e; font-size: 30px; }
@media (max-width: 640px) { .console-page { padding: 16px; } }
</style>
