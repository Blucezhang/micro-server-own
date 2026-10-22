<script lang="ts" setup>
import type { LogisticsTrace } from '#/api/marketplace';
import { useBuyerOrderTableColumns } from './buyer-orders-table-columns';

import { computed, onMounted, reactive, ref } from 'vue';
import { useMediaQuery, useWindowSize } from '@vueuse/core';
import { useRouter } from 'vue-router';

import { Alert, Button, Descriptions, Empty, Form, Input, Popconfirm, Space, Spin, Table, Tag } from 'ant-design-vue';

import { buyerApi, paymentApi } from '#/api/marketplace';

type Order = { orderNo: string; status: string; payableAmount: number | string; createdAt: string; shippingAddress?: string };
type SubOrder = { subOrderNo: string; status: string; merchantId: number; logisticsCompany?: string; trackingNo?: string };
type OrderItem = { id: number; subOrderNo: string; productName: string; quantity: number; lineTotal: number | string };
type OrderDetail = { order: Order; subOrders: SubOrder[]; items: OrderItem[] };
type Payment = { paymentNo: string; status: string; amount?: number | string };

const ORDER_STATUS_MAP: Record<string, string> = {
  PROCESSING: '处理中', PENDING_PAYMENT: '待支付', PAID: '已支付', FULFILLING: '履约中',
  COMPLETED: '已完成', CANCELED: '已取消', REFUNDING: '退款中', REFUNDED: '已退款', SAGA_FAILED: '处理异常',
};
const SUB_ORDER_STATUS_MAP: Record<string, string> = {
  PROCESSING: '处理中', PENDING_PAYMENT: '待支付', TO_SHIP: '待发货', SHIPPED: '已发货',
  RECEIVED: '已签收', CANCELED: '已取消', REFUNDED: '已退款',
};
const PAYMENT_STATUS_MAP: Record<string, string> = {
  CREATED: '已创建', SUCCEEDED: '支付成功', FAILED: '支付失败', REFUNDED: '已退款',
};
const TRACE_STATUS_MAP: Record<string, string> = {
  SHIPPED: '已发货', AUTO_RECEIVED: '自动签收', SHIPMENT_CORRECTED: '发货更正',
};

function formatOrderStatus(status?: string): string {
  if (!status) return '—';
  return ORDER_STATUS_MAP[status] || status;
}

function formatSubOrderStatus(status?: string): string {
  if (!status) return '—';
  return SUB_ORDER_STATUS_MAP[status] || status;
}

function formatPaymentStatus(status?: string): string {
  if (!status) return '—';
  return PAYMENT_STATUS_MAP[status] || status;
}

function formatTraceStatus(status?: string): string {
  if (!status) return '—';
  return TRACE_STATUS_MAP[status] || status;
}

const router = useRouter();
const loading = ref(false);
const actionLoading = ref(false);
const detailLoading = ref(false);
const loadingOrderNo = ref('');
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
const phoneLayout = useMediaQuery('(max-width: 680px)');
const { width: viewportWidth } = useWindowSize();
const ordersError = ref('');
const detailError = ref('');
const actionError = ref('');
const feedback = ref('');
const traceError = ref('');
let traceRequestId = 0;
let detailRequestId = 0;
let paymentRequestId = 0;
const paymentAttempt = computed(() => ({ data: { ...payForm }, key: crypto.randomUUID() }));
const actionKeys = new Map<string, string>();
const { orders: responsiveColumns, subOrders: responsiveSubOrderColumns, items: responsiveItemColumns } =
  useBuyerOrderTableColumns(phoneLayout, viewportWidth);

function isRawError(msg: string): boolean {
  if (!msg) return true;
  if (/^request failed with status code/i.test(msg)) return true;
  if (/network\s*error/i.test(msg)) return true;
  if (/timeout of \d+ms exceeded/i.test(msg)) return true;
  if (/axioserror/i.test(msg)) return true;
  if (/^(bad gateway|gateway timeout|internal server error|service unavailable)$/i.test(msg)) return true;
  if (/^(econnrefused|etimedout|enotfound|err_)/i.test(msg)) return true;
  return false;
}

