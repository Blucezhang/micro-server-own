<script lang="ts" setup>
import type { ProductReview } from '#/api/marketplace';

import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue';
import { useMediaQuery } from '@vueuse/core';
import { useRoute, useRouter } from 'vue-router';

import { Alert, Button, Empty, Form, Input, InputNumber, Pagination, Rate, Space, Table, Tag } from 'ant-design-vue';

import { catalogApi, merchantApi } from '#/api/marketplace';

const route = useRoute();
const router = useRouter();
const compactLayout = useMediaQuery('(max-width: 1100px)');
const productId = ref<number>();
const currentProductId = ref<number>();
const rows = ref<ProductReview[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 10;
const loading = ref(false);
const replyLoading = ref(false);
const replyPanel = ref<HTMLElement>();
const listError = ref('');
const queryError = ref('');
const replyError = ref('');
const feedback = ref('');
const hasLoaded = ref(false);
const replyForm = reactive({ content: '', reviewId: undefined as number | undefined });
const replyAttempt = computed(() => ({
  content: replyForm.content.trim(),
  reviewId: replyForm.reviewId,
  key: crypto.randomUUID(),
}));

const columns = [
  { key: 'rating', dataIndex: 'rating', title: '评分', width: 140 },
  { key: 'content', dataIndex: 'content', title: '评价内容', ellipsis: false },
  { key: 'merchantReply', dataIndex: 'merchantReply', title: '商家回复', width: 300 },
  { key: 'createdAt', dataIndex: 'createdAt', title: '评价时间', width: 160 },
  { key: 'actions', title: '操作', width: 100, align: 'center' as const },
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
    if (msg.includes('503')) {
      return '评价服务暂时不可用 (503)，请稍后重试';
    }
    if (/timeout/i.test(msg)) {
      return '评价请求超时，请稍后重试';
    }
    if (msg.includes('Network Error') || msg.includes('Failed to fetch')) {
      return '网络连接异常，请检查网络后重试';
    }
    return msg;
  }
  return fallback;
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
  return `${y}-${m}-${day} ${h}:${min}`;
}

