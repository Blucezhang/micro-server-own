<script lang="ts" setup>
import type { LogisticsTrace } from '#/api/marketplace';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Alert, Button, Descriptions, Empty, Form, Input, Popconfirm, Space, Table, Tag } from 'ant-design-vue';

import { buyerApi, paymentApi } from '#/api/marketplace';

type Order = { orderNo: string; status: string; payableAmount: number | string; createdAt: string; shippingAddress?: string };
type SubOrder = { subOrderNo: string; status: string; merchantId: number; logisticsCompany?: string; trackingNo?: string };
type OrderItem = { id: number; subOrderNo: string; productName: string; quantity: number; lineTotal: number | string };
type OrderDetail = { order: Order; subOrders: SubOrder[]; items: OrderItem[] };
type Payment = { paymentNo: string; status: string; amount?: number | string };
const router = useRouter();
const loading = ref(false);
const actionLoading = ref(false);
const detailLoading = ref(false);
const traceLoading = ref(false);
const rows = ref<Order[]>([]);
const detail = ref<OrderDetail>();
const payment = ref<Payment>();
const traces = ref<LogisticsTrace[]>([]);
const traceSubOrderNo = ref('');
const selectedOrderNo = ref('');
const paymentQuery = reactive({ paymentNo: '' });
const payForm = reactive({ channel: 'MOCK', orderNo: '' });
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const error = ref('');
const feedback = ref('');
const traceError = ref('');
const paymentAttempt = computed(() => ({ data: { ...payForm }, key: crypto.randomUUID() }));
const actionKeys = new Map<string, string>();
const columns = [
  { dataIndex: 'orderNo', key: 'orderNo', title: '订单号' },
  { dataIndex: 'status', key: 'status', title: '状态' },
  { dataIndex: 'payableAmount', key: 'payableAmount', title: '应付金额' },
  { key: 'createdAt', title: '创建时间' },
  { key: 'actions', title: '操作' },
];
const subOrderColumns = [
  { dataIndex: 'subOrderNo', key: 'subOrderNo', title: '子订单号' },
  { dataIndex: 'status', key: 'status', title: '状态' },
  { key: 'logistics', title: '物流' },
  { key: 'actions', title: '操作' },
];
const itemColumns = [
  { dataIndex: 'productName', key: 'productName', title: '商品' },
  { dataIndex: 'quantity', key: 'quantity', title: '数量' },
  { dataIndex: 'lineTotal', key: 'lineTotal', title: '小计' },
  { key: 'actions', title: '操作' },
];
function errorText(reason: unknown, fallback: string) { return reason instanceof Error ? reason.message : fallback; }
function formatDate(value: string) { const date = new Date(value); return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN'); }
function canAfterSale(subOrderNo: string) { return detail.value?.subOrders.some((sub) => sub.subOrderNo === subOrderNo && ['RECEIVED', 'SHIPPED'].includes(sub.status)); }
async function load() {
  loading.value = true; error.value = '';
  try {
    const result = await buyerApi.orders({ page: page.value, size: pageSize }) as { items: Order[]; total: number };
    rows.value = result.items || []; total.value = Number(result.total || 0);
  } catch (reason) { error.value = errorText(reason, '订单列表加载失败'); }
  finally { loading.value = false; }
}
async function openDetail(no: string) {
  detailLoading.value = true; error.value = ''; feedback.value = '';
  traceSubOrderNo.value = ''; traces.value = [];
  try {
    detail.value = await buyerApi.orderDetail(no) as OrderDetail;
    selectedOrderNo.value = no; payForm.orderNo = no;
  } catch (reason) { error.value = errorText(reason, '订单详情加载失败'); }
  finally { detailLoading.value = false; }
}
async function cancel(no: string) {
  if (actionLoading.value) return;
  const id = `cancel:${no}`;
  const key = actionKeys.get(id) || crypto.randomUUID();
  actionKeys.set(id, key); actionLoading.value = true; error.value = '';
  try {
    const order = await buyerApi.cancelOrder(no, key) as Order;
    actionKeys.delete(id); rows.value = rows.value.map((row) => row.orderNo === no ? order : row);
    if (detail.value?.order.orderNo === no) detail.value.order = order;
    feedback.value = '订单已取消';
  } catch (reason) { error.value = errorText(reason, '取消失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function receive(no: string) {
  if (actionLoading.value) return;
  const id = `receive:${no}`;
  const key = actionKeys.get(id) || crypto.randomUUID();
  actionKeys.set(id, key); actionLoading.value = true; error.value = '';
  try {
    const subOrder = await buyerApi.receiveSubOrder(no, key) as SubOrder;
    actionKeys.delete(id);
    if (detail.value) detail.value.subOrders = detail.value.subOrders.map((item) => item.subOrderNo === no ? subOrder : item);
    feedback.value = '已确认签收';
  } catch (reason) { error.value = errorText(reason, '签收失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function showTraces(no: string) {
  traceSubOrderNo.value = no; traceLoading.value = true; traceError.value = '';
  try { traces.value = await buyerApi.logisticsTraces(no); }
  catch (reason) { traces.value = []; traceError.value = errorText(reason, '物流轨迹加载失败'); }
  finally { traceLoading.value = false; }
}
async function createPayment() {
  if (actionLoading.value) return;
  const attempt = paymentAttempt.value;
  actionLoading.value = true; error.value = '';
  try {
    payment.value = await paymentApi.create(attempt.data, attempt.key) as Payment;
    paymentQuery.paymentNo = payment.value.paymentNo;
    feedback.value = '支付单已创建，请查询支付结果';
  } catch (reason) { error.value = errorText(reason, '支付单创建失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function queryPayment() {
  if (!paymentQuery.paymentNo.trim()) return;
  actionLoading.value = true; error.value = '';
  try { payment.value = await paymentApi.detail(paymentQuery.paymentNo.trim()) as Payment; }
  catch (reason) { error.value = errorText(reason, '支付状态查询失败'); }
  finally { actionLoading.value = false; }
}
async function changePage(pagination: { current?: number }) { page.value = (pagination.current || 1) - 1; await load(); }
onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head"><div><span class="market-overline">BUYER · 订单</span><h1 class="market-heading">订单与物流</h1><p class="market-subtitle">查看订单进度、物流轨迹与签收状态。支付功能目前只接入模拟支付单。</p></div><Button @click="load">刷新订单</Button></header>
    <section class="market-panel" aria-labelledby="orders-title"><h2 id="orders-title" class="market-panel-title">我的订单</h2><Alert v-if="error" :message="error" type="error" show-icon class="notice" /><Alert v-if="feedback" :message="feedback" type="success" show-icon class="notice" /><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" :scroll="{ x: 840 }" row-key="orderNo" @change="changePage"><template #bodyCell="{ column, record }"><span v-if="column.key === 'createdAt'">{{ formatDate(record.createdAt) }}</span><span v-else-if="column.key === 'payableAmount'">¥{{ record.payableAmount }}</span><Tag v-else-if="column.key === 'status'">{{ record.status }}</Tag><Space v-else-if="column.key === 'actions'"><Button type="link" :loading="detailLoading" @click="openDetail(record.orderNo)">详情</Button><Popconfirm v-if="record.status === 'PENDING_PAYMENT'" title="确认取消订单？" @confirm="cancel(record.orderNo)"><Button danger type="link" :disabled="actionLoading">取消</Button></Popconfirm></Space></template></Table></section>
    <template v-if="detail">
      <section class="market-panel" aria-labelledby="order-detail"><div class="detail-head"><h2 id="order-detail" class="market-panel-title">订单详情 · {{ selectedOrderNo }}</h2><Button @click="detail = undefined">关闭</Button></div><Descriptions :column="{ xs: 1, sm: 2 }" size="small"><Descriptions.Item label="当前状态">{{ detail.order.status }}</Descriptions.Item><Descriptions.Item label="应付金额">¥{{ detail.order.payableAmount }}</Descriptions.Item><Descriptions.Item v-if="detail.order.shippingAddress" label="收货地址">{{ detail.order.shippingAddress }}</Descriptions.Item></Descriptions></section>
      <section class="market-panel" aria-labelledby="sub-orders"><h2 id="sub-orders" class="market-panel-title">配送与签收</h2><Table :columns="subOrderColumns" :data-source="detail.subOrders || []" :pagination="false" :scroll="{ x: 760 }" row-key="subOrderNo"><template #bodyCell="{ column, record }"><Tag v-if="column.key === 'status'">{{ record.status }}</Tag><span v-else-if="column.key === 'logistics'">{{ record.logisticsCompany ? `${record.logisticsCompany} · ${record.trackingNo || '—'}` : '待发货' }}</span><Space v-else-if="column.key === 'actions'"><Button type="link" @click="showTraces(record.subOrderNo)">物流轨迹</Button><Popconfirm v-if="record.status === 'SHIPPED'" title="确认已收到商品？" @confirm="receive(record.subOrderNo)"><Button type="link" :disabled="actionLoading">确认签收</Button></Popconfirm></Space></template></Table><div v-if="traceSubOrderNo" class="trace-panel"><h3>{{ traceSubOrderNo }} · 物流轨迹</h3><Alert v-if="traceError" :message="traceError" type="error" show-icon /><Empty v-else-if="!traceLoading && !traces.length" description="暂无物流轨迹" /><ol v-else class="trace-list"><li v-for="(trace, index) in traces" :key="`${trace.createdAt}-${index}`"><span class="trace-time">{{ formatDate(trace.createdAt) }}</span><strong>{{ trace.traceStatus }}</strong><span>{{ trace.detail }}</span></li></ol></div></section>
      <section class="market-panel" aria-labelledby="order-items"><h2 id="order-items" class="market-panel-title">商品明细</h2><Table :columns="itemColumns" :data-source="detail.items || []" :pagination="false" :scroll="{ x: 620 }" row-key="id"><template #bodyCell="{ column, record }"><span v-if="column.key === 'lineTotal'">¥{{ record.lineTotal }}</span><Button v-else-if="column.key === 'actions' && canAfterSale(record.subOrderNo)" type="link" @click="router.push({ path: '/buyer/after-sales', query: { subOrderNo: record.subOrderNo, orderItemId: String(record.id) } })">申请售后</Button></template></Table></section>
      <section class="market-panel" aria-labelledby="payment-title"><h2 id="payment-title" class="market-panel-title">支付单</h2><p class="market-note">仅创建和查询模拟支付单，不在买家端提供模拟成功按钮。</p><Form v-if="detail.order.status === 'PENDING_PAYMENT'" :model="payForm" class="payment-form" layout="inline" @finish="createPayment"><Form.Item label="支付渠道">模拟支付</Form.Item><Button html-type="submit" type="primary" :loading="actionLoading">创建支付单</Button></Form><Form :model="paymentQuery" class="payment-form" layout="inline" @finish="queryPayment"><Form.Item label="支付单号"><Input v-model:value="paymentQuery.paymentNo" /></Form.Item><Button html-type="submit" :loading="actionLoading">查询结果</Button></Form><Descriptions v-if="payment" :column="1" size="small"><Descriptions.Item label="支付单号">{{ payment.paymentNo }}</Descriptions.Item><Descriptions.Item label="状态">{{ payment.status }}</Descriptions.Item><Descriptions.Item v-if="payment.amount != null" label="金额">¥{{ payment.amount }}</Descriptions.Item></Descriptions></section>
    </template>
  </main>
</template>

<style scoped>
.notice { margin-bottom: 16px; }
.detail-head { display: flex; align-items: start; justify-content: space-between; gap: 12px; }
.payment-form { margin-top: 14px; }
.trace-panel { margin-top: 22px; padding-top: 20px; border-top: 1px solid var(--market-line); }
.trace-panel h3 { margin: 0 0 12px; font-size: 14px; font-weight: 620; }
.trace-list { display: grid; gap: 12px; margin: 0; padding: 0; list-style: none; }
.trace-list li { display: grid; grid-template-columns: 160px 110px minmax(0, 1fr); gap: 10px; color: var(--market-muted); font-size: 12px; }
.trace-list strong { color: var(--market-text); font-weight: 600; }
@media (max-width: 760px) { .trace-list li { grid-template-columns: 1fr; gap: 2px; } }
</style>
