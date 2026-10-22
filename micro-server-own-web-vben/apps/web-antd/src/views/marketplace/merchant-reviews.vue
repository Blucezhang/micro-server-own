<script lang="ts" setup>
import type { ProductReview } from '#/api/marketplace';

import { computed, nextTick, reactive, ref, watch } from 'vue';
import { useMediaQuery } from '@vueuse/core';
import { useRoute, useRouter } from 'vue-router';
import { Alert, Button, Empty, Form, Input, InputNumber, Pagination, Rate, Space, Table, Tag } from 'ant-design-vue';

import { catalogApi, merchantApi } from '#/api/marketplace';

const route = useRoute();
const router = useRouter();
const isMobile = useMediaQuery('(max-width: 768px)');

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

const replyForm = reactive({
  content: '',
  reviewId: undefined as number | undefined,
});

const replyAttempt = computed(() => ({
  content: replyForm.content.trim(),
  key: crypto.randomUUID(),
  reviewId: replyForm.reviewId,
}));

const columns = [
  { dataIndex: 'rating', key: 'rating', title: '评分', width: 140 },
  { dataIndex: 'content', key: 'content', title: '评价内容' },
  { dataIndex: 'merchantReply', key: 'merchantReply', title: '商家回复', width: 320 },
  { dataIndex: 'createdAt', key: 'createdAt', title: '评价时间', width: 160 },
  { align: 'center' as const, key: 'actions', title: '操作', width: 100 },
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
  if (!dateStr) return '—';
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
  await load(0);
}

async function beginReply(review: ProductReview) {
  replyForm.reviewId = review.id;
  replyForm.content = review.merchantReply || '';
  replyError.value = '';
  feedback.value = '';
  await nextTick();
  replyPanel.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
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

async function changePage(pagination: number | { current?: number }) {
  const targetPage = typeof pagination === 'number' ? pagination : (pagination.current || 1);
  await load(targetPage - 1);
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
    void load(0);
  },
  { immediate: true },
);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div class="market-head-text">
        <div class="head-title-row">
          <h1 class="market-heading">商品评价管理</h1>
          <span v-if="currentProductId && hasLoaded && !listError && total > 0" class="catalog-badge">
            共 {{ total }} 条评价
          </span>
        </div>
        <p class="market-subtitle">查看买家真实评分与反馈，跟进处理尚未回复的商品评价。</p>
      </div>
      <div class="market-head-actions">
        <Button @click="router.push('/merchant/products')">返回商品管理</Button>
      </div>
    </header>

    <Alert
      v-if="feedback"
      :message="feedback"
      class="action-alert"
      show-icon
      type="success"
      closable
      @close="feedback = ''"
    />

    <section class="market-panel review-panel" aria-label="商品评价列表">
      <div class="review-toolbar">
        <Form :model="{ productId }" class="query-form" layout="inline" @finish="selectProduct">
          <Form.Item label="商品 ID" class="query-form-item">
            <InputNumber
              v-model:value="productId"
              :min="1"
              :precision="0"
              placeholder="输入商品 ID，例如：1001"
              class="query-input"
            />
          </Form.Item>
          <Form.Item class="query-form-actions">
            <Space wrap>
              <Button :loading="loading" html-type="submit" type="primary">查看评价</Button>
              <Button v-if="currentProductId" :disabled="loading" @click="() => load(page)">刷新</Button>
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
        <Button :loading="loading" size="small" type="primary" @click="() => load(page)">重新加载</Button>
      </div>

      <div v-else-if="!currentProductId" class="query-guide-strip">
        <div class="guide-content">
          <span class="guide-dot" aria-hidden="true" />
          <span class="guide-text">请输入商品 ID 并点击「查看评价」检索买家反馈与回复。</span>
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
              <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无该商品的买家评价" />
            </template>

            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'rating'">
                <div class="rating-cell">
                  <Rate :value="record.rating" allow-half disabled class="rating-stars" />
                  <span class="rating-score">{{ record.rating }} 分</span>
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
        </div>

        <div v-else class="mobile-card-list">
          <div v-if="loading" class="mobile-loading-tip">正在载入评价数据...</div>
          <div v-else-if="rows.length === 0" class="mobile-empty">
            <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无该商品的买家评价" />
          </div>
          <article v-for="item in (loading ? [] : rows)" :key="item.id" class="mobile-card">
            <div class="mobile-card-top">
              <div class="rating-cell">
                <Rate :value="item.rating" allow-half disabled class="rating-stars" />
                <span class="rating-score">{{ item.rating }} 分</span>
              </div>
              <time class="time-cell">{{ formatDate(item.createdAt) }}</time>
            </div>
            <p class="review-content">{{ item.content || '未填写文字评价' }}</p>
            <div v-if="item.merchantReply" class="reply-card">
              <div class="reply-tag-wrap">
                <Tag color="cyan">商家回复</Tag>
                <span v-if="item.repliedAt" class="reply-time">{{ formatDate(item.repliedAt) }}</span>
              </div>
              <p class="reply-text">{{ item.merchantReply }}</p>
            </div>
            <div class="mobile-card-actions">
              <span v-if="!item.merchantReply" class="text-placeholder">尚未回复</span>
              <span v-else class="reply-time">已回复</span>
              <Button type="link" size="small" @click="beginReply(item)">
                {{ item.merchantReply ? '修改回复' : '回复' }}
              </Button>
            </div>
          </article>
          <div v-if="total > pageSize" class="mobile-pagination-wrap">
            <Pagination :current="page + 1" :page-size="pageSize" :total="total" size="small" simple @change="changePage" />
          </div>
        </div>
      </template>
    </section>

    <!-- 回复抽屉/卡片 -->
    <section v-if="replyForm.reviewId" ref="replyPanel" class="reply-panel" aria-label="回复买家评价">
      <div class="reply-panel-header">
        <h2 class="reply-panel-title">回复买家评价 #{{ replyForm.reviewId }}</h2>
        <p class="reply-panel-subtitle">回复将公开展示在商品详情页，请注意用语客观规范（1–500字）。</p>
      </div>

      <Alert v-if="replyError" :message="replyError" class="action-alert" show-icon type="error" closable @close="replyError = ''" />

      <Form :model="replyForm" layout="vertical" @finish="submitReply">
        <Form.Item label="回复内容" name="content" :rules="[{ required: true, message: '请填写回复内容（1–500字）' }]">
          <Input.TextArea
            v-model:value="replyForm.content"
            :maxlength="500"
            :rows="3"
            placeholder="针对买家反馈提供诚恳专业的解答或售后指引..."
            show-count
          />
        </Form.Item>
        <Space wrap>
          <Button :loading="replyLoading" html-type="submit" type="primary">保存并发布回复</Button>
          <Button :disabled="replyLoading" @click="replyForm.reviewId = undefined">取消</Button>
        </Space>
      </Form>
    </section>
  </main>
</template>

<style scoped src="./merchant-reviews.css"></style>
