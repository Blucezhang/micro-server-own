<script lang="ts" setup>
import type { BuyerCoupon } from '#/api/marketplace';

import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Alert, Button, Empty, Form, InputNumber, Space, Table, Tag } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';

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
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive({ merchantTemplateId: undefined as number | undefined });
const claimAttempt = computed(() => ({
  key: crypto.randomUUID(),
  merchantTemplateId: form.merchantTemplateId,
}));
const availableCount = computed(() => coupons.value.filter((c) => c.status === 'AVAILABLE').length);
const reservedCount = computed(() => coupons.value.filter((c) => c.status === 'RESERVED').length);

const columns = [
  { dataIndex: 'couponNo', key: 'couponNo', title: '券号', width: 160 },
  { key: 'type', title: '类型', width: 85 },
  { key: 'merchantId', title: '商家 ID', width: 90 },
  { key: 'minimumAmount', title: '使用门槛', width: 100 },
  { key: 'discountAmount', title: '优惠金额', width: 100 },
  { key: 'expiry', title: '有效期至', width: 165 },
  { key: 'status', title: '状态', width: 90 },
];

const statusNames: Record<BuyerCoupon['status'], string> = {
  AVAILABLE: '可使用',
  EXPIRED: '已过期',
  RESERVED: '订单占用中',
  USED: '已使用',
};

function statusColor(status: BuyerCoupon['status']) {
  switch (status) {
    case 'AVAILABLE':
      return 'success';
    case 'RESERVED':
      return 'warning';
    case 'USED':
    case 'EXPIRED':
    default:
      return 'default';
  }
}

function errorMessage(reason: unknown, fallback: string) {
  if (!(reason instanceof Error)) return fallback;
  const status = reason.message.match(/(?:status code|HTTP)\s*:?[ ]*(5\d{2})/i)?.[1];
  return status ? `服务暂时不可用（HTTP ${status}），请稍后重试` : reason.message;
}

watch(() => form.merchantTemplateId, () => { feedback.value = null; }, { flush: 'sync' });

function formatMinimumAmount(amount: BuyerCoupon['minimumAmount']) {
  if (amount === undefined || amount === null || amount === '') return '无门槛';
  const num = Number(amount);
  if (Number.isNaN(num) || num <= 0) return '无门槛';
  return `满 ¥${amount}`;
}

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
    listError.value = errorMessage(error, '券包加载失败，请刷新重试');
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
      text: errorMessage(error, '领取失败，请重试'),
      type: 'error',
    };
  } finally {
    claimLoading.value = false;
  }
}