function cleanFallback(fallback: string): string {
  return fallback.replace(/，?请(稍后)?重试$/, '');
}

function formatError(reason: unknown, fallback: string): string {
  if (!reason) return fallback;
  const err = reason as any;
  const data = err?.response?.data;
  const serverMsg = typeof data === 'string' ? '' : (data?.message || data?.msg || data?.error);
  if (typeof serverMsg === 'string' && serverMsg.trim() && !isRawError(serverMsg.trim())) {
    return serverMsg.trim();
  }
  const status = err?.response?.status;
  const rawMsg = String(err?.message || (reason instanceof Error ? reason.message : '')).trim();
  const base = cleanFallback(fallback);
  if (status === 502 || status === 504 || /status code 50[24]/i.test(rawMsg)) {
    return `${base}，网关响应超时或异常，请稍后重试`;
  }
  if ((typeof status === 'number' && status >= 500) || /status code 5\d\d/i.test(rawMsg)) {
    return `${base}，服务暂时不可用，请稍后重试`;
  }
  if (status === 404 || /status code 404/i.test(rawMsg)) {
    return `${base}，请求的资源不存在`;
  }
  if (status === 403 || /status code 403/i.test(rawMsg)) {
    return `${base}，暂无操作权限`;
  }
  if (status === 401 || /status code 401/i.test(rawMsg)) {
    return '登录状态已失效，请重新登录';
  }
  if (/network\s*error/i.test(rawMsg)) {
    return `${base}，网络连接异常，请检查网络后重试`;
  }
  if (/timeout/i.test(rawMsg)) {
    return `${base}，请求超时，请稍后重试`;
  }
  if (rawMsg && !isRawError(rawMsg)) {
    return rawMsg;
  }
  return fallback;
}

function formatDate(value: string) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

function canAfterSale(subOrderNo: string) {
  return detail.value?.subOrders.some((sub) => sub.subOrderNo === subOrderNo && ['RECEIVED', 'SHIPPED'].includes(sub.status));
}

