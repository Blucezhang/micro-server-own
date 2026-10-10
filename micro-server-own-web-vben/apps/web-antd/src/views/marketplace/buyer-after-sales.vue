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
const listError = ref('');
const feedback = ref('');
const activeTab = ref<'detail' | 'create'>('create');
const form = reactive({ orderItemId: undefined as number | undefined, quantity: 1, reason: '', subOrderNo: '', type: 'REFUND_ONLY' as AfterSale['type'] });
const shipment = reactive({ logisticsCompany: '', trackingNo: '' });
const createAttempt = computed(() => ({ data: { reason: form.reason.trim(), subOrderNo: form.subOrderNo.trim(), type: form.type, items: [{ orderItemId: form.orderItemId, quantity: form.quantity }] }, key: crypto.randomUUID() }));
const shipmentAttempt = computed(() => ({ no: selected.value?.afterSale.afterSaleNo, logisticsCompany: shipment.logisticsCompany.trim(), trackingNo: shipment.trackingNo.trim(), key: crypto.randomUUID() }));
const actionKeys = new Map<string, string>();
const columns = [
  { dataIndex: 'afterSaleNo', key: 'afterSaleNo', title: '售后单号', width: 170 },
  { key: 'type', title: '类型', width: 95 },
  { key: 'status', title: '状态', width: 95 },
  { dataIndex: 'requestedAmount', key: 'requestedAmount', title: '申请金额', width: 100 },
  { key: 'actions', title: '操作', width: 90 },
];

function statusColor(status: string): string {
  switch (status) {
    case 'APPLYING': return 'processing';
    case 'APPROVED': return 'warning';
    case 'EXCHANGE_SHIPPED': return 'cyan';
    case 'CLOSED': return 'default';
    case 'REJECTED': return 'error';
    case 'COMPLETED':
    case 'REFUNDED': return 'success';
    default: return 'default';
  }
}

