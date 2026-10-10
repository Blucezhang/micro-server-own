<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useMediaQuery, useWindowSize } from '@vueuse/core';

import type { ColumnType } from 'ant-design-vue/es/table';
import { Alert, Button, Drawer, Empty, Form, Input, Space, Table, Tag } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

type SubOrder = {
  amount?: number | string;
  createdAt?: number | string;
  status?: string;
  subOrderNo: string;
};

const listLoading = ref(false);
const actionLoading = ref(false);
const rows = ref<SubOrder[]>([]);
const page = ref(0);
const total = ref(0);
const shippingOpen = ref(false);
const compactLayout = useMediaQuery('(max-width: 1000px)');
const phoneLayout = useMediaQuery('(max-width: 680px)');
const { width: viewportWidth } = useWindowSize();
const pageSize = 20;
const form = reactive({ logisticsCompany: '', subOrderNo: '', trackingNo: '' });
const actionFeedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const drawerFeedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const listError = ref('');

const columns: ColumnType[] = [
  { dataIndex: 'subOrderNo', key: 'subOrderNo', title: '子订单号', width: 260 },
  { dataIndex: 'status', key: 'status', title: '履约状态', width: 120 },
  { dataIndex: 'amount', key: 'amount', title: '订单金额', width: 150 },
  { key: 'createdAt', title: '创建时间', width: 210 },
  { key: 'actions', title: '操作', width: 118, fixed: 'right' as const },
];
const responsiveColumns = computed<ColumnType[]>(() => phoneLayout.value
  ? [
      { dataIndex: 'subOrderNo', key: 'subOrderNo', title: '履约订单', width: Math.max(120, viewportWidth.value - 184) },
      { key: 'actions', title: '操作', width: 118 },
    ]
  : columns);

function message(error: unknown, fallback: string) {
  if (!(error instanceof Error) || /^request failed with status code \d{3}$/i.test(error.message)) {
    return fallback;
  }
  return error.message;
}

function formatDate(value?: number | string) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

function formatAmount(value?: number | string) {
  if (value === undefined || value === null || value === '') return '—';
  const amount = Number(value);
  return Number.isFinite(amount) ? `¥${amount.toFixed(2)}` : '—';
}

function openShipping(subOrderNo: string) {
  Object.assign(form, { logisticsCompany: '', subOrderNo, trackingNo: '' });
  drawerFeedback.value = null;
  shippingOpen.value = true;
}

function closeShipping() {
  shippingOpen.value = false;
  drawerFeedback.value = null;
  Object.assign(form, { logisticsCompany: '', subOrderNo: '', trackingNo: '' });
}