onMounted(loadCoupons);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">BUYER · 优惠券</span>
        <h1 class="market-heading">我的优惠券</h1>
        <p class="market-subtitle">已领取优惠券可在结算时选择；最终使用条件由服务端试算确认。</p>
      </div>
      <Space class="head-actions">
        <Button v-if="checkoutReturn" type="primary" @click="router.push(checkoutReturn)">
          返回结算
        </Button>
        <Button :loading="listLoading" @click="loadCoupons">刷新券包</Button>
      </Space>
    </header>

    <div class="coupon-layout">
      <!-- Main Column: Coupon List -->
      <section class="market-panel wallet-panel" aria-labelledby="coupons-title">
        <div class="panel-head">
          <div class="panel-title-group">
            <h2 id="coupons-title" class="market-panel-title">我的券包</h2>
            <span v-if="!listError && !listLoading" class="count-badge">{{ coupons.length }} 张</span>
          </div>
          <div v-if="coupons.length" class="summary-pills">
            <span class="pill pill-available">可用 {{ availableCount }}</span>
            <span v-if="reservedCount" class="pill pill-reserved">占用中 {{ reservedCount }}</span>
          </div>
        </div>

        <Alert v-if="listError" :message="listError" class="panel-alert" show-icon type="error" />

        <Table
          :columns="columns"
          :data-source="coupons"
          :loading="listLoading"
          :pagination="{ pageSize: 10, showSizeChanger: false }"
          :scroll="{ x: 740 }"
          row-key="couponNo"
          size="middle"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'couponNo'">
              <span class="mono-code">{{ record.couponNo }}</span>
            </template>
            <template v-else-if="column.key === 'type'">
              <Tag :color="record.couponType === 'MALL' ? 'blue' : 'purple'" class="type-tag">
                {{ record.couponType === 'MALL' ? '平台券' : '店铺券' }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'merchantId'">
              <span class="merchant-cell">{{ record.couponType === 'MALL' ? '全平台' : `#${record.merchantId}` }}</span>
            </template>
            <template v-else-if="column.key === 'minimumAmount'">
              <span class="amount-cell">{{ formatMinimumAmount(record.minimumAmount) }}</span>
            </template>
            <template v-else-if="column.key === 'discountAmount'">
              <span class="discount-cell">¥<strong>{{ record.discountAmount }}</strong></span>
            </template>
            <template v-else-if="column.key === 'expiry'">
              <span class="expiry-cell">{{ formatExpiry(record.expiresAt) }}</span>
            </template>
            <template v-else-if="column.key === 'status'">
              <Tag :color="statusColor(record.status)">
                {{ statusNames[record.status as BuyerCoupon['status']] || record.status }}
              </Tag>
            </template>
          </template>
          <template #emptyText>
            <div v-if="!listError && !listLoading" class="table-empty-wrap">
              <Empty description="券包中暂无优惠券" :image="Empty.PRESENTED_IMAGE_SIMPLE">
                <p class="empty-tip">如有商家提供的模板 ID，可在右侧输入并领取</p>
              </Empty>
            </div>
          </template>
        </Table>
      </section>

      <!-- Sidebar Column: Claim & Guide -->
      <section class="market-panel claim-panel" aria-labelledby="claim-title">
        <div class="claim-head">
          <h2 id="claim-title" class="market-panel-title">领取商家券</h2>
          <p class="panel-subtitle">联系商家获取模板 ID 后，在此输入即可领取专属店铺券</p>
        </div>

        <Alert
          class="compact-notice"
          message="暂不提供公开领券列表。商家可在优惠券模板中复制 ID 发给买家，买家在此领取。"
          show-icon
          type="info"
        />

        <Form :model="form" layout="vertical" class="claim-form" @finish="claim">
          <Form.Item label="商家券模板 ID" required class="form-item-tight">
            <InputNumber
              v-model:value="form.merchantTemplateId"
              :min="1"
              :precision="0"
              placeholder="请输入有效模板 ID"
              class="full"
            />
          </Form.Item>
          <Button :loading="claimLoading" html-type="submit" type="primary" block class="claim-btn">
            领取优惠券
          </Button>
        </Form>

        <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="feedback-alert" show-icon />

        <div class="guide-box">
          <h3 class="guide-title">使用说明</h3>
          <ul class="guide-list">
            <li>可在结算时选择已领取的优惠券。</li>
            <li>优惠券适用条件与最终抵扣金额以结算服务端试算为准。</li>
          </ul>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.full {
  width: 100%;
}

.head-actions {
  display: flex;
  align-items: center;
}

.coupon-layout {
  display: grid;
  grid-template-columns: minmax(0, 2.2fr) minmax(300px, 1fr);
  gap: 20px;
  align-items: start;
}

.wallet-panel, .claim-panel {
  min-width: 0;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
}

.panel-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-title-group .market-panel-title {
  margin: 0;
}

.count-badge {
  display: inline-block;
  padding: 1px 8px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
  background: hsl(var(--muted));
  color: var(--market-muted);
}

.summary-pills {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pill {
  font-size: 11.5px;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 550;
}

.pill-available {
  background: hsl(var(--accent) / 0.5);
  color: hsl(var(--accent-foreground));
}

.pill-reserved {
  background: hsl(var(--muted));
  color: var(--market-muted);
}

.panel-alert {
  margin-bottom: 14px;
}

.mono-code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12.5px;
  color: var(--market-text);
  font-weight: 500;
}

.type-tag {
  border-radius: 4px;
  font-size: 11.5px;
}

.merchant-cell {
  font-size: 12.5px;
  color: var(--market-muted);
  font-family: ui-monospace, SFMono-Regular, monospace;
}

.amount-cell {
  font-size: 13px;
  color: var(--market-text);
}

.discount-cell {
  font-size: 13px;
  color: hsl(var(--accent-foreground));
}

.discount-cell strong {
  font-size: 14px;
  font-weight: 650;
}

.discount-cell .currency {
  font-size: 11px;
  margin-right: 2px;
}

.expiry-cell {
  font-size: 12.5px;
  color: var(--market-muted);
}

.table-empty-wrap {
  padding: 8px 0;
}

.empty-tip {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: var(--market-muted);
}

/* Claim Panel */
.claim-head {
  margin-bottom: 12px;
}

.claim-head .market-panel-title {
  margin: 0 0 4px;
}

.panel-subtitle {
  margin: 0;
  font-size: 12.5px;
  color: var(--market-muted);
  line-height: 1.45;
}

.compact-notice {
  margin-bottom: 14px;
  border-radius: var(--mall-radius-base);
  font-size: 12.5px;
}

.compact-notice :deep(.ant-alert-message) {
  font-size: 12.5px;
}

.form-item-tight {
  margin-bottom: 14px;
}

.claim-btn {
  margin-bottom: 14px;
}

.feedback-alert {
  margin-bottom: 16px;
}

/* Guide Box */
.guide-box {
  padding: 14px 16px;
  border-radius: var(--mall-radius-base);
  border: 1px solid var(--market-line);
  background: hsl(var(--muted) / 0.35);
}

.guide-title {
  margin: 0 0 8px;
  font-size: 12.5px;
  font-weight: 650;
  color: var(--market-text);
}

.guide-list {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--market-muted);
}

.guide-list li {
  margin-bottom: 4px;
}

.guide-list li:last-child {
  margin-bottom: 0;
}

@media (max-width: 1440px) {
  .coupon-layout {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 640px) {
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 680px) {
  .table-empty-wrap { padding: 6px 0; }
}
</style>
