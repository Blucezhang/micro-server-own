<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Alert, Button, Card, Form, InputNumber, Space, Table, Tag } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';
import type { BuyerCoupon } from '#/api/marketplace';

const coupons = ref<BuyerCoupon[]>([]);
const route = useRoute();
const router = useRouter();
const checkoutReturn = computed(() => {
  const redirect = route.query.redirect;
  return typeof redirect === 'string' && /^\/buyer\/checkout(?:[?#]|$)/.test(redirect)
    ? redirect
    : '';
});
const listLoading = ref(false);
const claimLoading = ref(false);
const listError = ref('');
const feedback = ref<{ text: string; type: 'error' | 'success' } | null>(null);
const form = reactive({ merchantTemplateId: undefined as number | undefined });
const claimAttempt = computed(() => ({
  key: crypto.randomUUID(),
  merchantTemplateId: form.merchantTemplateId,
}));
const columns = [
  { dataIndex: 'couponNo', key: 'couponNo', title: '券号' },
  { key: 'type', title: '类型' },
  { dataIndex: 'merchantId', key: 'merchantId', title: '商家 ID' },
  { dataIndex: 'minimumAmount', key: 'minimumAmount', title: '使用门槛' },
  { dataIndex: 'discountAmount', key: 'discountAmount', title: '优惠金额' },
  { key: 'expiry', title: '有效期至' },
  { key: 'status', title: '状态' },
];
const statusNames: Record<BuyerCoupon['status'], string> = {
  AVAILABLE: '可使用',
  EXPIRED: '已过期',
  RESERVED: '订单占用中',
  USED: '已使用',
};

watch(() => form.merchantTemplateId, () => { feedback.value = null; }, { flush: 'sync' });

function formatExpiry(value: BuyerCoupon['expiresAt']) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

async function loadCoupons() {
  listLoading.value = true;
  listError.value = '';
  try {
    coupons.value = await buyerApi.coupons();
  } catch (error) {
    listError.value = error instanceof Error ? error.message : '券包加载失败，请刷新重试';
  } finally {
    listLoading.value = false;
  }
}

async function claim() {
  if (claimLoading.value) return;
  const { key, merchantTemplateId } = claimAttempt.value;
  if (!Number.isSafeInteger(merchantTemplateId) || !merchantTemplateId || merchantTemplateId <= 0) {
    feedback.value = { text: '请输入正整数商家券模板 ID', type: 'error' };
    return;
  }
  claimLoading.value = true;
  feedback.value = null;
  try {
    await buyerApi.claimMerchantCoupon(merchantTemplateId, key);
    form.merchantTemplateId = undefined;
    feedback.value = { text: '领取成功，券包已刷新', type: 'success' };
    await loadCoupons();
  } catch (error) {
    feedback.value = {
      text: error instanceof Error ? error.message : '领取失败，请重试',
      type: 'error',
    };
  } finally {
    claimLoading.value = false;
  }
}

onMounted(loadCoupons);
</script>

<template>
  <main class="page">
    <Card :bordered="false">
      <Tag color="cyan">买家中心</Tag>
      <h1>我的优惠券</h1>
      <p>已领取优惠券可在结算时选择；最终使用条件由服务端试算确认。</p>
      <Button v-if="checkoutReturn" class="return-action" @click="router.push(checkoutReturn)">返回结算</Button>
    </Card>
    <Alert
      message="当前尚无公开的可领券活动列表"
      description="如已从商家或运营方获得有效模板 ID，可在下方领取对应店铺券。"
      show-icon
      type="info"
    />
    <Card :bordered="false" title="领取商家券">
      <Form :model="form" layout="vertical" @finish="claim">
        <Form.Item label="商家券模板 ID" required>
          <InputNumber v-model:value="form.merchantTemplateId" :min="1" :precision="0" class="template-id" />
        </Form.Item>
        <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="feedback" show-icon />
        <Button :loading="claimLoading" html-type="submit" type="primary">领取优惠券</Button>
      </Form>
    </Card>
    <Card :bordered="false" title="我的券包">
      <Alert v-if="listError" :message="listError" class="feedback" show-icon type="error" />
      <Space class="refresh"><Button :loading="listLoading" @click="loadCoupons">刷新券包</Button></Space>
      <Table
        :columns="columns"
        :data-source="coupons"
        :loading="listLoading"
        :pagination="{ pageSize: 10, showSizeChanger: false }"
        :scroll="{ x: 940 }"
        row-key="couponNo"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">{{ record.couponType === 'MALL' ? '平台券' : '店铺券' }}</template>
          <template v-else-if="column.key === 'expiry'">{{ formatExpiry(record.expiresAt) }}</template>
          <Tag v-else-if="column.key === 'status'" :color="record.status === 'AVAILABLE' ? 'green' : 'default'">
            {{ statusNames[record.status as BuyerCoupon['status']] || record.status }}
          </Tag>
        </template>
      </Table>
    </Card>
  </main>
</template>

<style scoped>
.page { display: grid; grid-template-columns: minmax(0, 1fr); gap: 16px; padding: 24px; }
h1 { font-size: 24px; margin: 10px 0 6px; }
p { color: #64748b; margin: 0; }
.template-id { width: 100%; }
.feedback { margin-bottom: 16px; }
.refresh { margin-bottom: 12px; }
.return-action { margin-top: 16px; }
@media (max-width: 640px) { .page { padding: 16px; } }
</style>