function errorText(reason: unknown, fallback: string) {
  if (!(reason instanceof Error)) return fallback;
  const status = reason.message.match(/(?:status code|HTTP)\s*:?[ ]*(5\d{2})/i)?.[1];
  return status ? `服务暂时不可用（HTTP ${status}），请稍后重试` : reason.message;
}
function applyResult(sale: AfterSale) {
  if (selected.value) selected.value.afterSale = sale;
  rows.value = rows.value.map((item) => item.afterSaleNo === sale.afterSaleNo ? sale : item);
}
async function load() {
  loading.value = true; listError.value = '';
  try {
    const result = await buyerApi.afterSales({ page: page.value, size: pageSize });
    rows.value = result.items || []; total.value = Number(result.total || 0);
  } catch (reason) { listError.value = errorText(reason, '售后列表加载失败'); }
  finally { loading.value = false; }
}
async function openDetail(no: string) {
  error.value = ''; feedback.value = '';
  activeTab.value = 'detail';
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
    selected.value = { afterSale: sale, events: [] };
    activeTab.value = 'detail';
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
function closeDetail() {
  selected.value = undefined;
  activeTab.value = 'create';
}

watch(() => [route.query.subOrderNo, route.query.orderItemId], ([subOrderNo, orderItemId]) => {
  if (typeof subOrderNo === 'string') form.subOrderNo = subOrderNo;
  const parsed = typeof orderItemId === 'string' ? Number(orderItemId) : NaN;
  if (Number.isSafeInteger(parsed) && parsed > 0) form.orderItemId = parsed;
  if (subOrderNo || (Number.isSafeInteger(parsed) && parsed > 0)) {
    activeTab.value = 'create';
  }
}, { immediate: true });
onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">BUYER · 售后</span>
        <h1 class="market-heading">售后服务</h1>
        <p class="market-subtitle">从已发货或已签收的子订单发起申请；状态变化和退款以订单服务为准。</p>
      </div>
      <div class="head-actions">
        <Button @click="activeTab = 'create'">+ 申请售后</Button>
        <Button :loading="loading" @click="load">刷新列表</Button>
      </div>
    </header>

    <div class="after-sales-layout">
      <!-- Left Column: List -->
      <section class="market-panel list-panel" aria-labelledby="my-after-sales">
        <div class="panel-head">
          <div class="panel-title-group">
            <h2 id="my-after-sales" class="market-panel-title">我的申请</h2>
            <span v-if="!listError && !loading" class="count-badge">{{ total || rows.length }} 笔</span>
          </div>
          <span class="panel-tip">点击「查看进度」查看单据流转</span>
        </div>

        <Alert v-if="listError" :message="listError" type="error" show-icon class="panel-alert" />

        <Table
          :columns="columns"
          :data-source="rows"
          :loading="loading"
          :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }"
          :scroll="{ x: 560 }"
          :row-class-name="(record: AfterSale) => record.afterSaleNo === selected?.afterSale.afterSaleNo ? 'is-selected-row' : ''"
          row-key="afterSaleNo"
          size="middle"
          @change="changePage"
        >
          <template #bodyCell="{ column, record }">
            <span v-if="column.key === 'afterSaleNo'" class="mono-code">{{ record.afterSaleNo }}</span>
            <span v-else-if="column.key === 'type'">{{ afterSaleTypeNames[record.type as AfterSale['type']] || record.type }}</span>
            <Tag v-else-if="column.key === 'status'" :color="statusColor(record.status)">
              {{ afterSaleStatusNames[record.status as AfterSale['status']] || record.status }}
            </Tag>
            <span v-else-if="column.key === 'requestedAmount'" class="amount-value">¥{{ record.requestedAmount }}</span>
            <template v-else-if="column.key === 'actions'">
              <Button
                type="link"
                :class="['action-link', record.afterSaleNo === selected?.afterSale.afterSaleNo && 'is-current']"
                @click="openDetail(record.afterSaleNo)"
              >
                {{ record.afterSaleNo === selected?.afterSale.afterSaleNo ? '查看中' : '查看进度' }}
              </Button>
            </template>
          </template>
          <template #emptyText>
            <div v-if="!listError && !loading" class="table-empty-wrap">
              <Empty description="暂无售后申请" :image="Empty.PRESENTED_IMAGE_SIMPLE">
                <Button type="primary" size="small" @click="activeTab = 'create'">提交新申请</Button>
              </Empty>
            </div>
          </template>
        </Table>
      </section>

      <!-- Right Column: Operational Detail / Create Switcher -->
      <section class="market-panel op-panel">
        <div class="panel-nav" role="group" aria-label="操作视图切换">
          <button
            type="button"
            :aria-pressed="activeTab === 'detail'"
            :class="['nav-tab', activeTab === 'detail' && 'is-active']"
            @click="activeTab = 'detail'"
          >
            <span>处理进度</span>
            <span v-if="selected" class="nav-tag">{{ selected.afterSale.afterSaleNo }}</span>
          </button>
          <button
            type="button"
            :aria-pressed="activeTab === 'create'"
            :class="['nav-tab', activeTab === 'create' && 'is-active']"
            @click="activeTab = 'create'"
          >
            <span>提交申请</span>
          </button>
        </div>

        <Alert v-if="error" :message="error" type="error" show-icon class="panel-alert" />
        <Alert v-if="feedback" :message="feedback" type="success" show-icon class="panel-alert" />

        <!-- Tab 1: Progress & Detail -->
        <div v-if="activeTab === 'detail'" class="tab-content" aria-labelledby="after-sale-detail">
          <div v-if="selected" class="detail-flow">
            <div class="detail-top">
              <div>
                <span class="detail-label">售后单号</span>
                <div class="detail-title-row">
                  <h3 id="after-sale-detail" class="detail-no">{{ selected.afterSale.afterSaleNo }}</h3>
                  <Tag :color="statusColor(selected.afterSale.status)">
                    {{ afterSaleStatusNames[selected.afterSale.status] || selected.afterSale.status }}
                  </Tag>
                </div>
              </div>
              <Button size="small" @click="closeDetail">关闭</Button>
            </div>

            <Descriptions :column="1" size="small" bordered class="detail-descriptions">
              <Descriptions.Item label="售后类型">
                {{ afterSaleTypeNames[selected.afterSale.type] || selected.afterSale.type }}
              </Descriptions.Item>
              <Descriptions.Item label="申请金额">
                <strong class="highlight-amount">¥{{ selected.afterSale.requestedAmount }}</strong>
              </Descriptions.Item>
              <Descriptions.Item label="处理备注">
                {{ selected.afterSale.merchantRemark || '—' }}
              </Descriptions.Item>
              <Descriptions.Item v-if="selected.afterSale.returnTrackingNo" label="退货物流">
                {{ selected.afterSale.returnCompany }} · {{ selected.afterSale.returnTrackingNo }}
              </Descriptions.Item>
            </Descriptions>

            <!-- Actions section -->
            <div v-if="selected.afterSale.status === 'APPROVED'" class="action-card">
              <div class="action-card-head">
                <h4 class="action-card-title">填写退货物流</h4>
                <p class="action-card-subtitle">审核已通过，请寄出商品并填写真实物流信息</p>
              </div>
              <Form :model="shipment" layout="vertical" @finish="returnShipment">
                <Form.Item label="物流公司" name="logisticsCompany" :rules="[{ required: true, message: '请输入物流公司' }]">
                  <Input v-model:value="shipment.logisticsCompany" :maxlength="100" placeholder="例如：顺丰速运 / 中通快递" />
                </Form.Item>
                <Form.Item label="物流单号" name="trackingNo" :rules="[{ required: true, message: '请输入物流单号' }]">
                  <Input v-model:value="shipment.trackingNo" :maxlength="100" placeholder="请输入快递运单号" />
                </Form.Item>
                <Button html-type="submit" type="primary" :loading="actionLoading" block>提交物流信息</Button>
              </Form>
            </div>

            <div v-else-if="selected.afterSale.status === 'APPLYING'" class="action-card action-card-inline">
              <div class="action-card-info">
                <h4 class="action-card-title">撤回申请</h4>
                <p class="action-card-subtitle">当前申请正等待审核，如问题已协商解决可主动撤回</p>
              </div>
              <Popconfirm title="确认撤回这条售后申请？" ok-text="确认撤回" cancel-text="取消" @confirm="act(selected.afterSale.afterSaleNo, 'close')">
                <Button danger :loading="actionLoading">撤回申请</Button>
              </Popconfirm>
            </div>

            <div v-else-if="selected.afterSale.status === 'EXCHANGE_SHIPPED'" class="action-card action-card-inline">
              <div class="action-card-info">
                <h4 class="action-card-title">换货已发出</h4>
                <p class="action-card-subtitle">商家已寄出换货商品，请在确认收货无误后点击完成</p>
              </div>
              <Popconfirm title="确认已收到换货商品？" ok-text="确认收到" cancel-text="取消" @confirm="act(selected.afterSale.afterSaleNo, 'receive')">
                <Button type="primary" :loading="actionLoading">确认收到换货</Button>
              </Popconfirm>
            </div>

            <div v-else class="status-tip-box">
              <span class="status-tip-text">当前状态无需买家操作，系统或商家处理后将自动更新</span>
            </div>

            <!-- Timeline / Events -->
            <div v-if="selected.events?.length" class="events-section">
              <h4 class="events-heading">处理记录</h4>
              <ol class="events-timeline">
                <li v-for="event in selected.events" :key="`${event.eventType}-${event.occurredAt}`" class="timeline-item">
                  <span class="timeline-dot" />
                  <div class="timeline-info">
                    <strong class="timeline-type">{{ event.eventType }}</strong>
                    <time class="timeline-date">{{ new Date(event.occurredAt).toLocaleString('zh-CN') }}</time>
                  </div>
                </li>
              </ol>
            </div>
          </div>

          <!-- Empty selected state -->
          <div v-else class="empty-detail">
            <Empty description="尚未选择售后申请" :image="Empty.PRESENTED_IMAGE_SIMPLE">
              <p class="empty-guide">在左侧列表中点击任意申请的「查看进度」进行查看，或发起新申请。</p>
              <Button type="primary" size="small" @click="activeTab = 'create'">发起售后申请</Button>
            </Empty>
          </div>
        </div>

        <!-- Tab 2: Create New Request Form -->
        <div v-else class="tab-content" aria-labelledby="new-after-sale">
          <div class="create-head">
            <h3 id="new-after-sale" class="create-title">发起新申请</h3>
            <p class="market-note">可从订单明细跳转并自动填入子订单和明细 ID；申请金额由服务端计算。</p>
          </div>
          <Form :model="form" layout="vertical" class="create-form" @finish="submit">
            <Form.Item label="子订单号" name="subOrderNo" :rules="[{ required: true, message: '请输入子订单号' }]">
              <Input v-model:value="form.subOrderNo" placeholder="请输入子订单号" />
            </Form.Item>
            <Form.Item label="售后类型" name="type" :rules="[{ required: true }]">
              <Select v-model:value="form.type" :options="Object.entries(afterSaleTypeNames).map(([value, label]) => ({ value, label }))" />
            </Form.Item>
            <div class="form-row">
              <Form.Item label="订单明细 ID" name="orderItemId" :rules="[{ required: true, type: 'number', min: 1, message: '有效明细 ID' }]" class="form-col">
                <InputNumber v-model:value="form.orderItemId" :min="1" :precision="0" class="full" placeholder="明细 ID" />
              </Form.Item>
              <Form.Item label="数量" name="quantity" :rules="[{ required: true, type: 'number', min: 1, message: '至少 1 件' }]" class="form-col">
                <InputNumber v-model:value="form.quantity" :min="1" :precision="0" class="full" />
              </Form.Item>
            </div>
            <Form.Item label="申请原因" name="reason" :rules="[{ required: true, message: '请填写申请原因' }]">
              <Input.TextArea v-model:value="form.reason" :maxlength="500" :rows="3" show-count placeholder="请详细描述商品问题或售后诉求" />
            </Form.Item>
            <Button html-type="submit" type="primary" :loading="actionLoading" block class="submit-btn">
              提交售后申请
            </Button>
          </Form>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.full { width: 100%; }
