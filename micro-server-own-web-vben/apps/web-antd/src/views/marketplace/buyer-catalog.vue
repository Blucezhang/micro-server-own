<script lang="ts" setup>
import type { BuyerFavorite, ProductReview, ProductReviewPage } from '#/api/marketplace';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Alert, Button, Drawer, Empty, Form, Input, Pagination, Rate, Select, Space, Spin, Table, Tag } from 'ant-design-vue';

import { buyerApi, cartApi, catalogApi } from '#/api/marketplace';

type Product = {
  id: number;
  name: string;
  originalPrice: string;
  promotionPrice?: string;
  partyId: number;
  categoryId?: number;
  content?: string;
  specification?: string;
};

const router = useRouter();
const loading = ref(false);
const detailLoading = ref(false);
const actionLoading = ref(false);
const rows = ref<Product[]>([]);
const categories = ref<{ id: number; name: string }[]>([]);
const favorites = ref<BuyerFavorite[]>([]);
const detail = ref<Product>();
const reviews = ref<ProductReview[]>([]);
const reviewTotal = ref(0);
const reviewPage = ref(0);
const total = ref(0);
const catalogError = ref('');
const detailError = ref('');
const actionError = ref('');
const feedback = ref('');
const reviewForm = reactive({ content: '', rating: 5 });
const reportForm = reactive({ reason: '', reviewId: undefined as number | undefined });
const filters = reactive({ categoryId: undefined as number | undefined, keyword: '', page: 0, size: 20 });
const favoriteIds = computed(() => new Set(favorites.value.map((item) => item.productId)));
const reviewAttempt = computed(() => ({ productId: detail.value?.id, content: reviewForm.content.trim(), rating: reviewForm.rating, key: crypto.randomUUID() }));
const reportAttempt = computed(() => ({ productId: detail.value?.id, reviewId: reportForm.reviewId, reason: reportForm.reason.trim(), key: crypto.randomUUID() }));
const favoriteAttempts = new Map<string, string>();

function productCategoryName(product: Product): string {
  if (!product.categoryId) return '全品类';
  return categories.value.find((category) => category.id === product.categoryId)?.name || `分类 #${product.categoryId}`;
}

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

