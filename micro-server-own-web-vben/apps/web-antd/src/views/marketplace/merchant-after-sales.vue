<script lang="ts" setup>
import type { AfterSale } from '#/api/marketplace';

import { computed, onMounted, reactive, ref } from 'vue';

import { Alert, Button, Descriptions, Empty, Form, Input, Popconfirm, Select, Space, Table, Tag } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';
import { afterSaleStatusNames, afterSaleTypeNames } from '#/api/marketplace-models';

type Detail = {
  afterSale: AfterSale;
  items: { orderItemId: number; quantity: number }[];
  events: { eventType: string; createdAt: string }[];
};

const filter = reactive({
  page: 0,
  size: 20,
  status: undefined as AfterSale['status'] | undefined,
});

const rows = ref<AfterSale[]>([]);
const total = ref(0);
const selected = ref<Detail>();
const listLoading = ref(false);
const detailLoading = ref(false);
const actionLoading = ref(false);
const listError = ref('');
const actionError = ref('');
const feedback = ref('');
const auditForm = reactive({ approved: true, remark: '' });
const shipment = reactive({ logisticsCompany: '', trackingNo: '' });

const auditAttempt = computed(() => ({
  no: selected.value?.afterSale.afterSaleNo,
  approved: auditForm.approved,
  remark: auditForm.remark.trim(),
  key: crypto.randomUUID(),
}));

const shipmentAttempt = computed(() => ({
  no: selected.value?.afterSale.afterSaleNo,
  logisticsCompany: shipment.logisticsCompany.trim(),
  trackingNo: shipment.trackingNo.trim(),
  key: crypto.randomUUID(),
}));

const actionKeys = new Map<string, string>();

const columns = [
  { key: 'afterSaleNo', dataIndex: 'afterSaleNo', title: '售后单号', width: 170 },
  { key: 'type', title: '申请类型', width: 110 },
  { key: 'status', title: '当前状态', width: 130 },
  { key: 'requestedAmount', dataIndex: 'requestedAmount', title: '申请退款金额', width: 140 },
  { key: 'reason', dataIndex: 'reason', title: '申请原因', ellipsis: true },
  { key: 'actions', title: '操作', width: 110, align: 'center' as const },
];

function humanizeError(reason: unknown, fallback: string): string {
  if (reason instanceof Error) {
    const msg = reason.message || '';
    if (msg.includes('502') || msg.includes('Bad Gateway')) {
      return '网关服务暂时不可用 (502)，请稍后重试';
    }
    if (msg.includes('504') || msg.includes('Gateway Timeout')) {
      return '网关响应超时 (504)，请稍后重试';
    }
    if (msg.includes('Network Error') || msg.includes('Failed to fetch')) {
      return '网络连接异常，请检查网络后重试';
    }
    return msg;
  }
  return fallback;
}

function statusColor(status?: AfterSale['status']): string {
  switch (status) {
    case 'APPLYING':
      return 'processing';
    case 'APPROVED':
    case 'RETURNING':
    case 'EXCHANGE_PENDING_SHIPMENT':
      return 'warning';
    case 'REJECTED':
    case 'CLOSED':
      return 'default';
    default:
      return 'blue';
  }
}

function applyResult(sale: AfterSale) {
  if (selected.value) selected.value.afterSale = sale;
  rows.value = rows.value.map((item) => (item.afterSaleNo === sale.afterSaleNo ? sale : item));
}

