<script lang="ts" setup>
import type { BuyerFavorite, ProductReview, ProductReviewPage } from '#/api/marketplace';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Alert, Button, Drawer, Empty, Form, Input, Pagination, Rate, Select, Space, Spin, Table } from 'ant-design-vue';

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
  if (!product.categoryId) return '未分类';
  return categories.value.find((category) => category.id === product.categoryId)?.name || `类目 #${product.categoryId}`;
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
  reviews.value = result.items || []; reviewTotal.value = Number(result.total || 0); reviewPage.value = page;
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
async function changePage(page: number) { filters.page = page - 1; await load(); }
async function search() { filters.page = 0; await load(); }
async function changeReviewPage(pagination: { current?: number }) { if (detail.value) await loadReviews(detail.value.id, (pagination.current || 1) - 1); }
onMounted(async () => {
  const [categoryResult] = await Promise.allSettled([catalogApi.categories(), loadFavorites(), load()]);
  if (categoryResult.status === 'fulfilled') categories.value = categoryResult.value as { id: number; name: string }[];
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div><span class="market-overline">BUYER · 商品</span><h1 class="market-heading">发现好物</h1><p class="market-subtitle">浏览商品、查看评价并加入收藏；价格与可售状态以结算时为准。</p></div>
      <Button @click="router.push('/buyer/favorites')">我的收藏</Button>
    </header>
    <section class="market-panel catalog-filters" aria-label="商品筛选">
      <Form :model="filters" class="filters" layout="inline" @finish="search">
        <Form.Item label="关键词" name="keyword"><Input v-model:value="filters.keyword" allow-clear /></Form.Item>
        <Form.Item label="分类" name="categoryId">
          <Select v-model:value="filters.categoryId" class="category-select" allow-clear :field-names="{ label: 'name', value: 'id' }" :options="categories" />
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
          <span v-else-if="catalogError" class="catalog-status-error">加载失败</span>
          <span v-else>{{ total }} 件商品</span>
        </div>
        <p class="market-note">价格与可售状态以结算试算为准</p>
      </div>
      <Spin v-if="loading" class="catalog-loading" />
      <div v-else-if="catalogError" class="catalog-error-state">
        <Alert
          :message="catalogError"
          description="未能成功获取商品列表，请检查网络或稍后重新加载。"
          show-icon
          type="error"
        >
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
            <button class="product-art" type="button" :aria-label="`查看${product.name}详情`" @click="openDetail(product)">
              <div class="product-art-header">
                <span class="product-art-tag">{{ productCategoryName(product) }}</span>
                <span class="product-art-id">#{{ product.id }}</span>
              </div>
              <div class="product-art-badge" aria-hidden="true">
                <div class="product-art-emblem">
                  <svg class="product-art-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z" />
                    <path d="m3.3 7 8.7 5 8.7-5" />
                    <path d="M12 12v10" />
                  </svg>
                  <span class="product-art-mark">{{ (product.name || '商品').slice(0, 2) }}</span>
                </div>
              </div>
              <div class="product-art-footer">
                <span v-if="product.specification" class="product-art-spec" :title="product.specification">
                  {{ product.specification }}
                </span>
              </div>
            </button>
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
            <div class="product-card-body">
              <h3 class="product-name">
                <button type="button" :title="product.name" @click="openDetail(product)">{{ product.name }}</button>
              </h3>
              <div class="product-meta">
                <span class="product-merchant">商家 #{{ product.partyId }}</span>
                <template v-if="product.specification">
                  <span class="product-spec-dot" aria-hidden="true">·</span>
                  <span class="product-spec" :title="product.specification">{{ product.specification }}</span>
                </template>
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
        <div class="product-summary"><span class="market-overline">商品 · #{{ detail.id }}</span><strong>¥{{ detail.promotionPrice || detail.originalPrice }}</strong><p>商家 ID {{ detail.partyId }}<span v-if="detail.specification"> · {{ detail.specification }}</span></p><p v-if="detail.content">{{ detail.content }}</p></div>
        <Space class="detail-actions">
          <Button :disabled="actionLoading" @click="toggleFavorite(detail.id)">{{ favoriteIds.has(detail.id) ? '取消收藏' : '收藏商品' }}</Button>
          <Button type="primary" :disabled="actionLoading" @click="add(detail)">加入购物车</Button>
        </Space>
        <Alert v-if="detailError" :message="detailError" show-icon type="error" class="notice" />
        <Alert v-if="feedback" :message="feedback" show-icon type="info" class="notice" />
        <section class="drawer-section" aria-label="商品评价">
<h2>商品评价</h2>
          <Table :columns="[{ dataIndex: 'rating', key: 'rating', title: '评分' }, { dataIndex: 'content', key: 'content', title: '评价' }, { dataIndex: 'merchantReply', key: 'merchantReply', title: '商家回复' }, { key: 'actions', title: '操作' }]" :data-source="reviews" :loading="detailLoading" :pagination="{ current: reviewPage + 1, pageSize: 10, total: reviewTotal, showSizeChanger: false }" :scroll="{ x: 580 }" row-key="id" @change="changeReviewPage">
            <template #bodyCell="{ column, record }"><Button v-if="column.key === 'actions'" type="link" @click="reportForm.reviewId = record.id; reportForm.reason = ''">举报</Button></template>
          </Table>
        </section>
        <section v-if="reportForm.reviewId" class="drawer-section" aria-label="举报评价">
<h2>举报评价 #{{ reportForm.reviewId }}</h2>
          <Form :model="reportForm" layout="vertical" @finish="reportReview">
            <Form.Item label="举报原因" name="reason" :rules="[{ required: true, message: '请填写举报原因' }]"><Input.TextArea v-model:value="reportForm.reason" :maxlength="200" :rows="2" show-count /></Form.Item>
            <Space><Button html-type="submit" :loading="actionLoading" type="primary">提交举报</Button><Button :disabled="actionLoading" @click="reportForm.reviewId = undefined">取消</Button></Space>
          </Form>
        </section>
        <section class="drawer-section" aria-label="发表评价">
<h2>发表评价</h2><p class="market-note">仅已签收且尚未评价的买家可提交，资格由服务端校验。</p>
          <Form :model="reviewForm" layout="vertical" @finish="createReview">
            <Form.Item label="评分" name="rating" :rules="[{ required: true, type: 'number', min: 1, max: 5 }]"><Rate v-model:value="reviewForm.rating" /></Form.Item>
            <Form.Item label="评价内容" name="content"><Input.TextArea v-model:value="reviewForm.content" :maxlength="500" :rows="3" show-count /></Form.Item>
            <Button :loading="actionLoading" html-type="submit" type="primary">提交评价</Button>
          </Form>
        </section>
      </template>
    </Drawer>
  </main>
</template>

<style scoped>
.category-select { width: 160px; }
.filters { margin-bottom: 0; }
.catalog-filters { padding: 18px 20px; }
.catalog-error-state { margin: 16px 0; }
.catalog-status-error { color: #cf1322; font-size: 12px; font-weight: 500; }
.catalog-results { min-width: 0; }
.results-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 16px; margin-bottom: 14px; }
.results-title { display: flex; align-items: baseline; gap: 10px; }
.results-title h2 { margin: 0; color: var(--market-text); font-size: 17px; font-weight: 650; letter-spacing: -.02em; }
.results-title span { color: var(--market-muted); font-size: 12px; }
.results-heading .market-note { margin: 0; font-size: 12px; }
.catalog-loading { display: grid; min-height: 240px; place-items: center; }
.product-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
@media (min-width: 1200px) {
  .product-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}
@media (min-width: 681px) and (max-width: 960px) {
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
.product-card { position: relative; min-width: 0; overflow: hidden; border: 1px solid var(--market-line); border-radius: var(--mall-radius-xl); background: var(--market-surface); box-shadow: 0 1px 2px rgb(28 36 48 / .035); transition: transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease; }
.product-card:hover { transform: translateY(-2px); border-color: hsl(var(--primary) / .28); box-shadow: 0 12px 28px rgb(28 36 48 / .08); }
.product-art { display: flex; flex-direction: column; justify-content: space-between; position: relative; width: 100%; height: 132px; padding: 12px 14px; box-sizing: border-box; overflow: hidden; border: 0; border-bottom: 1px solid var(--market-line); background: linear-gradient(150deg, hsl(var(--muted) / .42) 0%, hsl(var(--muted) / .8) 100%); color: var(--market-text); cursor: pointer; text-align: left; transition: background 160ms ease; }
.product-art:hover { background: linear-gradient(150deg, hsl(var(--muted) / .55) 0%, hsl(var(--muted) / .92) 100%); }
.product-art:focus-visible { outline: 2px solid hsl(var(--primary)); outline-offset: -2px; }
.product-art-header { display: flex; align-items: center; gap: 8px; max-width: calc(100% - 42px); }
.product-art-tag { display: inline-flex; align-items: center; padding: 1px 7px; border-radius: var(--mall-radius-base); background: hsl(var(--card) / .88); border: 1px solid hsl(var(--border) / .65); color: var(--market-muted); font-size: 11px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.product-art-id { color: var(--market-muted); font-size: 11px; font-weight: 500; opacity: .8; font-variant-numeric: tabular-nums; }
.product-art-badge { display: flex; align-items: center; justify-content: center; }
.product-art-emblem { display: inline-flex; align-items: center; gap: 7px; padding: 6px 14px; border: 1px solid hsl(var(--border) / .75); border-radius: 9999px; background: hsl(var(--card) / .92); box-shadow: 0 2px 8px rgb(28 36 48 / .04); transition: transform 160ms ease; }
.product-card:hover .product-art-emblem { transform: scale(1.03); }
.product-art-icon { width: 15px; height: 15px; color: hsl(var(--primary)); flex-shrink: 0; }
.product-art-mark { color: var(--market-text); font-size: 13px; font-weight: 650; letter-spacing: .04em; }
.product-art-footer { display: flex; align-items: center; min-height: 16px; }
.product-art-spec { display: inline-block; max-width: 100%; color: var(--market-muted); font-size: 11px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.favorite-action { position: absolute; z-index: 2; top: 10px; right: 10px; display: grid; width: 32px; height: 32px; place-items: center; border: 1px solid hsl(var(--border) / .85); border-radius: 50%; background: hsl(var(--card) / .92); color: hsl(var(--muted-foreground)); cursor: pointer; transition: transform 140ms ease, color 140ms ease, border-color 140ms ease; }
.favorite-action:hover { transform: scale(1.08); color: hsl(var(--primary)); }
.favorite-action.is-active { color: hsl(var(--primary)); border-color: hsl(var(--primary) / .35); background: hsl(var(--card)); }
.favorite-glyph { font-family: Arial, sans-serif; font-size: 16px; line-height: 1; }
.product-card-body { display: flex; flex-direction: column; padding: 13px 16px 14px; }
.product-name { margin: 0; font-size: 14.5px; font-weight: 650; line-height: 1.45; }
.product-name button { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; width: 100%; height: 42px; padding: 0; border: 0; background: none; color: var(--market-text); cursor: pointer; text-align: left; font-size: inherit; font-weight: inherit; line-height: inherit; transition: color 140ms ease; }
.product-name button:hover { color: hsl(var(--primary)); }
.product-meta { display: flex; align-items: center; gap: 6px; margin: 6px 0 10px; color: var(--market-muted); font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.product-merchant { font-weight: 500; }
.product-spec-dot { opacity: .5; }
.product-spec { overflow: hidden; text-overflow: ellipsis; }
.product-price-line { display: flex; align-items: baseline; gap: 7px; min-height: 28px; }
.price-current { display: inline-flex; align-items: baseline; color: hsl(var(--primary)); font-weight: 700; line-height: 1; }
.price-symbol { font-size: 13px; margin-right: 1px; }
.price-amount { font-size: 21px; letter-spacing: -.03em; }
.price-original { color: var(--market-muted); font-size: 12px; text-decoration: line-through; }
.price-discount-tag { display: inline-block; padding: 1px 6px; border-radius: 4px; background: hsl(var(--accent)); color: hsl(var(--accent-foreground)); font-size: 11px; font-weight: 600; line-height: 1.35; }
.product-actions { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-top: 12px; padding-top: 10px; border-top: 1px solid var(--market-line); }
.product-actions .btn-detail { padding-inline: 0; color: var(--market-muted); font-size: 13px; font-weight: 500; }
.product-actions .btn-detail:hover { color: hsl(var(--primary)); }
.product-actions .btn-add-cart { font-size: 13px; border-radius: var(--mall-radius-base); }
.catalog-pagination { display: flex; justify-content: flex-end; margin-top: 20px; }
.notice, .detail-actions { margin: 14px 0; }
.product-summary { display: grid; gap: 7px; }
.product-summary strong { font-size: 28px; font-weight: 650; letter-spacing: -.04em; }
.product-summary p { margin: 0; color: var(--market-muted); font-size: 12px; line-height: 1.6; }
.drawer-section { min-width: 0; margin-top: 26px; padding-top: 22px; border-top: 1px solid var(--market-line); }
.drawer-section h2 { margin: 0 0 14px; font-size: 15px; font-weight: 620; }
@media (max-width: 680px) {
  .filters :deep(.ant-form-item), .filter-actions { width: 100%; margin-right: 0; margin-bottom: 12px; }
  .filters :deep(.ant-form-item-control), .filters :deep(.ant-form-item-control-input), .filters :deep(.ant-form-item-control-input-content), .filters :deep(.ant-input), .category-select { width: 100%; }
  .catalog-filters { padding: 16px; }
  .results-heading { align-items: flex-start; flex-direction: column; gap: 4px; }
  .product-grid { grid-template-columns: minmax(0, 1fr); gap: 12px; }
  .product-art { height: 120px; }
  .product-actions { align-items: center; }
  .product-actions .btn-add-cart { flex: 1; }
}
</style>
