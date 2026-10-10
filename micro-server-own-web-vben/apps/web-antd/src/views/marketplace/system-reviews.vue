<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useMediaQuery, useWindowSize } from '@vueuse/core';

import { Alert, Button, Empty, Form, Input, InputNumber, Select, Space, Table, Tag } from 'ant-design-vue';
import type { ColumnType } from 'ant-design-vue/es/table';

import { systemApi } from '#/api/marketplace';

interface ProductReviewReport {
  id: number;
  productId: number;
  reason: string;
  reportedAt: string;
  reporterId: number;
  resolutionNote?: null | string;
  resolvedAt?: null | string;
  resolvedBy?: null | number;
  reviewId: number;
  status: string;
}

const listLoading = ref(false);
const resolveLoading = ref(false);
const rows = ref<ProductReviewReport[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const listError = ref<null | string>(null);
const hasLoaded = ref(false);
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const selectedReport = ref<null | ProductReviewReport>(null);
const phoneLayout = useMediaQuery('(max-width: 680px)');
const { width: viewportWidth } = useWindowSize();

const form = reactive({
  note: '',
  reportId: undefined as number | undefined,
  status: 'RESOLVED' as 'DISMISSED' | 'RESOLVED',
});

const desktopColumns: ColumnType[] = [
  { dataIndex: 'id', key: 'id', title: '举报 ID', width: 90 },
  { dataIndex: 'reviewId', key: 'reviewId', title: '评价 ID', width: 100 },
  { dataIndex: 'productId', key: 'productId', title: '商品 ID', width: 100 },
  { dataIndex: 'reporterId', key: 'reporterId', title: '举报人 ID', width: 105 },
  { dataIndex: 'reason', ellipsis: true, key: 'reason', title: '举报原因' },
  { dataIndex: 'status', key: 'status', title: '状态', width: 95 },
  { dataIndex: 'reportedAt', key: 'reportedAt', title: '举报时间', width: 170 },
  { key: 'action', title: '操作', width: 85, fixed: 'right' as const },
];
const columns = computed<ColumnType[]>(() => phoneLayout.value
  ? [
      { dataIndex: 'id', key: 'id', title: '举报信息', width: Math.max(120, viewportWidth.value - 151) },
      { key: 'action', title: '操作', width: 85 },
    ]
  : desktopColumns);

function message(error: unknown, fallback: string): string {
  if (!(error instanceof Error)) return fallback;
  const status = error.message.match(/(?:status code|HTTP)\s*:?\s*(\d{3})/i)?.[1];
  if (!status) return error.message || fallback;
  return Number(status) >= 500
    ? `服务暂时不可用（HTTP ${status}），请稍后重试`
    : `请求失败（HTTP ${status}），请检查操作后重试`;
}

function statusTagColor(status: string) {
  if (status === 'PENDING') return 'warning';
  if (status === 'RESOLVED') return 'success';
  if (status === 'DISMISSED') return 'default';
  return 'blue';
}

function statusLabel(status: string) {
  if (status === 'PENDING') return '待处置';
  if (status === 'RESOLVED') return '已解决';
  if (status === 'DISMISSED') return '已驳回';
  return status;
}

function formatDate(value: unknown): string {
  if (!value) return '-';
  const str = String(value);
  try {
    const d = new Date(str);
    if (!Number.isNaN(d.getTime())) {
      return d.toLocaleString('zh-CN', {
        hour12: false,
        month: '2-digit',
        day: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    }
  } catch {}
  return str;
}

watch(
  () => form.reportId,
  (newId) => {
    selectedReport.value = newId ? rows.value.find((r) => r.id === newId) || null : null;
  },
);

function selectForResolution(record: Record<string, unknown>) {
  const report = rows.value.find((item) => item.id === Number(record.id));
  if (!report) return;
  form.reportId = report.id;
  selectedReport.value = report;
  feedback.value = null;
}

function clearSelected() {
  form.reportId = undefined;
  selectedReport.value = null;
}

function resetResolveForm() {
  Object.assign(form, { note: '', reportId: undefined, status: 'RESOLVED' });
  selectedReport.value = null;
  feedback.value = null;
}

async function load() {
  listLoading.value = true;
  listError.value = null;
  try {
    const result = (await systemApi.reviewReports({
      page: page.value,
      size: pageSize,
      status: 'PENDING',
    })) as {
      items?: ProductReviewReport[];
      total?: number;
    };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
    hasLoaded.value = true;
  } catch (error) {
    rows.value = [];
    total.value = 0;
    if (selectedReport.value) {
      clearSelected();
    }
    listError.value = message(error, '待处置举报加载失败，请刷新重试');
  } finally {
    listLoading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

async function resolve() {
  if (resolveLoading.value || !form.reportId) {
    feedback.value = { text: '请填写待处置的举报 ID', type: 'error' };
    return;
  }
  resolveLoading.value = true;
  feedback.value = null;
  try {
    await systemApi.resolveReviewReport(form.reportId, {
      note: form.note.trim(),
      status: form.status,
    });
    Object.assign(form, { note: '', reportId: undefined, status: 'RESOLVED' });
    selectedReport.value = null;
    feedback.value = { text: '处置结果已提交', type: 'success' };
    await load();
  } catch (error) {
    feedback.value = { text: message(error, '处置提交失败，请稍后重试'), type: 'error' };
  } finally {
    resolveLoading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">SYSTEM · MODERATION</span>
        <div class="head-title-row">
          <h1 class="market-heading">评价举报处置</h1>
          <Tag color="orange" class="status-badge">待办审核</Tag>
        </div>
        <p class="market-subtitle">
          仅处理待办举报；处置状态由服务端校验，处理结果、操作人和时间将写入举报记录。
        </p>
      </div>
      <Space>
        <Button :loading="listLoading" @click="load">刷新待办</Button>
      </Space>
    </header>

    <section class="market-panel" aria-labelledby="report-list-title">
      <div class="panel-header-row">
        <div>
          <h2 id="report-list-title" class="market-panel-title">待处置举报列表</h2>
          <p class="market-note">仅展示待处置的用户举报记录，点击操作列的「处置」可直接载入表单。</p>
        </div>
        <div v-if="listLoading && !hasLoaded" class="total-tag">
          <span>正在加载待办...</span>
        </div>
        <div v-else-if="listError" class="total-tag text-error">
          <span>加载失败</span>
        </div>
        <div v-else-if="hasLoaded" class="total-tag">
          共 <span class="total-num">{{ total }}</span> 条待处置
        </div>
      </div>

      <div v-if="listError" class="list-state-error">
        <Alert :message="listError" type="error" show-icon />
        <div class="state-actions">
          <Button size="small" type="primary" ghost :loading="listLoading" @click="load">
            重试加载
          </Button>
        </div>
      </div>

      <Table
        v-else
        :class="{ 'compact-table': phoneLayout }"
        :columns="columns"
        :data-source="rows"
        :loading="listLoading"
        :pagination="
          hasLoaded && total > 0
            ? {
                current: page + 1,
                pageSize,
                total,
                showSizeChanger: false,
                showTotal: (t: number) => `共 ${t} 条记录`,
              }
            : false
        "
        :scroll="phoneLayout ? undefined : { x: 860 }"
        row-key="id"
        size="middle"
        @change="changePage"
      >
        <template #emptyText>
          <Empty description="当前暂无待处置的评价举报" />
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'id'">
            <span class="mono-id">#{{ record.id }}</span>
            <div v-if="phoneLayout" class="report-mobile-summary">
              <span>评价 #{{ record.reviewId }} · 商品 #{{ record.productId }} · 举报人 #{{ record.reporterId }}</span>
              <span>{{ record.reason || '无原因说明' }}</span>
              <Tag color="warning">待处置</Tag>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <Tag :color="statusTagColor(record.status)">{{ statusLabel(record.status) }}</Tag>
          </template>
          <template v-else-if="column.key === 'reportedAt'">
            <span class="date-text">{{ formatDate(record.reportedAt) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <Button type="link" size="small" class="action-btn" @click="selectForResolution(record)">处置</Button>
          </template>
        </template>
      </Table>
    </section>

    <section class="market-panel" aria-labelledby="report-action-title">
      <h2 id="report-action-title" class="market-panel-title">提交处置结果</h2>

      <div v-if="selectedReport" class="selected-callout">
        <div class="selected-header">
          <span class="selected-title">
            已选定待办举报 <span class="mono-id">#{{ selectedReport.id }}</span>
          </span>
          <Button type="link" size="small" @click="clearSelected">取消选定</Button>
        </div>
        <div class="selected-grid">
          <div class="selected-cell">
            <span class="cell-label">关联评价 ID:</span>
            <span class="cell-val">{{ selectedReport.reviewId }}</span>
          </div>
          <div class="selected-cell">
            <span class="cell-label">关联商品 ID:</span>
            <span class="cell-val">{{ selectedReport.productId }}</span>
          </div>
          <div class="selected-cell">
            <span class="cell-label">举报人 ID:</span>
            <span class="cell-val">{{ selectedReport.reporterId }}</span>
          </div>
          <div class="selected-cell full-width">
            <span class="cell-label">举报原因:</span>
            <span class="cell-val reason-text">{{ selectedReport.reason || '无原因说明' }}</span>
          </div>
        </div>
      </div>

      <Form :disabled="resolveLoading" :model="form" class="resolve-form" layout="vertical" @finish="resolve">
        <div class="form-row">
          <Form.Item label="举报 ID" name="reportId" :rules="[{ required: true, message: '请填写待处置的举报 ID' }]">
            <InputNumber
              v-model:value="form.reportId"
              class="full"
              :min="1"
              :precision="0"
              placeholder="请输入或从表格点击「处置」选择"
            />
          </Form.Item>
          <Form.Item label="处置状态" name="status" :rules="[{ required: true, message: '请选择处置状态' }]">
            <Select
              v-model:value="form.status"
              :options="[
                { label: '已解决', value: 'RESOLVED' },
                { label: '已驳回', value: 'DISMISSED' },
              ]"
            />
          </Form.Item>
        </div>

        <Form.Item label="处置说明 (至多 500 字)">
          <Input.TextArea
            v-model:value="form.note"
            :maxlength="500"
            :rows="3"
            show-count
            placeholder="请输入处置说明（选填，至多 500 字）"
          />
        </Form.Item>

        <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="notice" show-icon closable @close="feedback = null" />

        <Space>
          <Button :loading="resolveLoading" html-type="submit" type="primary">提交处置</Button>
          <Button :disabled="resolveLoading" @click="resetResolveForm">重置</Button>
        </Space>
      </Form>
    </section>
  </main>
</template>

<style scoped>
.head-title-row { display: flex; align-items: center; gap: 10px; }
.status-badge { font-weight: 600; border-radius: 4px; }
.panel-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}
.panel-header-row .market-panel-title { margin-bottom: 4px; }
.total-tag { font-size: 13px; color: var(--market-muted); white-space: nowrap; }
.total-num { font-weight: 600; color: hsl(var(--primary)); }
.text-error { color: #cf1322; }
.list-state-error { padding: 4px 0 16px; display: flex; flex-direction: column; gap: 12px; }
.state-actions { display: flex; justify-content: flex-start; }
.mono-id { font-family: monospace; font-weight: 600; color: var(--market-text); }
.report-mobile-summary { display: grid; gap: 4px; margin-top: 4px; color: var(--market-muted); font-size: 12px; line-height: 1.45; }
.report-mobile-summary .ant-tag { justify-self: start; margin: 0; }
.date-text { font-size: 12.5px; color: var(--market-muted); }
.action-btn { padding: 0; font-weight: 550; }
.resolve-form { max-width: 640px; }
.full { width: 100%; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.selected-callout {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: var(--mall-radius-base);
  background: hsl(var(--accent) / 0.3);
  border: 1px solid hsl(var(--accent-foreground) / 0.18);
  max-width: 640px;
}
.selected-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.selected-title { font-size: 13px; font-weight: 600; color: hsl(var(--accent-foreground)); }
.selected-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 8px 16px;
  font-size: 12.5px;
}
.selected-cell { display: flex; gap: 6px; align-items: center; }
.cell-label { color: var(--market-muted); }
.cell-val { color: var(--market-text); font-weight: 500; }
.full-width { grid-column: 1 / -1; align-items: flex-start; }
.reason-text { word-break: break-word; color: var(--market-text); }
.notice { margin-bottom: 16px; }

@media (max-width: 640px) {
  .panel-header-row { flex-direction: column; align-items: flex-start; gap: 8px; }
  .form-row { grid-template-columns: 1fr; gap: 0; }
}
@media (min-width: 681px) { .report-mobile-summary { display: none; } }
</style>