async function load() {
  listLoading.value = true;
  listError.value = '';
  try {
    const result = await merchantApi.afterSales(filter);
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (reason) {
    listError.value = humanizeError(reason, '售后工单加载失败，请稍后重试');
  } finally {
    listLoading.value = false;
  }
}

async function openDetail(no: string) {
  detailLoading.value = true;
  actionError.value = '';
  feedback.value = '';
  try {
    selected.value = (await merchantApi.afterSaleDetail(no)) as Detail;
    auditForm.remark = '';
  } catch (reason) {
    actionError.value = humanizeError(reason, '售后详情加载失败，请稍后重试');
  } finally {
    detailLoading.value = false;
  }
}

async function audit(approved: boolean) {
  if (!selected.value || actionLoading.value) return;
  auditForm.approved = approved;
  const attempt = auditAttempt.value;
  if (!approved && !attempt.remark) {
    actionError.value = '驳回时请填写原因备注';
    return;
  }
  actionLoading.value = true;
  actionError.value = '';
  feedback.value = '';
  try {
    applyResult(await merchantApi.auditAfterSale(attempt.no!, { approved, remark: attempt.remark }, attempt.key));
    feedback.value = approved ? '已同意售后申请' : '已驳回售后申请';
  } catch (reason) {
    actionError.value = humanizeError(reason, '审核操作失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function receiveReturn() {
  const no = selected.value?.afterSale.afterSaleNo;
  if (!no || actionLoading.value) return;
  const key = actionKeys.get(no) || crypto.randomUUID();
  actionKeys.set(no, key);
  actionLoading.value = true;
  actionError.value = '';
  feedback.value = '';
  try {
    applyResult(await merchantApi.receiveReturn(no, key));
    actionKeys.delete(no);
    feedback.value = '已确认收到退货包裹';
  } catch (reason) {
    actionError.value = humanizeError(reason, '确认收货失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function shipExchange() {
  if (actionLoading.value) return;
  const attempt = shipmentAttempt.value;
  if (!attempt.no || !attempt.logisticsCompany || !attempt.trackingNo) {
    actionError.value = '请完整填写换货物流公司与运单号';
    return;
  }
  actionLoading.value = true;
  actionError.value = '';
  feedback.value = '';
  try {
    applyResult(
      await merchantApi.shipExchange(
        attempt.no,
        { logisticsCompany: attempt.logisticsCompany, trackingNo: attempt.trackingNo },
        attempt.key,
      ),
    );
    Object.assign(shipment, { logisticsCompany: '', trackingNo: '' });
    feedback.value = '换货物流信息已提交';
  } catch (reason) {
    actionError.value = humanizeError(reason, '换货发货提交失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  filter.page = (pagination.current || 1) - 1;
  await load();
}

async function changeStatus() {
  filter.page = 0;
  await load();
}

onMounted(() => load());
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · 售后</span>
        <h1 class="market-heading">售后与客诉处理</h1>
        <p class="market-subtitle">实时处理退款、退货退款与换货业务流；遵循状态机仅展示当前可操作项。</p>
      </div>
      <Button :loading="listLoading" @click="load">刷新列表</Button>
    </header>

    <section class="market-panel" aria-labelledby="after-sales-title">
      <div class="list-head">
        <div>
          <h2 id="after-sales-title" class="market-panel-title">售后工单列表</h2>
          <p class="panel-subtitle">
            <template v-if="listError">工单列表加载受阻 · 请点击重试</template>
            <template v-else-if="listLoading && rows.length === 0">正在同步售后工单...</template>
            <template v-else>支持按工单流转状态快速筛选与处理</template>
          </p>
        </div>
        <div class="filter-actions">
          <Select
            v-model:value="filter.status"
            allow-clear
            placeholder="全部流转状态"
            class="status-filter"
            :options="Object.entries(afterSaleStatusNames).map(([value, label]) => ({ value, label }))"
            @change="changeStatus"
          />
        </div>
      </div>

      <Alert v-if="actionError && !selected" :message="actionError" type="error" show-icon class="notice" closable @close="actionError = ''" />

      <div v-if="listError" class="state-box state-error">
        <div class="state-info">
          <span class="state-title">售后工单加载失败</span>
          <span class="state-desc">{{ listError }}</span>
        </div>
        <Button :loading="listLoading" size="small" type="primary" ghost @click="load">点击重试</Button>
      </div>

      <Table
        v-else
        :columns="columns"
        :data-source="rows"
        :loading="listLoading"
        :pagination="total > 0 ? { current: filter.page + 1, pageSize: filter.size, total, showSizeChanger: false, showTotal: (t) => `共 ${t} 单` } : false"
        :scroll="{ x: 840 }"
        row-key="afterSaleNo"
        @change="changePage"
      >
        <template #emptyText>
          <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无符合条件的售后申请单" />
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'afterSaleNo'">
            <span class="mono-code">{{ record.afterSaleNo }}</span>
          </template>

          <template v-else-if="column.key === 'type'">
            <Tag color="cyan">{{ afterSaleTypeNames[record.type as AfterSale['type']] || record.type }}</Tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <Tag :color="statusColor(record.status as AfterSale['status'])">
              {{ afterSaleStatusNames[record.status as AfterSale['status']] || record.status }}
            </Tag>
          </template>

          <template v-else-if="column.key === 'requestedAmount'">
            <span class="amount-cell">¥{{ Number(record.requestedAmount).toFixed(2) }}</span>
          </template>

          <template v-else-if="column.key === 'reason'">
            <span :title="record.reason" class="reason-text">{{ record.reason || '无原因说明' }}</span>
          </template>

          <template v-else-if="column.key === 'actions'">
            <Button type="link" size="small" :loading="detailLoading" @click="openDetail(record.afterSaleNo)">
              查看处理
            </Button>
          </template>
        </template>
      </Table>
    </section>

    <section v-if="selected" class="market-panel detail-panel" aria-labelledby="after-sale-detail">
      <div class="list-head">
        <div>
          <h2 id="after-sale-detail" class="market-panel-title">工单详情 · {{ selected.afterSale.afterSaleNo }}</h2>
          <p class="panel-subtitle">审核申请、确认用户退货寄件及发货换货跟踪</p>
        </div>
        <Button @click="selected = undefined">收起详情</Button>
      </div>

      <Alert v-if="actionError" :message="actionError" type="error" show-icon class="notice" closable @close="actionError = ''" />
      <Alert v-if="feedback" :message="feedback" type="success" show-icon class="notice" closable @close="feedback = ''" />

      <Descriptions :column="{ xs: 1, sm: 2, md: 3 }" bordered size="small" class="detail-descriptions">
        <Descriptions.Item label="售后工单号">
          <span class="mono-code">{{ selected.afterSale.afterSaleNo }}</span>
        </Descriptions.Item>
        <Descriptions.Item label="关联合同/子订单">
          <span class="mono-code">{{ selected.afterSale.subOrderNo }}</span>
        </Descriptions.Item>
        <Descriptions.Item label="当前工单状态">
          <Tag :color="statusColor(selected.afterSale.status)">
            {{ afterSaleStatusNames[selected.afterSale.status] }}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item label="申请类型">
          <Tag color="cyan">{{ afterSaleTypeNames[selected.afterSale.type] }}</Tag>
        </Descriptions.Item>
        <Descriptions.Item label="申请金额">
          <span class="amount-cell">¥{{ Number(selected.afterSale.requestedAmount).toFixed(2) }}</span>
        </Descriptions.Item>
        <Descriptions.Item label="申请原因说明" :span="2">
          {{ selected.afterSale.reason || '未填写具体原因' }}
        </Descriptions.Item>
        <Descriptions.Item v-if="selected.afterSale.returnTrackingNo" label="买家退货物流" :span="3">
          {{ selected.afterSale.returnCompany }} · <span class="mono-code">{{ selected.afterSale.returnTrackingNo }}</span>
        </Descriptions.Item>
      </Descriptions>

      <div v-if="selected.afterSale.status === 'APPLYING'" class="action-panel">
        <h3 class="action-heading">初审售后申请</h3>
        <p class="market-note">请核实买家反馈的原因与诉求，选择同意或驳回本次售后申请。</p>
        <Input.TextArea
          v-model:value="auditForm.remark"
          :maxlength="500"
          :rows="2"
          placeholder="填写审核备注信息；驳回时必须填写具体驳回理由"
          show-count
          class="audit-input"
        />
        <Space class="action-row">
          <Popconfirm title="确认同意此售后申请？" ok-text="同意" cancel-text="取消" @confirm="audit(true)">
            <Button type="primary" :loading="actionLoading">同意申请</Button>
          </Popconfirm>
          <Popconfirm title="确认驳回此售后申请？" ok-text="驳回" cancel-text="取消" @confirm="audit(false)">
            <Button danger :disabled="actionLoading">驳回申请</Button>
          </Popconfirm>
        </Space>
      </div>

      <div v-else-if="selected.afterSale.status === 'RETURNING'" class="action-panel">
        <h3 class="action-heading">确认退货包裹</h3>
        <p class="market-note">请确认仓库已实际清点核对退回商品完好后，再点击确认收货。</p>
        <Popconfirm title="确认已核对并收到退货商品？" ok-text="确认收货" cancel-text="取消" @confirm="receiveReturn">
          <Button type="primary" :loading="actionLoading">确认收到退货</Button>
        </Popconfirm>
      </div>

      <div v-else-if="selected.afterSale.status === 'EXCHANGE_PENDING_SHIPMENT'" class="action-panel">
        <h3 class="action-heading">寄出换货商品</h3>
        <p class="market-note">录入换货发货的物流承运商与运单编号，通知买家查收。</p>
        <Form :model="shipment" layout="vertical" class="shipment-form" @finish="shipExchange">
          <Form.Item label="物流承运商" name="logisticsCompany" :rules="[{ required: true, message: '请输入物流承运商' }]">
            <Input v-model:value="shipment.logisticsCompany" :maxlength="100" placeholder="例如：顺丰速运 / 中通快递" />
          </Form.Item>
          <Form.Item label="运单编号" name="trackingNo" :rules="[{ required: true, message: '请输入运单编号' }]">
            <Input v-model:value="shipment.trackingNo" :maxlength="100" placeholder="例如：SF123456789" />
          </Form.Item>
          <Button html-type="submit" type="primary" :loading="actionLoading">提交换货物流</Button>
        </Form>
      </div>

      <div v-else class="action-panel-empty">
        <Empty description="当前工单状态下无待处理操作" />
      </div>
    </section>
  </main>
</template>

<style scoped>
.list-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.panel-subtitle { margin: 4px 0 0; font-size: 13px; color: var(--market-muted); }
.filter-actions { display: flex; align-items: center; }
.status-filter { width: 190px; }
.notice { margin-bottom: 16px; }
.mono-code { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 12px; color: var(--market-muted); }
.amount-cell { font-weight: 600; color: var(--market-text); font-variant-numeric: tabular-nums; }
.reason-text { color: var(--market-text); }
.detail-panel { margin-top: 16px; }
.detail-descriptions { margin-bottom: 16px; }
.action-panel { max-width: 640px; margin-top: 20px; padding-top: 20px; border-top: 1px solid var(--market-line); }
.action-heading { margin: 0 0 6px; font-size: 15px; font-weight: 600; color: var(--market-text); }
.market-note { margin: 0 0 12px; font-size: 13px; color: var(--market-muted); }
.audit-input { margin-bottom: 12px; }
.action-row { margin-top: 4px; }
.shipment-form { max-width: 480px; }
.action-panel-empty { margin-top: 24px; padding: 16px 0; }
.state-box { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 16px 18px; border-radius: var(--mall-radius-base, 8px); border: 1px solid hsl(var(--border)); background: hsl(var(--muted) / 0.4); }
.state-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.state-title { font-size: 13.5px; font-weight: 600; color: hsl(var(--foreground)); }
.state-desc { font-size: 12.5px; color: hsl(var(--muted-foreground)); word-break: break-word; }
@media (max-width: 760px) {
  .list-head { align-items: stretch; flex-direction: column; }
  .status-filter { width: 100%; }
}
@media (max-width: 480px) {
  .state-box { flex-direction: column; align-items: flex-start; gap: 10px; }
}
</style>