async function load(nextPage = 0) {
  const id = currentProductId.value;
  if (!id) return;
  loading.value = true;
  listError.value = '';
  try {
    const result = await catalogApi.reviews(id, nextPage);
    if (currentProductId.value !== id) return;
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
    page.value = nextPage;
    hasLoaded.value = true;
  } catch (reason) {
    if (currentProductId.value !== id) return;
    listError.value = humanizeError(reason, '评价列表加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function selectProduct() {
  queryError.value = '';
  if (!Number.isSafeInteger(productId.value) || !productId.value || productId.value <= 0) {
    queryError.value = '请输入正整数商品 ID';
    return;
  }
  currentProductId.value = productId.value;
  rows.value = [];
  total.value = 0;
  hasLoaded.value = false;
  listError.value = '';
  replyError.value = '';
  replyForm.reviewId = undefined;
  await router.replace({ query: { productId: String(productId.value) } });
  await load();
}

async function beginReply(review: ProductReview) {
  replyForm.reviewId = review.id;
  replyForm.content = review.merchantReply || '';
  replyError.value = '';
  feedback.value = '';
  await nextTick();
  replyPanel.value?.scrollIntoView({ block: 'start' });
}

async function submitReply() {
  const attempt = replyAttempt.value;
  if (replyLoading.value || !attempt.reviewId) return;
  if (!attempt.content || attempt.content.length > 500) {
    replyError.value = '回复内容需要 1–500 个字符';
    return;
  }
  replyLoading.value = true;
  replyError.value = '';
  feedback.value = '';
  try {
    await merchantApi.replyReview(attempt.reviewId, attempt.content, attempt.key);
    replyForm.reviewId = undefined;
    replyForm.content = '';
    feedback.value = '回复已成功保存并同步给用户';
    await load(page.value);
  } catch (reason) {
    replyError.value = humanizeError(reason, '回复提交失败，请重试');
  } finally {
    replyLoading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  await load((pagination.current || 1) - 1);
}

watch(
  () => route.query.productId,
  (value) => {
    const parsed = typeof value === 'string' && /^[1-9]\d*$/.test(value) ? Number(value) : undefined;
    if (!parsed || !Number.isSafeInteger(parsed) || parsed === currentProductId.value) return;
    productId.value = parsed;
    currentProductId.value = parsed;
    hasLoaded.value = false;
    listError.value = '';
    load();
  },
  { immediate: true },
);

onMounted(() => {
  if (!currentProductId.value) {
    feedback.value = '请输入商品 ID 查询评价，或从商品列表快捷跳转。';
  }
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">商品评价管理</h1>
        <p class="market-subtitle">查看买家评分与反馈，处理尚未回复的评价。</p>
      </div>
    </header>

    <section class="market-panel review-query-panel" aria-labelledby="review-query-title">
      <div class="panel-header">
        <h2 id="review-query-title" class="market-panel-title">查询商品</h2>
      </div>
      <Alert v-if="queryError" :message="queryError" class="notice" show-icon type="error" />
      <Form :model="{ productId }" class="query-form" layout="inline" @finish="selectProduct">
        <Form.Item label="商品 ID">
          <InputNumber
            v-model:value="productId"
            :min="1"
            :precision="0"
            placeholder="输入商品 ID，例如：1001"
            style="width: 220px"
          />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button html-type="submit" type="primary">查看评价</Button>
            <Button @click="router.push('/merchant/products')">前往商品管理</Button>
          </Space>
        </Form.Item>
      </Form>
    </section>

    <section class="market-panel" aria-labelledby="review-list-title">
      <div class="panel-header">
        <div class="list-head-info">
          <h2 id="review-list-title" class="market-panel-title">
            {{ currentProductId ? `商品 ${currentProductId} · 买家评价` : '买家评价' }}
          </h2>
          <span v-if="currentProductId && hasLoaded && !listError" class="panel-badge">共 {{ total }} 条评价</span>
        </div>
      </div>

      <Alert v-if="feedback" :message="feedback" class="notice" show-icon type="success" closable @close="feedback = ''" />

      <div v-if="!currentProductId" class="empty-wrap">
        <Empty description="请先在上方输入商品 ID 并点击「查看评价」" />
      </div>

      <div v-else-if="listError" class="list-state-error">
        <Alert :message="listError" type="error" show-icon />
        <div class="state-actions">
          <Button size="small" type="primary" ghost :loading="loading" @click="() => load(page)">
            重试加载
          </Button>
        </div>
      </div>

      <Table
        v-else-if="!compactLayout"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="total > pageSize ? { current: page + 1, pageSize, total, showSizeChanger: false } : false"
        :scroll="{ x: 860 }"
        row-key="id"
        @change="changePage"
      >
        <template #emptyText>
          <Empty description="暂无该商品的买家评价" />
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'rating'">
            <div class="rating-cell">
              <Rate :value="record.rating" allow-half disabled class="rating-stars" />
              <span class="rating-score">{{ record.rating }}分</span>
            </div>
          </template>

          <template v-else-if="column.key === 'content'">
            <div class="review-content">
              {{ record.content || '未填写文字评价' }}
            </div>
          </template>

          <template v-else-if="column.key === 'merchantReply'">
            <div v-if="record.merchantReply" class="reply-card">
              <div class="reply-tag-wrap">
                <Tag color="cyan">商家回复</Tag>
                <span v-if="record.repliedAt" class="reply-time">{{ formatDate(record.repliedAt) }}</span>
              </div>
              <p class="reply-text">{{ record.merchantReply }}</p>
            </div>
            <span v-else class="text-placeholder">未回复</span>
          </template>

          <template v-else-if="column.key === 'createdAt'">
            <span class="time-cell">{{ formatDate(record.createdAt) }}</span>
          </template>

          <template v-else-if="column.key === 'actions'">
            <Button type="link" size="small" @click="beginReply(record as unknown as ProductReview)">
              {{ record.merchantReply ? '修改回复' : '回复' }}
            </Button>
          </template>
        </template>
      </Table>
      <div v-else class="review-mobile-list">
        <div v-if="loading" class="review-mobile-state">正在加载评价...</div>
        <Empty v-else-if="rows.length === 0" description="暂无该商品的买家评价" />
        <article v-for="review in loading ? [] : rows" :key="review.id" class="review-mobile-item">
          <div class="review-mobile-meta">
            <div class="rating-cell">
              <Rate :value="review.rating" allow-half disabled class="rating-stars" />
              <strong class="rating-score">{{ review.rating }} 分</strong>
            </div>
            <time class="time-cell">{{ formatDate(review.createdAt) }}</time>
          </div>
          <p class="review-content">{{ review.content || '未填写文字评价' }}</p>
          <div class="review-mobile-reply">
            <span class="reply-label">{{ review.merchantReply ? '商家已回复' : '尚未回复' }}</span>
            <p v-if="review.merchantReply" class="reply-text">{{ review.merchantReply }}</p>
          </div>
          <div class="review-mobile-actions">
            <span v-if="review.repliedAt" class="reply-time">回复于 {{ formatDate(review.repliedAt) }}</span>
            <Button type="link" size="small" @click="beginReply(review)">{{ review.merchantReply ? '修改回复' : '回复' }}</Button>
          </div>
        </article>
        <Pagination
          v-if="total > pageSize"
          :current="page + 1"
          :page-size="pageSize"
          :total="total"
          class="review-mobile-pagination"
          simple
          size="small"
          @change="(current: number) => changePage({ current })"
        />
      </div>
    </section>

    <section v-if="replyForm.reviewId" ref="replyPanel" class="market-panel reply-panel" aria-labelledby="reply-title">
      <div class="panel-header">
        <h2 id="reply-title" class="market-panel-title">回复买家评价 #{{ replyForm.reviewId }}</h2>
        <p class="panel-subtitle">回复将公开展示在商品详情页，请注意用语客观规范</p>
      </div>
      <Alert v-if="replyError" :message="replyError" class="notice" show-icon type="error" />
      <Form :model="replyForm" layout="vertical" @finish="submitReply">
        <Form.Item
          label="回复内容"
          name="content"
          :rules="[{ required: true, message: '请填写回复内容（1–500字）' }]"
        >
          <Input.TextArea
            v-model:value="replyForm.content"
            :maxlength="500"
            :rows="3"
            placeholder="针对买家反馈提供诚恳专业的解答或售后指引..."
            show-count
          />
        </Form.Item>
        <Space>
          <Button :loading="replyLoading" html-type="submit" type="primary">保存并发布回复</Button>
          <Button :disabled="replyLoading" @click="replyForm.reviewId = undefined">取消</Button>
        </Space>
      </Form>
    </section>
  </main>
</template>

<style scoped>
.review-query-panel { padding-block: 18px; }
.review-query-panel .panel-header { margin-bottom: 12px; }
.review-query-panel .market-panel-title { margin: 0; }
.review-mobile-list { display: grid; }
.review-mobile-item { padding: 16px 0; border-top: 1px solid hsl(var(--border)); }
.review-mobile-meta { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
.review-mobile-meta .time-cell { font-size: 12px; }
.review-mobile-item .review-content { margin: 10px 0; font-size: 13px; }
.review-mobile-reply { padding: 10px 12px; border-left: 2px solid hsl(var(--border)); background: hsl(var(--muted) / 0.45); }
.reply-label { color: hsl(var(--muted-foreground)); font-size: 12px; font-weight: 600; }
.review-mobile-reply .reply-text { margin-top: 4px; }
.review-mobile-actions { display: flex; justify-content: flex-end; align-items: center; gap: 8px; min-height: 36px; }
.review-mobile-actions .reply-time { margin-right: auto; }
.review-mobile-actions :deep(.ant-btn-link) { padding-inline: 0; }
.review-mobile-state { padding: 24px 0; text-align: center; color: hsl(var(--muted-foreground)); }
.review-mobile-pagination { margin: 14px auto 0; }
.panel-header {
  margin-bottom: 16px;
}

.panel-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--market-muted, #64748b);
}

.list-head-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-badge {
  font-size: 12px;
  color: var(--market-muted, #64748b);
  background: var(--market-surface, #f1f5f9);
  padding: 2px 8px;
  border-radius: 12px;
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

.rating-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.rating-stars {
  font-size: 13px;
  line-height: 1;
}

.rating-score {
  font-weight: 600;
  font-size: 13px;
  color: var(--market-text);
}

.review-content {
  color: var(--market-text);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.reply-card {
  background: var(--market-surface);
  border: 1px solid var(--market-line);
  border-radius: 6px;
  padding: 8px 12px;
}

.reply-tag-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.reply-time {
  font-size: 11px;
  color: var(--market-muted);
}

.reply-text {
  margin: 0;
  font-size: 13px;
  color: var(--market-text);
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-word;
}

.text-placeholder {
  color: var(--market-muted);
  font-size: 13px;
}

.time-cell {
  font-size: 13px;
  color: var(--market-muted);
  white-space: nowrap;
}

.reply-panel {
  margin-top: 16px;
  max-width: 680px;
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