async function load(clearActionFeedback = false) {
  listLoading.value = true;
  listError.value = '';
  if (clearActionFeedback) actionFeedback.value = null;
  try {
    const result = (await merchantApi.subOrders({ page: page.value, size: pageSize, status: 'TO_SHIP' })) as {
      items?: SubOrder[];
      total?: number;
    };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (error) {
    listError.value = message(error, '待发货订单加载失败，请重试');
  } finally {
    listLoading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

async function ship() {
  if (actionLoading.value) return;
  const subOrderNo = form.subOrderNo.trim();
  const logisticsCompany = form.logisticsCompany.trim();
  const trackingNo = form.trackingNo.trim();
  drawerFeedback.value = null;
  if (!subOrderNo || !logisticsCompany || !trackingNo) {
    drawerFeedback.value = { text: '请填写子订单号、物流公司和物流单号', type: 'error' };
    return;
  }
  actionLoading.value = true;
  try {
    await merchantApi.ship(subOrderNo, { logisticsCompany, trackingNo });
    closeShipping();
    actionFeedback.value = { text: '发货信息已提交，订单状态由服务端更新。', type: 'success' };
    await load();
  } catch (error) {
    drawerFeedback.value = { text: message(error, '发货失败，请稍后重试'), type: 'error' };
  } finally {
    actionLoading.value = false;
  }
}

onMounted(() => load());
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · FULFILLMENT</span>
        <h1 class="market-heading">发货履约</h1>
        <p class="market-subtitle">仅展示待发货子订单。录入物流信息后，订单服务负责状态流转与追踪记录。</p>
      </div>
      <Button :loading="listLoading" @click="load(true)">刷新订单</Button>
    </header>

    <Alert v-if="actionFeedback" :message="actionFeedback.text" :type="actionFeedback.type" closable show-icon @close="actionFeedback = null" />

    <section class="market-panel fulfillment-panel" aria-labelledby="to-ship-title">
      <div class="list-heading">
        <div>
          <h2 id="to-ship-title" class="market-panel-title">待处理订单</h2>
          <p class="market-note">
            <template v-if="listError">订单加载受阻 · 请点击重试</template>
            <template v-else-if="listLoading && rows.length === 0">正在载入待处理订单...</template>
            <template v-else>按订单录入物流信息，发货后状态由订单服务自动流转。</template>
          </p>
        </div>
        <Tag v-if="!listError" class="queue-count">{{ total }} 待发货</Tag>
        <Tag v-else color="error" class="queue-status-tag">加载失败</Tag>
      </div>

      <div v-if="listError" class="state-box state-error">
        <div class="state-info">
          <span class="state-title">待发货订单加载失败</span>
          <span class="state-desc">{{ listError }}</span>
        </div>
        <Button :loading="listLoading" size="small" type="primary" ghost @click="load(true)">重新加载</Button>
      </div>

      <Table
        v-else
        :class="{ 'compact-table': phoneLayout }"
        :columns="responsiveColumns"
        :data-source="rows"
        :loading="listLoading"
        :pagination="total > 0 ? { current: page + 1, pageSize, total, showSizeChanger: false } : false"
        :scroll="phoneLayout ? undefined : compactLayout ? { x: 880 } : undefined"
        row-key="subOrderNo"
        @change="changePage"
      >
        <template #emptyText>
          <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无待发货订单" />
        </template>
        <template #bodyCell="{ column, record }">
          <div v-if="column.key === 'subOrderNo'" class="mobile-order-cell">
            <span>{{ record.subOrderNo }}</span>
            <span class="mobile-order-meta">待发货 · {{ formatAmount(record.amount) }}</span>
          </div>
          <template v-if="column.key === 'createdAt'">{{ formatDate(record.createdAt) }}</template>
          <span v-else-if="column.key === 'amount'" class="order-amount">{{ formatAmount(record.amount) }}</span>
          <Tag v-else-if="column.key === 'status'" color="gold">待发货</Tag>
          <Button v-else-if="column.key === 'actions'" size="small" type="primary" @click="openShipping(String(record.subOrderNo))">录入物流</Button>
        </template>
      </Table>
    </section>

    <Drawer v-model:open="shippingOpen" :closable="false" :width="520" placement="right" @close="closeShipping">
      <template #title>
        <div class="editor-heading">
          <span class="market-overline">MERCHANT · SHIPPING</span>
          <h2>录入物流</h2>
          <p>提交后将写入该子订单的发货记录，请核对物流单号。</p>
        </div>
      </template>
      <Form :disabled="actionLoading" :model="form" class="shipment-form" layout="vertical" @finish="ship">
        <Form.Item label="子订单号" name="subOrderNo">
          <Input v-model:value="form.subOrderNo" disabled />
        </Form.Item>
        <Form.Item label="物流公司" name="logisticsCompany" :rules="[{ required: true, message: '请填写物流公司' }]">
          <Input v-model:value="form.logisticsCompany" placeholder="例如：顺丰速运" />
        </Form.Item>
        <Form.Item label="物流单号" name="trackingNo" :rules="[{ required: true, message: '请填写物流单号' }]">
          <Input v-model:value="form.trackingNo" placeholder="请输入物流面单号" />
        </Form.Item>
        <Alert v-if="drawerFeedback?.type === 'error'" :message="drawerFeedback.text" class="notice" show-icon type="error" />
        <div class="editor-footer">
          <Space>
            <Button :disabled="actionLoading" @click="closeShipping">取消</Button>
            <Button :loading="actionLoading" html-type="submit" type="primary">确认发货</Button>
          </Space>
        </div>
      </Form>
    </Drawer>
  </main>
</template>

<style scoped>
.shipment-form { max-width: 680px; }
.notice { margin-bottom: 16px; }
.fulfillment-panel { padding-bottom: 10px; }
.list-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.list-heading .market-panel-title { margin-bottom: 4px; }
.queue-count { margin: 0; padding: 5px 10px; border: 0; border-radius: 999px; background: hsl(42 88% 92%); color: hsl(31 75% 34%); font-weight: 600; }
.queue-status-tag { margin: 0; font-weight: 600; }
.state-box { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 16px 18px; border-radius: var(--mall-radius-base, 8px); border: 1px solid hsl(var(--border)); background: hsl(var(--muted) / 0.4); }
.state-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.state-title { font-size: 13.5px; font-weight: 600; color: hsl(var(--foreground)); }
.state-desc { font-size: 12.5px; color: hsl(var(--muted-foreground)); word-break: break-word; }
.order-amount { color: hsl(var(--foreground)); font-weight: 650; font-variant-numeric: tabular-nums; }
.mobile-order-cell { display: grid; gap: 4px; min-width: 0; }
.mobile-order-meta { color: hsl(var(--muted-foreground)); font-size: 12px; }
.editor-heading { padding: 2px 0 4px; }
.editor-heading h2 { margin: 6px 0 5px; color: hsl(var(--foreground)); font-size: 20px; font-weight: 650; }
.editor-heading p { margin: 0; color: hsl(var(--muted-foreground)); font-size: 13px; font-weight: 400; line-height: 1.6; }
.editor-footer { position: sticky; bottom: -24px; display: flex; justify-content: flex-end; margin: 28px -24px -24px; padding: 16px 24px; border-top: 1px solid hsl(var(--border)); background: hsl(var(--card)); }
@media (min-width: 681px) { .mobile-order-meta { display: none; } }
@media (max-width: 480px) {
  .list-heading { flex-direction: column; align-items: flex-start; gap: 8px; }
  .state-box { flex-direction: column; align-items: flex-start; gap: 10px; }
}
</style>