async function load() {
  loading.value = true;
  ordersError.value = '';
  try {
    const result = await buyerApi.orders({ page: page.value, size: pageSize }) as { items: Order[]; total: number };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (reason) {
    rows.value = [];
    total.value = 0;
    ordersError.value = formatError(reason, '订单列表加载失败');
  } finally {
    loading.value = false;
  }
}

function resetOrderDetailState() {
  detail.value = undefined;
  selectedOrderNo.value = '';
  payment.value = undefined;
  paymentQuery.paymentNo = '';
  payForm.orderNo = '';
  paymentRequestId++;
  traceSubOrderNo.value = '';
  traces.value = [];
  traceError.value = '';
  traceLoading.value = false;
  traceRequestId++;
}

function closeDetail() {
  detailRequestId++;
  resetOrderDetailState();
}

async function openDetail(no: string) {
  resetOrderDetailState();
  loadingOrderNo.value = no;
  detailLoading.value = true;
  detailError.value = '';
  actionError.value = '';
  feedback.value = '';
  const currentRequestId = ++detailRequestId;
  try {
    const result = await buyerApi.orderDetail(no) as OrderDetail;
    if (currentRequestId !== detailRequestId) return;
    detail.value = result;
    selectedOrderNo.value = no;
    payForm.orderNo = no;
  } catch (reason) {
    if (currentRequestId !== detailRequestId) return;
    detailError.value = formatError(reason, '订单详情加载失败');
  } finally {
    if (currentRequestId === detailRequestId) {
      detailLoading.value = false;
      loadingOrderNo.value = '';
    }
  }
}

async function cancel(no: string) {
  if (actionLoading.value) return;
  const id = `cancel:${no}`;
  const key = actionKeys.get(id) || crypto.randomUUID();
  actionKeys.set(id, key);
  actionLoading.value = true;
  actionError.value = '';
  try {
    const order = await buyerApi.cancelOrder(no, key) as Order;
    actionKeys.delete(id);
    rows.value = rows.value.map((row) => row.orderNo === no ? order : row);
    if (detail.value?.order.orderNo === no) detail.value.order = order;
    feedback.value = '订单已取消';
  } catch (reason) {
    actionError.value = formatError(reason, '取消失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function receive(no: string) {
  if (actionLoading.value) return;
  const id = `receive:${no}`;
  const key = actionKeys.get(id) || crypto.randomUUID();
  actionKeys.set(id, key);
  actionLoading.value = true;
  actionError.value = '';
  try {
    const subOrder = await buyerApi.receiveSubOrder(no, key) as SubOrder;
    actionKeys.delete(id);
    if (detail.value) detail.value.subOrders = detail.value.subOrders.map((item) => item.subOrderNo === no ? subOrder : item);
    feedback.value = '已确认签收';
  } catch (reason) {
    actionError.value = formatError(reason, '签收失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function showTraces(no: string) {
  traceSubOrderNo.value = no;
  traces.value = [];
  traceError.value = '';
  traceLoading.value = true;
  const requestId = ++traceRequestId;
  try {
    const result = await buyerApi.logisticsTraces(no) as LogisticsTrace[];
    if (requestId !== traceRequestId || traceSubOrderNo.value !== no) return;
    traces.value = result || [];
  } catch (reason) {
    if (requestId !== traceRequestId || traceSubOrderNo.value !== no) return;
    traces.value = [];
    traceError.value = formatError(reason, '物流轨迹加载失败');
  } finally {
    if (requestId === traceRequestId && traceSubOrderNo.value === no) {
      traceLoading.value = false;
    }
  }
}

async function retryTraces() {
  if (traceSubOrderNo.value) {
    await showTraces(traceSubOrderNo.value);
  }
}

async function createPayment() {
  if (actionLoading.value) return;
  const attempt = paymentAttempt.value;
  actionLoading.value = true;
  actionError.value = '';
  const currentRequestId = ++paymentRequestId;
  try {
    const result = await paymentApi.create(attempt.data, attempt.key) as Payment;
    if (currentRequestId !== paymentRequestId) return;
    payment.value = result;
    paymentQuery.paymentNo = result.paymentNo;
    feedback.value = '支付单已创建，请查询支付结果';
  } catch (reason) {
    if (currentRequestId !== paymentRequestId) return;
    actionError.value = formatError(reason, '支付单创建失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function queryPayment() {
  if (!paymentQuery.paymentNo.trim()) return;
  actionLoading.value = true;
  actionError.value = '';
  const currentRequestId = ++paymentRequestId;
  try {
    const result = await paymentApi.detail(paymentQuery.paymentNo.trim()) as Payment;
    if (currentRequestId !== paymentRequestId) return;
    payment.value = result;
  } catch (reason) {
    if (currentRequestId !== paymentRequestId) return;
    actionError.value = formatError(reason, '支付状态查询失败');
  } finally {
    actionLoading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">订单与物流</h1>
        <p class="market-subtitle">查看订单进度、物流轨迹与签收状态。支付功能目前只接入模拟支付单。</p>
      </div>
      <Button :loading="loading" @click="load">刷新订单</Button>
    </header>

    <section class="market-panel" aria-labelledby="orders-title">
      <div class="orders-head">
        <h2 id="orders-title" class="market-panel-title">我的订单</h2>
        <span v-if="loading" class="orders-count">加载中...</span>
        <span v-else-if="!ordersError" class="orders-count">共 {{ total }} 笔订单</span>
      </div>

      <Alert v-if="detailError" :message="detailError" type="error" show-icon class="orders-notice" />
      <Alert v-if="actionError" :message="actionError" type="error" show-icon class="orders-notice" />
      <Alert v-if="feedback" :message="feedback" type="success" show-icon class="orders-notice" />

      <Alert v-if="ordersError" :message="ordersError" type="error" show-icon class="orders-alert">
        <template #action>
          <Button size="small" danger ghost :loading="loading" @click="load">重试</Button>
        </template>
      </Alert>

      <Table
        v-else
        :class="{ 'compact-table': phoneLayout }"
        :columns="responsiveColumns"
        :data-source="rows"
        :loading="loading"
        :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }"
        :scroll="phoneLayout ? undefined : { x: 840 }"
        row-key="orderNo"
        @change="changePage"
      >
        <template #emptyText>
          <Empty description="暂无订单记录" />
        </template>
        <template #bodyCell="{ column, record }">
          <div v-if="column.key === 'orderNo'" class="mobile-order-cell">
            <span>{{ record.orderNo }}</span>
            <span class="mobile-order-meta">{{ formatOrderStatus(record.status) }} · ¥{{ record.payableAmount }}</span>
          </div>
          <span v-if="column.key === 'createdAt'">{{ formatDate(record.createdAt) }}</span>
          <span v-else-if="column.key === 'payableAmount'">¥{{ record.payableAmount }}</span>
          <Tag v-else-if="column.key === 'status'">{{ formatOrderStatus(record.status) }}</Tag>
          <Space v-else-if="column.key === 'actions'">
            <Button type="link" :loading="detailLoading && loadingOrderNo === record.orderNo" @click="openDetail(record.orderNo)">详情</Button>
            <Popconfirm v-if="record.status === 'PENDING_PAYMENT'" title="确认取消订单？" @confirm="cancel(record.orderNo)">
              <Button danger type="link" :disabled="actionLoading">取消</Button>
            </Popconfirm>
          </Space>
        </template>
      </Table>
    </section>

    <template v-if="detail">
      <section class="market-panel" aria-labelledby="order-detail">
        <div class="detail-head">
          <h2 id="order-detail" class="market-panel-title">订单详情 · {{ selectedOrderNo }}</h2>
          <Button @click="closeDetail">关闭</Button>
        </div>
        <Descriptions :column="{ xs: 1, sm: 2 }" size="small">
          <Descriptions.Item label="当前状态">{{ formatOrderStatus(detail.order.status) }}</Descriptions.Item>
          <Descriptions.Item label="应付金额">¥{{ detail.order.payableAmount }}</Descriptions.Item>
          <Descriptions.Item v-if="detail.order.shippingAddress" label="收货地址">{{ detail.order.shippingAddress }}</Descriptions.Item>
        </Descriptions>
      </section>

      <section class="market-panel" aria-labelledby="sub-orders">
        <h2 id="sub-orders" class="market-panel-title">配送与签收</h2>
        <Table
          :class="{ 'compact-table': phoneLayout }"
          :columns="responsiveSubOrderColumns"
          :data-source="detail.subOrders || []"
          :pagination="false"
          :scroll="phoneLayout ? undefined : { x: 760 }"
          row-key="subOrderNo"
        >
          <template #bodyCell="{ column, record }">
            <div v-if="column.key === 'subOrderNo'" class="mobile-order-cell">
              <span>{{ record.subOrderNo }}</span>
              <span class="mobile-order-meta">{{ formatSubOrderStatus(record.status) }} · {{ record.logisticsCompany || '待发货' }}</span>
            </div>
            <Tag v-if="column.key === 'status'">{{ formatSubOrderStatus(record.status) }}</Tag>
            <span v-else-if="column.key === 'logistics'">{{ record.logisticsCompany ? `${record.logisticsCompany} · ${record.trackingNo || '—'}` : '待发货' }}</span>
            <Space v-else-if="column.key === 'actions'">
              <Button type="link" @click="showTraces(record.subOrderNo)">物流轨迹</Button>
              <Popconfirm v-if="record.status === 'SHIPPED'" title="确认已收到商品？" @confirm="receive(record.subOrderNo)">
                <Button type="link" :disabled="actionLoading">确认签收</Button>
              </Popconfirm>
            </Space>
          </template>
        </Table>

        <div v-if="traceSubOrderNo" class="trace-panel">
          <h3 class="trace-title">{{ traceSubOrderNo }} · 物流轨迹</h3>
          <div v-if="traceLoading" class="trace-loading">
            <Spin />
            <span class="trace-loading-text">物流轨迹加载中...</span>
          </div>
          <Alert v-else-if="traceError" :message="traceError" type="error" show-icon class="trace-alert">
            <template #action>
              <Button size="small" danger ghost :loading="traceLoading" @click="retryTraces">重试</Button>
            </template>
          </Alert>
          <Empty v-else-if="!traces.length" description="暂无物流轨迹" />
          <ol v-else class="trace-list">
            <li v-for="(trace, index) in traces" :key="`${trace.createdAt}-${index}`" class="trace-item">
              <span class="trace-time">{{ formatDate(trace.createdAt) }}</span>
              <strong class="trace-tag">{{ formatTraceStatus(trace.traceStatus) }}</strong>
              <span class="trace-detail">{{ trace.detail }}</span>
            </li>
          </ol>
        </div>
      </section>

      <section class="market-panel" aria-labelledby="order-items">
        <h2 id="order-items" class="market-panel-title">商品明细</h2>
        <Table
          :class="{ 'compact-table': phoneLayout }"
          :columns="responsiveItemColumns"
          :data-source="detail.items || []"
          :pagination="false"
          :scroll="phoneLayout ? undefined : { x: 620 }"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <div v-if="column.key === 'productName'" class="mobile-order-cell">
              <span>{{ record.productName }}</span>
              <span class="mobile-order-meta">×{{ record.quantity }} · ¥{{ record.lineTotal }}</span>
            </div>
            <span v-if="column.key === 'lineTotal'">¥{{ record.lineTotal }}</span>
            <Button v-else-if="column.key === 'actions' && canAfterSale(record.subOrderNo)" type="link" @click="router.push({ path: '/buyer/after-sales', query: { subOrderNo: record.subOrderNo, orderItemId: String(record.id) } })">申请售后</Button>
          </template>
        </Table>
      </section>

      <section class="market-panel" aria-labelledby="payment-title">
        <h2 id="payment-title" class="market-panel-title">支付单</h2>
        <p class="market-note">仅创建和查询模拟支付单，不在买家端提供模拟成功按钮。</p>
        <Form v-if="detail.order.status === 'PENDING_PAYMENT'" :model="payForm" class="payment-form" layout="inline" @finish="createPayment">
          <Form.Item label="支付渠道">模拟支付</Form.Item>
          <Button html-type="submit" type="primary" :loading="actionLoading">创建支付单</Button>
        </Form>
        <Form :model="paymentQuery" class="payment-form" layout="inline" @finish="queryPayment">
          <Form.Item label="支付单号">
            <Input v-model:value="paymentQuery.paymentNo" />
          </Form.Item>
          <Button html-type="submit" :loading="actionLoading">查询结果</Button>
        </Form>
        <Descriptions v-if="payment" :column="1" size="small">
          <Descriptions.Item label="支付单号">{{ payment.paymentNo }}</Descriptions.Item>
          <Descriptions.Item label="状态">{{ formatPaymentStatus(payment.status) }}</Descriptions.Item>
          <Descriptions.Item v-if="payment.amount != null" label="金额">¥{{ payment.amount }}</Descriptions.Item>
        </Descriptions>
      </section>
    </template>
  </main>
</template>

<style scoped src="./buyer-orders.css"></style>
