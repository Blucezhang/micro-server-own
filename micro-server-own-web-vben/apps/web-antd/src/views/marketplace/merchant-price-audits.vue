<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Alert, Button, Empty, Form, InputNumber, Space, Table } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

export interface ProductPriceAudit {
  id: number;
  productId: number;
  merchantId: number;
  originalPriceBefore: number;
  originalPriceAfter: number;
  promotionPriceBefore?: null | number;
  promotionPriceAfter?: null | number;
  reason: string;
  changedAt: string;
}

const route = useRoute();
const loading = ref(false);
const rows = ref<ProductPriceAudit[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const listError = ref('');
const queryError = ref('');
const hasLoaded = ref(false);
const initialQueryDone = ref(false);
const query = reactive({ productId: Number(route.query.productId) || undefined as number | undefined });

const columns = [
  { key: 'id', title: '审计记录 ID', dataIndex: 'id', width: 110 },
  { key: 'originalPrice', title: '原价变更', width: 180 },
  { key: 'promotionPrice', title: '促销价变更', width: 180 },
  { key: 'reason', title: '变更原因', dataIndex: 'reason', ellipsis: true },
  { key: 'changedAt', title: '变更时间', width: 170 },
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

function formatPrice(val?: null | number): string {
  if (val === null || val === undefined || Number.isNaN(Number(val))) return '未设置';
  return `¥${Number(val).toFixed(2)}`;
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  if (Number.isNaN(d.getTime())) return dateStr;
  const pad = (n: number) => String(n).padStart(2, '0');
  const y = d.getFullYear();
  const m = pad(d.getMonth() + 1);
  const day = pad(d.getDate());
  const h = pad(d.getHours());
  const min = pad(d.getMinutes());
  const s = pad(d.getSeconds());
  return `${y}-${m}-${day} ${h}:${min}:${s}`;
}

async function load() {
  if (!query.productId) return;
  initialQueryDone.value = true;
  loading.value = true;
  listError.value = '';
  try {
    const result = (await merchantApi.priceAudits(query.productId, page.value)) as { items?: ProductPriceAudit[]; total?: number };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
    hasLoaded.value = true;
  } catch (reason) {
    listError.value = humanizeError(reason, '价格审计记录加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function search() {
  queryError.value = '';
  if (!query.productId || !Number.isSafeInteger(query.productId) || query.productId <= 0) {
    queryError.value = '请输入正整数商品 ID';
    return;
  }
  page.value = 0;
  await load();
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

onMounted(() => {
  if (query.productId) void search();
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · AUDIT</span>
        <h1 class="market-heading">商品价格审计</h1>
        <p class="market-subtitle">追踪与核对商品价格的历史变更记录、前后的价差调整与审核原因说明。</p>
      </div>
    </header>

    <section class="market-panel" aria-labelledby="audit-query-title">
      <div class="panel-header">
        <div>
          <h2 id="audit-query-title" class="market-panel-title">查询审计记录</h2>
          <p class="panel-subtitle">输入商品 ID 查看该商品所有的原价与促销价调整时间线</p>
        </div>
      </div>
      <Alert v-if="queryError" :message="queryError" class="notice" show-icon type="error" />
      <Form :disabled="loading" :model="query" class="query-form" layout="inline" @finish="search">
        <Form.Item label="商品 ID" name="productId" :rules="[{ required: true, message: '请填写商品 ID' }]">
          <InputNumber v-model:value="query.productId" :min="1" :precision="0" placeholder="例如：1001" style="width: 200px" />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button :loading="loading" html-type="submit" type="primary">查询审计记录</Button>
            <Button v-if="query.productId" :disabled="loading" @click="search">刷新</Button>
          </Space>
        </Form.Item>
      </Form>
    </section>

    <section class="market-panel" aria-labelledby="audit-list-title">
      <div class="panel-header">
        <div>
          <h2 id="audit-list-title" class="market-panel-title">
            {{ query.productId ? `商品 #${query.productId} 价格变更历史` : '变更历史列表' }}
          </h2>
          <p v-if="initialQueryDone && hasLoaded && !listError" class="panel-subtitle">
            共 {{ total }} 条调价审计记录
          </p>
        </div>
      </div>

      <div v-if="!initialQueryDone" class="empty-wrap">
        <Empty description="请先在上方输入商品 ID 并点击查询" />
      </div>

      <div v-else-if="listError" class="list-state-error">
        <Alert :message="listError" type="error" show-icon />
        <div class="state-actions">
          <Button size="small" type="primary" ghost :loading="loading" @click="load">
            重试加载
          </Button>
        </div>
      </div>

      <Table
        v-else
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false, showTotal: (t) => `共 ${t} 条记录` }"
        :scroll="{ x: 780 }"
        row-key="id"
        @change="changePage"
      >
        <template #emptyText>
          <Empty description="暂无该商品的价格变更审计记录" />
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'id'">
            <span class="mono-code">#{{ record.id }}</span>
          </template>

          <template v-else-if="column.key === 'originalPrice'">
            <div class="price-flow">
              <span class="price-before">{{ formatPrice(record.originalPriceBefore) }}</span>
              <span class="price-arrow">→</span>
              <span class="price-after">{{ formatPrice(record.originalPriceAfter) }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'promotionPrice'">
            <div v-if="record.promotionPriceBefore != null || record.promotionPriceAfter != null" class="price-flow">
              <span class="price-before">{{ formatPrice(record.promotionPriceBefore) }}</span>
              <span class="price-arrow">→</span>
              <span class="price-after">{{ formatPrice(record.promotionPriceAfter) }}</span>
            </div>
            <span v-else class="text-muted">无促销价变动</span>
          </template>

          <template v-else-if="column.key === 'reason'">
            <span :title="record.reason" class="reason-text">{{ record.reason || '未填写原因' }}</span>
          </template>

          <template v-else-if="column.key === 'changedAt'">
            <span class="date-text">{{ formatDate(record.changedAt) }}</span>
          </template>
        </template>
      </Table>
    </section>
  </main>
</template>

<style scoped>
.panel-header {
  margin-bottom: 16px;
}

.panel-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--market-muted);
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.notice {
  margin-bottom: 16px;
}

.empty-wrap {
  padding: 24px 0;
}

.list-state-error {
  padding: 4px 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.state-actions {
  display: flex;
  justify-content: flex-start;
}

.mono-code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  color: var(--market-muted);
}

.price-flow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-variant-numeric: tabular-nums;
}

.price-before {
  color: var(--market-muted, #64748b);
  text-decoration: line-through;
}

.price-arrow {
  color: var(--market-muted);
  font-size: 12px;
}

.price-after {
  font-weight: 600;
  color: var(--market-text);
}

.text-muted {
  color: var(--market-muted);
  font-size: 13px;
}

.reason-text {
  color: var(--market-text);
}

.date-text {
  font-size: 13px;
  color: var(--market-muted);
  white-space: nowrap;
}

@media (max-width: 640px) {
  .query-form {
    flex-direction: column;
    align-items: stretch;
  }

  .query-form :deep(.ant-form-item) {
    margin-inline-end: 0;
    width: 100%;
  }

  .query-form :deep(.ant-input-number) {
    width: 100% !important;
  }
}
</style>
