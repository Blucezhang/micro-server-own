<script lang="ts" setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMediaQuery } from '@vueuse/core';
import { Alert, Button, Empty, Form, InputNumber, Pagination, Space, Table } from 'ant-design-vue';
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
const router = useRouter();
const isMobile = useMediaQuery('(max-width: 768px)');

const loading = ref(false);
const rows = ref<ProductPriceAudit[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const listError = ref('');
const queryError = ref('');
const initialQueryDone = ref(false);

const query = reactive({
  productId: Number(route.query.productId) || undefined as number | undefined,
});

const columns = [
  { key: 'id', title: '审计记录 ID', dataIndex: 'id', width: 120 },
  { key: 'originalPrice', title: '原价变更', width: 190 },
  { key: 'promotionPrice', title: '促销价变更', width: 190 },
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
  if (!dateStr) return '—';
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
    const result = (await merchantApi.priceAudits(query.productId, page.value)) as {
      items?: ProductPriceAudit[];
      total?: number;
    };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
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

async function changePage(pagination: number | { current?: number }) {
  const targetPage = typeof pagination === 'number' ? pagination : (pagination.current || 1);
  page.value = targetPage - 1;
  await load();
}

watch(
  () => route.query.productId,
  (val) => {
    const parsed = Number(val);
    if (parsed && Number.isSafeInteger(parsed) && parsed > 0 && parsed !== query.productId) {
      query.productId = parsed;
      void search();
    }
  },
);

onMounted(() => {
  if (query.productId) void search();
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div class="market-head-text">
        <div class="head-title-row">
          <h1 class="market-heading">商品价格审计</h1>
          <span v-if="initialQueryDone && !listError && total > 0" class="catalog-badge">
            共 {{ total }} 条记录
          </span>
        </div>
        <p class="market-subtitle">追踪与核对商品原价与促销价的历史变更记录、调价幅度与原因追溯。</p>
      </div>
      <div class="market-head-actions">
        <Button @click="router.push('/merchant/products')">返回商品管理</Button>
      </div>
    </header>

    <section class="market-panel audit-panel" aria-label="商品价格审计记录">
      <div class="audit-toolbar">
        <Form :disabled="loading" :model="query" class="query-form" layout="inline" @finish="search">
          <Form.Item label="商品 ID" name="productId" class="query-form-item">
            <InputNumber
              v-model:value="query.productId"
              :min="1"
              :precision="0"
              placeholder="输入商品 ID，例如：1001"
              class="query-input"
            />
          </Form.Item>
          <Form.Item class="query-form-actions">
            <Space wrap>
              <Button :loading="loading" html-type="submit" type="primary">查询审计记录</Button>
              <Button v-if="initialQueryDone && query.productId" :disabled="loading" @click="load">刷新</Button>
            </Space>
          </Form.Item>
        </Form>
      </div>

      <Alert v-if="queryError" :message="queryError" class="query-alert" show-icon type="error" closable @close="queryError = ''" />

      <div v-if="listError" class="catalog-error-row" role="alert">
        <div class="error-main">
          <span class="error-badge">加载失败</span>
          <span class="error-detail">{{ listError }}</span>
        </div>
        <Button :loading="loading" size="small" type="primary" @click="load">重新加载</Button>
      </div>

      <div v-else-if="!initialQueryDone" class="query-guide-strip">
        <div class="guide-content">
          <span class="guide-dot" aria-hidden="true" />
          <span class="guide-text">请输入商品 ID 并点击「查询审计记录」调取历史调价明细。</span>
        </div>
        <Button size="small" type="link" class="guide-link" @click="router.push('/merchant/products')">
          前往商品管理选择商品
        </Button>
      </div>

      <template v-else>
        <div v-if="!isMobile" class="table-container">
          <Table
            :columns="columns"
            :data-source="rows"
            :loading="loading"
            :pagination="total > pageSize ? { current: page + 1, pageSize, total, showSizeChanger: false } : false"
            row-key="id"
            @change="changePage"
          >
            <template #emptyText>
              <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无该商品的价格变更审计记录" />
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
        </div>

        <div v-else class="mobile-card-list">
          <div v-if="loading" class="mobile-loading-tip">正在载入审计记录...</div>
          <div v-else-if="rows.length === 0" class="mobile-empty">
            <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无该商品的价格变更审计记录" />
          </div>
          <article v-for="item in (loading ? [] : rows)" :key="item.id" class="mobile-card">
            <div class="mobile-card-top">
              <span class="mono-code">#{{ item.id }}</span>
              <span class="date-text">{{ formatDate(item.changedAt) }}</span>
            </div>
            <div class="mobile-card-mid">
              <div class="mobile-price-row">
                <span class="price-label">原价变更</span>
                <div class="price-flow">
                  <span class="price-before">{{ formatPrice(item.originalPriceBefore) }}</span>
                  <span class="price-arrow">→</span>
                  <span class="price-after">{{ formatPrice(item.originalPriceAfter) }}</span>
                </div>
              </div>
              <div class="mobile-price-row">
                <span class="price-label">促销价变更</span>
                <div v-if="item.promotionPriceBefore != null || item.promotionPriceAfter != null" class="price-flow">
                  <span class="price-before">{{ formatPrice(item.promotionPriceBefore) }}</span>
                  <span class="price-arrow">→</span>
                  <span class="price-after">{{ formatPrice(item.promotionPriceAfter) }}</span>
                </div>
                <span v-else class="text-muted">无促销变动</span>
              </div>
            </div>
            <div class="mobile-card-bot">
              <span class="reason-label">变更原因</span>
              <span class="reason-text">{{ item.reason || '未填写原因' }}</span>
            </div>
          </article>
          <div v-if="total > pageSize" class="mobile-pagination-wrap">
            <Pagination :current="page + 1" :page-size="pageSize" :total="total" size="small" simple @change="changePage" />
          </div>
        </div>
      </template>
    </section>
  </main>
</template>

<style scoped src="./merchant-price-audits.css"></style>
