<script lang="ts" setup>
import type { AfterSale } from '#/api/marketplace';

import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Alert, Button, Descriptions, Empty, Form, Input, InputNumber, Popconfirm, Select, Table, Tag } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';
import { afterSaleStatusNames, afterSaleTypeNames } from '#/api/marketplace-models';

type Detail = { afterSale: AfterSale; events: { eventType: string; occurredAt: string }[] };
const route = useRoute();
const loading = ref(false);
const actionLoading = ref(false);
const rows = ref<AfterSale[]>([]);
const selected = ref<Detail>();
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const error = ref('');
const feedback = ref('');
const form = reactive({ orderItemId: undefined as number | undefined, quantity: 1, reason: '', subOrderNo: '', type: 'REFUND_ONLY' as AfterSale['type'] });
const shipment = reactive({ logisticsCompany: '', trackingNo: '' });
const createAttempt = computed(() => ({ data: { reason: form.reason.trim(), subOrderNo: form.subOrderNo.trim(), type: form.type, items: [{ orderItemId: form.orderItemId, quantity: form.quantity }] }, key: crypto.randomUUID() }));
const shipmentAttempt = computed(() => ({ no: selected.value?.afterSale.afterSaleNo, logisticsCompany: shipment.logisticsCompany.trim(), trackingNo: shipment.trackingNo.trim(), key: crypto.randomUUID() }));
const actionKeys = new Map<string, string>();
const columns = [
  { dataIndex: 'afterSaleNo', key: 'afterSaleNo', title: '售后单号' },
  { key: 'type', title: '类型' },
  { key: 'status', title: '状态' },
  { dataIndex: 'requestedAmount', key: 'requestedAmount', title: '申请金额' },
  { key: 'actions', title: '操作' },
];
function errorText(reason: unknown, fallback: string) { return reason instanceof Error ? reason.message : fallback; }
function applyResult(sale: AfterSale) {
  if (selected.value) selected.value.afterSale = sale;
  rows.value = rows.value.map((item) => item.afterSaleNo === sale.afterSaleNo ? sale : item);
}
async function load() {
  loading.value = true; error.value = '';
  try {
    const result = await buyerApi.afterSales({ page: page.value, size: pageSize });
    rows.value = result.items || []; total.value = Number(result.total || 0);
  } catch (reason) { error.value = errorText(reason, '售后列表加载失败'); }
  finally { loading.value = false; }
}
async function openDetail(no: string) {
  error.value = ''; feedback.value = '';
  try { selected.value = await buyerApi.afterSaleDetail(no) as Detail; }
  catch (reason) { error.value = errorText(reason, '售后详情加载失败'); }
}
async function submit() {
  if (actionLoading.value) return;
  const attempt = createAttempt.value;
  if (!attempt.data.subOrderNo || !Number.isSafeInteger(form.orderItemId) || !form.orderItemId || form.orderItemId <= 0 || !Number.isSafeInteger(form.quantity) || form.quantity < 1 || !attempt.data.reason) {
    error.value = '请填写子订单号、有效明细 ID、数量和申请原因'; return;
  }
  actionLoading.value = true; error.value = ''; feedback.value = '';
  try {
    const sale = await buyerApi.createAfterSale(attempt.data, attempt.key);
    rows.value = [sale, ...rows.value]; total.value += 1;
    Object.assign(form, { orderItemId: undefined, quantity: 1, reason: '', subOrderNo: '', type: 'REFUND_ONLY' });
    feedback.value = '售后申请已提交';
  } catch (reason) { error.value = errorText(reason, '申请失败，请在当前页面重试'); }
  finally { actionLoading.value = false; }
}
async function act(no: string, action: 'close' | 'receive') {
  if (actionLoading.value) return;
  const attemptId = `${action}:${no}`;
  const key = actionKeys.get(attemptId) || crypto.randomUUID();
  actionKeys.set(attemptId, key); actionLoading.value = true; error.value = ''; feedback.value = '';
  try {
    const sale = action === 'close' ? await buyerApi.closeAfterSale(no, key) : await buyerApi.receiveExchange(no, key);
    applyResult(sale); actionKeys.delete(attemptId);
    feedback.value = action === 'close' ? '申请已关闭' : '已确认收到换货';
  } catch (reason) { error.value = errorText(reason, '操作失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function returnShipment() {
  if (actionLoading.value) return;
  const attempt = shipmentAttempt.value;
  if (!attempt.no || !attempt.logisticsCompany || !attempt.trackingNo) { error.value = '请填写物流公司和单号'; return; }
  actionLoading.value = true; error.value = ''; feedback.value = '';
  try {
    applyResult(await buyerApi.returnShipment(attempt.no, { logisticsCompany: attempt.logisticsCompany, trackingNo: attempt.trackingNo }, attempt.key));
    Object.assign(shipment, { logisticsCompany: '', trackingNo: '' });
    feedback.value = '退货物流已提交';
  } catch (reason) { error.value = errorText(reason, '提交失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function changePage(pagination: { current?: number }) { page.value = (pagination.current || 1) - 1; await load(); }
watch(() => [route.query.subOrderNo, route.query.orderItemId], ([subOrderNo, orderItemId]) => {
  if (typeof subOrderNo === 'string') form.subOrderNo = subOrderNo;
  const parsed = typeof orderItemId === 'string' ? Number(orderItemId) : NaN;
  if (Number.isSafeInteger(parsed) && parsed > 0) form.orderItemId = parsed;
}, { immediate: true });
onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head"><div><span class="market-overline">BUYER · 售后</span><h1 class="market-heading">售后服务</h1><p class="market-subtitle">从已发货或已签收的子订单发起申请；状态变化和退款以订单服务为准。</p></div><Button @click="load">刷新列表</Button></header>
    <section class="market-panel" aria-labelledby="my-after-sales"><h2 id="my-after-sales" class="market-panel-title">我的申请</h2><Alert v-if="error" :message="error" type="error" show-icon class="notice" /><Alert v-if="feedback" :message="feedback" type="success" show-icon class="notice" /><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" :scroll="{ x: 760 }" row-key="afterSaleNo" @change="changePage"><template #bodyCell="{ column, record }"><span v-if="column.key === 'type'">{{ afterSaleTypeNames[record.type as AfterSale['type']] || record.type }}</span><Tag v-else-if="column.key === 'status'">{{ afterSaleStatusNames[record.status as AfterSale['status']] || record.status }}</Tag><span v-else-if="column.key === 'requestedAmount'">¥{{ record.requestedAmount }}</span><Button v-else-if="column.key === 'actions'" type="link" @click="openDetail(record.afterSaleNo)">查看进度</Button></template></Table></section>
    <div class="market-grid">
      <section class="market-panel" aria-labelledby="new-after-sale"><h2 id="new-after-sale" class="market-panel-title">提交申请</h2><p class="market-note">可从订单明细跳转并自动填入子订单和明细 ID；申请金额由服务端计算。</p><Form :model="form" layout="vertical" @finish="submit"><Form.Item label="子订单号" name="subOrderNo" :rules="[{ required: true, message: '请输入子订单号' }]"><Input v-model:value="form.subOrderNo" /></Form.Item><Form.Item label="售后类型" name="type" :rules="[{ required: true }]"><Select v-model:value="form.type" :options="Object.entries(afterSaleTypeNames).map(([value, label]) => ({ value, label }))" /></Form.Item><Form.Item label="订单明细 ID" name="orderItemId" :rules="[{ required: true, type: 'number', min: 1 }]"><InputNumber v-model:value="form.orderItemId" :min="1" :precision="0" class="full" /></Form.Item><Form.Item label="数量" name="quantity" :rules="[{ required: true, type: 'number', min: 1 }]"><InputNumber v-model:value="form.quantity" :min="1" :precision="0" class="full" /></Form.Item><Form.Item label="申请原因" name="reason" :rules="[{ required: true, message: '请填写申请原因' }]"><Input.TextArea v-model:value="form.reason" :maxlength="500" :rows="3" show-count /></Form.Item><Button html-type="submit" type="primary" :loading="actionLoading">提交售后申请</Button></Form></section>
      <section v-if="selected" class="market-panel" aria-labelledby="after-sale-detail"><div class="detail-head"><h2 id="after-sale-detail" class="market-panel-title">进度 · {{ selected.afterSale.afterSaleNo }}</h2><Button @click="selected = undefined">关闭</Button></div><Descriptions :column="1" size="small"><Descriptions.Item label="状态">{{ afterSaleStatusNames[selected.afterSale.status] }}</Descriptions.Item><Descriptions.Item label="申请金额">¥{{ selected.afterSale.requestedAmount }}</Descriptions.Item><Descriptions.Item label="处理备注">{{ selected.afterSale.merchantRemark || '—' }}</Descriptions.Item><Descriptions.Item v-if="selected.afterSale.returnTrackingNo" label="物流">{{ selected.afterSale.returnCompany }} · {{ selected.afterSale.returnTrackingNo }}</Descriptions.Item></Descriptions><div v-if="selected.afterSale.status === 'APPROVED'" class="action-panel"><h3>填写退货物流</h3><Form :model="shipment" layout="vertical" @finish="returnShipment"><Form.Item label="物流公司" name="logisticsCompany" :rules="[{ required: true }]"><Input v-model:value="shipment.logisticsCompany" :maxlength="100" /></Form.Item><Form.Item label="物流单号" name="trackingNo" :rules="[{ required: true }]"><Input v-model:value="shipment.trackingNo" :maxlength="100" /></Form.Item><Button html-type="submit" type="primary" :loading="actionLoading">提交物流</Button></Form></div><div v-else-if="selected.afterSale.status === 'APPLYING'" class="action-panel"><Popconfirm title="确认撤回这条售后申请？" @confirm="act(selected.afterSale.afterSaleNo, 'close')"><Button :loading="actionLoading">撤回申请</Button></Popconfirm></div><div v-else-if="selected.afterSale.status === 'EXCHANGE_SHIPPED'" class="action-panel"><Popconfirm title="确认已收到换货商品？" @confirm="act(selected.afterSale.afterSaleNo, 'receive')"><Button type="primary" :loading="actionLoading">确认收到换货</Button></Popconfirm></div><Empty v-else description="当前没有待操作事项" /><div v-if="selected.events?.length" class="events"><h3>处理记录</h3><ol><li v-for="event in selected.events" :key="`${event.eventType}-${event.occurredAt}`"><strong>{{ event.eventType }}</strong><span>{{ new Date(event.occurredAt).toLocaleString('zh-CN') }}</span></li></ol></div></section>
      <section v-else class="market-panel empty-panel"><span class="market-overline">进度</span><h2 class="market-panel-title">选择一条申请</h2><p class="market-note">在上方列表中打开售后单，即可查看状态及下一步操作。</p></section>
    </div>
  </main>
</template>

<style scoped>
.full { width: 100%; }
.notice { margin-bottom: 16px; }
.detail-head { display: flex; align-items: start; justify-content: space-between; gap: 12px; }
.action-panel, .events { margin-top: 22px; padding-top: 20px; border-top: 1px solid var(--market-line); }
.action-panel h3, .events h3 { margin: 0 0 12px; font-size: 14px; font-weight: 620; }
.events ol { display: grid; gap: 8px; margin: 0; padding-left: 20px; }
.events li { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 10px; color: var(--market-muted); font-size: 12px; }
.events strong { color: var(--market-text); font-weight: 550; }
.empty-panel { align-self: stretch; }
</style>
