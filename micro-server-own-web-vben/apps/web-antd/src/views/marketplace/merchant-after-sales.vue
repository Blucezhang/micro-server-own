<script lang="ts" setup>
import type { AfterSale } from '#/api/marketplace';

import { computed, onMounted, reactive, ref } from 'vue';

import { Alert, Button, Descriptions, Empty, Form, Input, Popconfirm, Select, Space, Table, Tag } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';
import { afterSaleStatusNames, afterSaleTypeNames } from '#/api/marketplace-models';

type Detail = { afterSale: AfterSale; items: { orderItemId: number; quantity: number }[]; events: { eventType: string; createdAt: string }[] };
const filter = reactive({ page: 0, size: 20, status: undefined as AfterSale['status'] | undefined });
const rows = ref<AfterSale[]>([]);
const total = ref(0);
const selected = ref<Detail>();
const loading = ref(false);
const detailLoading = ref(false);
const actionLoading = ref(false);
const error = ref('');
const feedback = ref('');
const auditForm = reactive({ approved: true, remark: '' });
const shipment = reactive({ logisticsCompany: '', trackingNo: '' });
const auditAttempt = computed(() => ({ no: selected.value?.afterSale.afterSaleNo, approved: auditForm.approved, remark: auditForm.remark.trim(), key: crypto.randomUUID() }));
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
    const result = await merchantApi.afterSales(filter);
    rows.value = result.items || []; total.value = Number(result.total || 0);
  } catch (reason) { error.value = errorText(reason, '售后列表加载失败'); }
  finally { loading.value = false; }
}
async function openDetail(no: string) {
  detailLoading.value = true; error.value = ''; feedback.value = '';
  try { selected.value = await merchantApi.afterSaleDetail(no) as Detail; auditForm.remark = ''; }
  catch (reason) { error.value = errorText(reason, '售后详情加载失败'); }
  finally { detailLoading.value = false; }
}
async function audit(approved: boolean) {
  if (!selected.value || actionLoading.value) return;
  auditForm.approved = approved;
  const attempt = auditAttempt.value;
  if (!approved && !attempt.remark) { error.value = '驳回时请填写原因'; return; }
  actionLoading.value = true; error.value = ''; feedback.value = '';
  try {
    applyResult(await merchantApi.auditAfterSale(attempt.no!, { approved, remark: attempt.remark }, attempt.key));
    feedback.value = approved ? '已同意申请' : '已驳回申请';
  } catch (reason) { error.value = errorText(reason, '审核失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function receiveReturn() {
  const no = selected.value?.afterSale.afterSaleNo;
  if (!no || actionLoading.value) return;
  const key = actionKeys.get(no) || crypto.randomUUID();
  actionKeys.set(no, key); actionLoading.value = true; error.value = ''; feedback.value = '';
  try {
    applyResult(await merchantApi.receiveReturn(no, key));
    actionKeys.delete(no); feedback.value = '已确认收到退货';
  } catch (reason) { error.value = errorText(reason, '确认收货失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function shipExchange() {
  if (actionLoading.value) return;
  const attempt = shipmentAttempt.value;
  if (!attempt.no || !attempt.logisticsCompany || !attempt.trackingNo) { error.value = '请填写物流公司和单号'; return; }
  actionLoading.value = true; error.value = ''; feedback.value = '';
  try {
    applyResult(await merchantApi.shipExchange(attempt.no, { logisticsCompany: attempt.logisticsCompany, trackingNo: attempt.trackingNo }, attempt.key));
    Object.assign(shipment, { logisticsCompany: '', trackingNo: '' });
    feedback.value = '换货物流已提交';
  } catch (reason) { error.value = errorText(reason, '换货发货失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function changePage(pagination: { current?: number }) { filter.page = (pagination.current || 1) - 1; await load(); }
async function changeStatus() { filter.page = 0; await load(); }
onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head"><div><span class="market-overline">MERCHANT · 售后</span><h1 class="market-heading">售后处理</h1><p class="market-subtitle">按服务端状态处理审核、退货与换货；每一步只显示当前可执行的操作。</p></div><Button @click="load">刷新列表</Button></header>
    <section class="market-panel" aria-labelledby="after-sales-title">
      <div class="list-head"><h2 id="after-sales-title" class="market-panel-title">售后申请</h2><Select v-model:value="filter.status" allow-clear placeholder="全部状态" class="status-filter" :options="Object.entries(afterSaleStatusNames).map(([value, label]) => ({ value, label }))" @change="changeStatus" /></div>
      <Alert v-if="error && !selected" :message="error" type="error" show-icon class="notice" />
      <Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: filter.page + 1, pageSize: filter.size, total, showSizeChanger: false }" :scroll="{ x: 760 }" row-key="afterSaleNo" @change="changePage">
        <template #bodyCell="{ column, record }">
          <span v-if="column.key === 'type'">{{ afterSaleTypeNames[record.type as AfterSale['type']] || record.type }}</span>
          <Tag v-else-if="column.key === 'status'" color="blue">{{ afterSaleStatusNames[record.status as AfterSale['status']] || record.status }}</Tag>
          <span v-else-if="column.key === 'requestedAmount'">¥{{ record.requestedAmount }}</span>
          <Button v-else-if="column.key === 'actions'" type="link" :loading="detailLoading" @click="openDetail(record.afterSaleNo)">查看处理</Button>
        </template>
      </Table>
    </section>
    <section v-if="selected" class="market-panel" aria-labelledby="after-sale-detail">
      <div class="list-head"><h2 id="after-sale-detail" class="market-panel-title">申请详情 · {{ selected.afterSale.afterSaleNo }}</h2><Button @click="selected = undefined">关闭详情</Button></div>
      <Alert v-if="error" :message="error" type="error" show-icon class="notice" />
      <Alert v-if="feedback" :message="feedback" type="success" show-icon class="notice" />
      <Descriptions :column="{ xs: 1, sm: 2 }" size="small">
        <Descriptions.Item label="子订单">{{ selected.afterSale.subOrderNo }}</Descriptions.Item>
        <Descriptions.Item label="当前状态">{{ afterSaleStatusNames[selected.afterSale.status] }}</Descriptions.Item>
        <Descriptions.Item label="申请类型">{{ afterSaleTypeNames[selected.afterSale.type] }}</Descriptions.Item>
        <Descriptions.Item label="申请金额">¥{{ selected.afterSale.requestedAmount }}</Descriptions.Item>
        <Descriptions.Item label="原因">{{ selected.afterSale.reason }}</Descriptions.Item>
        <Descriptions.Item v-if="selected.afterSale.returnTrackingNo" label="退货物流">{{ selected.afterSale.returnCompany }} · {{ selected.afterSale.returnTrackingNo }}</Descriptions.Item>
      </Descriptions>
      <div v-if="selected.afterSale.status === 'APPLYING'" class="action-panel">
        <h3>审核申请</h3><Input.TextArea v-model:value="auditForm.remark" :maxlength="500" :rows="2" placeholder="审核备注；驳回时必填" show-count />
        <Space class="action-row"><Popconfirm title="同意这条售后申请？" @confirm="audit(true)"><Button type="primary" :loading="actionLoading">同意申请</Button></Popconfirm><Popconfirm title="确认驳回申请？" @confirm="audit(false)"><Button danger :disabled="actionLoading">驳回申请</Button></Popconfirm></Space>
      </div>
      <div v-else-if="selected.afterSale.status === 'RETURNING'" class="action-panel"><h3>确认退货</h3><p class="market-note">请先核对实际退回的商品和物流，再确认收货。</p><Popconfirm title="已核对并收到退货？" @confirm="receiveReturn"><Button type="primary" :loading="actionLoading">确认收到退货</Button></Popconfirm></div>
      <div v-else-if="selected.afterSale.status === 'EXCHANGE_PENDING_SHIPMENT'" class="action-panel"><h3>寄出换货</h3><Form :model="shipment" layout="vertical" @finish="shipExchange"><Form.Item label="物流公司" name="logisticsCompany" :rules="[{ required: true, message: '请输入物流公司' }]"><Input v-model:value="shipment.logisticsCompany" :maxlength="100" /></Form.Item><Form.Item label="物流单号" name="trackingNo" :rules="[{ required: true, message: '请输入物流单号' }]"><Input v-model:value="shipment.trackingNo" :maxlength="100" /></Form.Item><Button html-type="submit" type="primary" :loading="actionLoading">提交换货物流</Button></Form></div>
      <Empty v-else description="当前状态没有待处理操作" />
    </section>
  </main>
</template>

<style scoped>
.list-head { display: flex; align-items: start; justify-content: space-between; gap: 16px; }
.status-filter { width: 190px; }
.notice { margin-bottom: 16px; }
.action-panel { max-width: 620px; margin-top: 22px; padding-top: 20px; border-top: 1px solid var(--market-line); }
.action-panel h3 { margin: 0 0 12px; font-size: 14px; font-weight: 620; }
.action-row { margin-top: 16px; }
@media (max-width: 760px) { .list-head { align-items: stretch; flex-direction: column; } .status-filter { width: 100%; } }
</style>