async function load() {
  loading.value = true;
  catalogError.value = '';
  try {
    const result = await catalogApi.products(filters) as { items: Product[]; total: number };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (reason) {
    rows.value = [];
    total.value = 0;
    catalogError.value = formatError(reason, '商品列表加载失败');
  } finally {
    loading.value = false;
  }
}

async function loadFavorites() {
  try {
    favorites.value = await buyerApi.favorites();
  } catch (reason) {
    actionError.value = formatError(reason, '收藏状态加载失败');
  }
}

async function loadReviews(productId: number, page = 0) {
  const result: ProductReviewPage = await catalogApi.reviews(productId, page);
  if (detail.value?.id !== productId) return;
  reviews.value = result.items || [];
  reviewTotal.value = Number(result.total || 0);
  reviewPage.value = page;
}

async function openDetail(row: Record<string, any>) {
  detailLoading.value = true;
  detailError.value = '';
  actionError.value = '';
  feedback.value = '';
  detail.value = undefined;
  reviews.value = [];
  reportForm.reviewId = undefined;
  try {
    detail.value = await catalogApi.product(Number(row.id)) as Product;
    await loadReviews(Number(row.id));
  } catch (reason) {
    if (detail.value) {
      detailError.value = formatError(reason, '商品详情或评价加载失败');
    } else {
      actionError.value = formatError(reason, '商品详情加载失败');
    }
  } finally {
    detailLoading.value = false;
  }
}

async function toggleFavorite(productId: number) {
  if (actionLoading.value) return;
  const isFavorite = favoriteIds.value.has(productId);
  const attemptId = `${productId}:${isFavorite ? 'remove' : 'add'}`;
  const key = favoriteAttempts.get(attemptId) || crypto.randomUUID();
  favoriteAttempts.set(attemptId, key);
  actionLoading.value = true;
  feedback.value = '';
  actionError.value = '';
  try {
    if (isFavorite) {
      await buyerApi.removeFavorite(productId, key);
      favorites.value = favorites.value.filter((item) => item.productId !== productId);
    } else {
      const favorite = await buyerApi.addFavorite(productId, key);
      favorites.value = [favorite, ...favorites.value.filter((item) => item.productId !== productId)];
    }
    favoriteAttempts.delete(attemptId);
    feedback.value = isFavorite ? '已取消收藏' : '已加入收藏';
  } catch (reason) {
    actionError.value = formatError(reason, '收藏操作失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function add(row: Record<string, any>) {
  if (actionLoading.value) return;
  actionLoading.value = true;
  feedback.value = '';
  actionError.value = '';
  try {
    await cartApi.add(Number(row.id), 1);
    feedback.value = '已加入购物车';
  } catch (reason) {
    actionError.value = formatError(reason, '加入购物车失败');
  } finally {
    actionLoading.value = false;
  }
}

async function createReview() {
  const attempt = reviewAttempt.value;
  if (actionLoading.value || !attempt.productId) return;
  actionLoading.value = true;
  feedback.value = '';
  detailError.value = '';
  try {
    await buyerApi.createReview(attempt.productId, { content: attempt.content, rating: attempt.rating }, attempt.key);
    reviewForm.content = '';
    reviewForm.rating = 5;
    feedback.value = '评价已提交';
    await loadReviews(attempt.productId);
  } catch (reason) {
    detailError.value = formatError(reason, '评价提交失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function reportReview() {
  const attempt = reportAttempt.value;
  if (actionLoading.value || !attempt.productId || !attempt.reviewId) return;
  if (!attempt.reason || attempt.reason.length > 200) {
    feedback.value = '举报原因需要 1–200 个字符';
    return;
  }
  actionLoading.value = true;
  feedback.value = '';
  detailError.value = '';
  try {
    await buyerApi.reportReview(attempt.productId, attempt.reviewId, attempt.reason, attempt.key);
    reportForm.reason = '';
    reportForm.reviewId = undefined;
    feedback.value = '举报已提交，等待平台处理';
  } catch (reason) {
    detailError.value = formatError(reason, '举报提交失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function changePage(page: number) {
  filters.page = page - 1;
  await load();
}

async function search() {
  filters.page = 0;
  await load();
}

async function changeReviewPage(pagination: { current?: number }) {
  if (detail.value) await loadReviews(detail.value.id, (pagination.current || 1) - 1);
}

onMounted(async () => {
  const [categoryResult] = await Promise.allSettled([catalogApi.categories(), loadFavorites(), load()]);
  if (categoryResult.status === 'fulfilled') {
    categories.value = categoryResult.value as { id: number; name: string }[];
  }
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">发现好物</h1>
        <p class="market-subtitle">浏览商品并可直接加入收藏或购物车；价格与优惠由结算服务试算确认。</p>
      </div>
      <Button @click="router.push('/buyer/favorites')">我的收藏</Button>
    </header>

    <section class="market-panel catalog-filters" aria-label="商品筛选">
      <Form :model="filters" class="filters" layout="inline" @finish="search">
        <Form.Item label="关键词" name="keyword">
          <Input v-model:value="filters.keyword" placeholder="搜索商品名称..." allow-clear />
        </Form.Item>
        <Form.Item label="分类" name="categoryId">
          <Select
            v-model:value="filters.categoryId"
            placeholder="全部分类"
            class="category-select"
            allow-clear
            :field-names="{ label: 'name', value: 'id' }"
            :options="categories"
          />
        </Form.Item>
        <Space class="filter-actions">
          <Button html-type="submit" type="primary">搜索</Button>
          <Button @click="Object.assign(filters, { categoryId: undefined, keyword: '' }); search()">重置</Button>
        </Space>
      </Form>
      <Alert v-if="actionError && !catalogError" :message="actionError" show-icon type="error" class="notice" />
      <Alert v-if="feedback" :message="feedback" show-icon type="info" class="notice" />
    </section>

    <section class="catalog-results" aria-label="商品列表">
      <div class="results-heading">
        <div class="results-title">
          <h2>为你挑选</h2>
          <span v-if="loading">加载中...</span>
          <span v-else-if="!catalogError">{{ total }} 件商品</span>
        </div>
      </div>

      <Spin v-if="loading" class="catalog-loading" />
      <div v-else-if="catalogError" class="catalog-error-state">
        <Alert :message="catalogError" show-icon type="error">
          <template #action>
            <Button :loading="loading" danger ghost size="small" type="primary" @click="load">
              重新加载
            </Button>
          </template>
        </Alert>
      </div>
      <Empty v-else-if="!rows.length" description="没有找到符合条件的商品" />
      <template v-else>
        <div class="product-grid">
          <article v-for="product in rows" :key="product.id" class="product-card">
            <div class="product-card-top">
              <div class="product-card-badges">
                <Tag class="product-cat-tag">{{ productCategoryName(product) }}</Tag>
                <span class="product-merchant-tag">商家 #{{ product.partyId }}</span>
              </div>
              <Button
                class="favorite-action"
                :class="{ 'is-active': favoriteIds.has(product.id) }"
                :aria-label="favoriteIds.has(product.id) ? `取消收藏 ${product.name}` : `加入收藏 ${product.name}`"
                :disabled="actionLoading"
                :title="favoriteIds.has(product.id) ? '取消收藏' : '加入收藏'"
                type="text"
                @click="toggleFavorite(product.id)"
              >
                <span aria-hidden="true" class="favorite-glyph">{{ favoriteIds.has(product.id) ? '♥' : '♡' }}</span>
              </Button>
            </div>

            <div class="product-card-body">
              <h3 class="product-name">
                <button type="button" :title="product.name" @click="openDetail(product)">{{ product.name }}</button>
              </h3>
              <div v-if="product.specification" class="product-spec-line">
                <span class="product-spec-text" :title="product.specification">{{ product.specification }}</span>
              </div>

              <div class="product-price-line">
                <div class="price-current">
                  <span class="price-symbol">¥</span>
                  <strong class="price-amount">{{ product.promotionPrice || product.originalPrice }}</strong>
                </div>
                <del v-if="product.promotionPrice" class="price-original">¥{{ product.originalPrice }}</del>
                <span v-if="product.promotionPrice" class="price-discount-tag">特惠</span>
              </div>

              <div class="product-actions">
                <Button class="btn-detail" type="link" @click="openDetail(product)">详情与评价</Button>
                <Button class="btn-add-cart" :disabled="actionLoading" type="primary" @click="add(product)">加入购物车</Button>
              </div>
            </div>
          </article>
        </div>
        <Pagination
          v-if="total > filters.size"
          :current="filters.page + 1"
          :page-size="filters.size"
          :show-size-changer="false"
          :total="total"
          class="catalog-pagination"
          @change="changePage"
        />
      </template>
    </section>

    <Drawer :open="!!detail" :title="detail?.name || '商品详情'" width="min(600px, 100vw)" @close="detail = undefined">
      <template v-if="detail">
        <div class="product-summary">
          <div class="detail-head-meta">
            <Tag class="product-cat-tag">{{ productCategoryName(detail) }}</Tag>
            <span class="detail-id-text">商品编号 #{{ detail.id }}</span>
          </div>
          <strong class="detail-price">¥{{ detail.promotionPrice || detail.originalPrice }}</strong>
          <p class="detail-merchant">商家 #{{ detail.partyId }}<span v-if="detail.specification"> · 规格：{{ detail.specification }}</span></p>
          <p v-if="detail.content" class="detail-content">{{ detail.content }}</p>
        </div>
        <Space class="detail-actions">
          <Button :disabled="actionLoading" @click="toggleFavorite(detail.id)">
            {{ favoriteIds.has(detail.id) ? '取消收藏' : '收藏商品' }}
          </Button>
          <Button type="primary" :disabled="actionLoading" @click="add(detail)">加入购物车</Button>
        </Space>
        <Alert v-if="detailError" :message="detailError" show-icon type="error" class="notice" />
        <Alert v-if="feedback" :message="feedback" show-icon type="info" class="notice" />

        <section class="drawer-section" aria-label="商品评价">
          <h2>商品评价</h2>
          <Table
            :columns="[
              { dataIndex: 'rating', key: 'rating', title: '评分', width: 80 },
              { dataIndex: 'content', key: 'content', title: '评价内容' },
              { dataIndex: 'merchantReply', key: 'merchantReply', title: '商家回复' },
              { key: 'actions', title: '操作', width: 70, align: 'right' }
            ]"
            :data-source="reviews"
            :loading="detailLoading"
            :pagination="{ current: reviewPage + 1, pageSize: 10, total: reviewTotal, showSizeChanger: false }"
            :scroll="{ x: 500 }"
            row-key="id"
            @change="changeReviewPage"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'rating'">
                <span class="review-rating">{{ record.rating }} 星</span>
              </template>
              <template v-else-if="column.key === 'actions'">
                <Button type="link" @click="reportForm.reviewId = record.id; reportForm.reason = ''">举报</Button>
              </template>
            </template>
          </Table>
        </section>

        <section v-if="reportForm.reviewId" class="drawer-section" aria-label="举报评价">
          <h2>举报评价 #{{ reportForm.reviewId }}</h2>
          <Form :model="reportForm" layout="vertical" @finish="reportReview">
            <Form.Item label="举报原因" name="reason" :rules="[{ required: true, message: '请填写举报原因' }]">
              <Input.TextArea v-model:value="reportForm.reason" :maxlength="200" :rows="2" show-count />
            </Form.Item>
            <Space>
              <Button html-type="submit" :loading="actionLoading" type="primary">提交举报</Button>
              <Button :disabled="actionLoading" @click="reportForm.reviewId = undefined">取消</Button>
            </Space>
          </Form>
        </section>

        <section class="drawer-section" aria-label="发表评价">
          <h2>发表评价</h2>
          <p class="market-note">仅已完成订单的买家可提交评价，资格由服务端校验。</p>
          <Form :model="reviewForm" layout="vertical" @finish="createReview">
            <Form.Item label="评分" name="rating" :rules="[{ required: true, type: 'number', min: 1, max: 5 }]">
              <Rate v-model:value="reviewForm.rating" />
            </Form.Item>
            <Form.Item label="评价内容" name="content">
              <Input.TextArea v-model:value="reviewForm.content" :maxlength="500" :rows="3" show-count />
            </Form.Item>
            <Button :loading="actionLoading" html-type="submit" type="primary">提交评价</Button>
          </Form>
        </section>
      </template>
    </Drawer>
  </main>
</template>

<style scoped src="./buyer-catalog.css"></style>