.head-actions { display: flex; align-items: center; gap: 10px; }
.after-sales-layout { display: grid; grid-template-columns: minmax(0, 1.35fr) minmax(360px, 1fr); gap: 20px; align-items: start; }
.list-panel, .op-panel { min-width: 0; }
.panel-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; gap: 12px; }
.panel-title-group { display: flex; align-items: center; gap: 8px; }
.panel-title-group .market-panel-title { margin: 0; }
.count-badge { display: inline-block; padding: 1px 8px; font-size: 11px; font-weight: 600; border-radius: 10px; background: hsl(var(--muted)); color: var(--market-muted); }
.panel-tip { font-size: 12px; color: var(--market-muted); }
.panel-alert { margin-bottom: 14px; }
.mono-code { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 12.5px; color: var(--market-text); font-weight: 500; }
.amount-value { font-weight: 600; color: var(--market-text); }
.action-link { padding: 0 4px; font-size: 13px; }
.action-link.is-current { font-weight: 600; }
:deep(.is-selected-row > td) { background-color: hsl(var(--accent) / 0.35) !important; }
:deep(.is-selected-row > td:first-child) { box-shadow: inset 3px 0 0 hsl(var(--ring)); }
.table-empty-wrap { padding: 8px 0; }
.panel-nav { display: flex; align-items: center; gap: 4px; padding: 3px; margin-bottom: 18px; border-radius: var(--mall-radius-base); background: hsl(var(--muted) / 0.6); border: 1px solid var(--market-line); }
.nav-tab { flex: 1; display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 6px 12px; font-size: 13px; font-weight: 550; color: var(--market-muted); background: transparent; border: none; border-radius: calc(var(--mall-radius-base) - 2px); cursor: pointer; transition: all 160ms ease; }
.nav-tab:hover { color: var(--market-text); }
.nav-tab.is-active { color: var(--market-text); background: var(--market-surface); box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06); }
.nav-tag { font-size: 11px; font-family: ui-monospace, SFMono-Regular, monospace; max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; background: hsl(var(--muted)); padding: 0 4px; border-radius: 4px; }
.tab-content { min-width: 0; }
.detail-flow { display: flex; flex-direction: column; gap: 16px; }
.detail-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; padding-bottom: 12px; border-bottom: 1px solid var(--market-line); }
.detail-label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: var(--market-muted); }
.detail-title-row { display: flex; align-items: center; gap: 8px; margin-top: 2px; }
.detail-no { margin: 0; font-size: 16px; font-weight: 650; font-family: ui-monospace, SFMono-Regular, monospace; color: var(--market-text); }
.highlight-amount { color: hsl(var(--accent-foreground)); font-size: 15px; }
.detail-descriptions :deep(.ant-descriptions-row) { border-bottom-color: var(--market-line); }
.action-card { padding: 14px 16px; border-radius: var(--mall-radius-base); border: 1px solid var(--market-line); background: hsl(var(--muted) / 0.35); }
.action-card-head { margin-bottom: 12px; }
.action-card-title { margin: 0 0 2px; font-size: 13.5px; font-weight: 650; color: var(--market-text); }
.action-card-subtitle { margin: 0; font-size: 12px; color: var(--market-muted); line-height: 1.4; }
.action-card-inline { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.status-tip-box { padding: 12px 14px; border-radius: var(--mall-radius-base); background: hsl(var(--muted) / 0.3); border: 1px dashed var(--market-line); text-align: center; }
.status-tip-text { font-size: 12.5px; color: var(--market-muted); }
.events-section { padding-top: 14px; border-top: 1px solid var(--market-line); }
.events-heading { margin: 0 0 10px; font-size: 13px; font-weight: 620; color: var(--market-text); }
.events-timeline { display: flex; flex-direction: column; gap: 10px; margin: 0; padding: 0; list-style: none; }
.timeline-item { display: flex; align-items: flex-start; gap: 10px; font-size: 12px; }
.timeline-dot { width: 6px; height: 6px; border-radius: 50%; background: hsl(var(--ring)); margin-top: 5px; flex-shrink: 0; }
.timeline-info { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 8px; width: 100%; }
.timeline-type { color: var(--market-text); font-weight: 550; }
.timeline-date { color: var(--market-muted); }
.empty-detail { padding: 36px 16px; text-align: center; }
.empty-guide { margin: 4px 0 12px; font-size: 12.5px; color: var(--market-muted); }
.create-head { margin-bottom: 14px; }
.create-title { margin: 0 0 4px; font-size: 15px; font-weight: 650; color: var(--market-text); }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.form-col { margin-bottom: 16px; }
.submit-btn { margin-top: 6px; }
@media (max-width: 1080px) {
  .after-sales-layout { grid-template-columns: 1fr; gap: 16px; }
}
@media (max-width: 640px) {
  .head-actions { width: 100%; }
  .head-actions .ant-btn { flex: 1; }
  .form-row { grid-template-columns: 1fr; gap: 0; }
  .action-card-inline { flex-direction: column; align-items: stretch; }
}
</style>
