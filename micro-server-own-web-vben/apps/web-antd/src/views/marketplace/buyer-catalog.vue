<script lang="ts" setup>
import type { BuyerFavorite, ProductReview, ProductReviewPage } from '#/api/marketplace';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Alert, Button, Drawer, Form, Input, Rate, Select, Space, Table } from 'ant-design-vue';

import { buyerApi, cartApi, catalogApi } from '#/api/marketplace';

type Product = { id: number; name: string; originalPrice: string; promotionPrice?: string; partyId: number; content?: string; specification?: string };
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
const error = ref('');
const feedback = ref('');
const reviewForm = reactive({ content: '', rating: 5 });
const reportForm = reactive({ reason: '', reviewId: undefined as number | undefined });
const filters = reactive({ categoryId: undefined as number | undefined, keyword: '', page: 0, size: 20 });
const favoriteIds = computed(() => new Set(favorites.value.map((item) => item.productId)));
const reviewAttempt = computed(() => ({ productId: detail.value?.id, content: reviewForm.content.trim(), rating: reviewForm.rating, key: crypto.randomUUID() }));
const reportAttempt = computed(() => ({ productId: detail.value?.id, reviewId: reportForm.reviewId, reason: reportForm.reason.trim(), key: crypto.randomUUID() }));
const favoriteAttempts = new Map<string, string>();
const columns = [
  { dataIndex: 'name', key: 'name', title: '商品' },
  { key: 'price', title: '价格' },
  { dataIndex: 'partyId', key: 'partyId', title: '商家 ID' },
  { key: 'actions', title: '操作' },
];
function errorText(reason: unknown, fallback: string) { return reason instanceof Error ? reason.message : fallback; }
async function load() {
  loading.value = true; error.value = '';
  try {
    const result = await catalogApi.products(filters) as { items: Product[]; total: number };
    rows.value = result.items || []; total.value = Number(result.total || 0);
  } catch (reason) { error.value = errorText(reason, '商品列表加载失败'); }
  finally { loading.value = false; }
}
async function loadFavorites() {
  try { favorites.value = await buyerApi.favorites(); }
  catch (reason) { error.value = errorText(reason, '收藏状态加载失败'); }
}
async function loadReviews(productId: number, page = 0) {
  const result: ProductReviewPage = await catalogApi.reviews(productId, page);
  if (detail.value?.id !== productId) return;
  reviews.value = result.items || []; reviewTotal.value = Number(result.total || 0); reviewPage.value = page;
}
async function openDetail(row: Record<string, any>) {
  detailLoading.value = true; error.value = ''; feedback.value = '';
  reportForm.reviewId = undefined;
  try { detail.value = await catalogApi.product(Number(row.id)) as Product; await loadReviews(Number(row.id)); }
  catch (reason) { error.value = errorText(reason, '商品详情或评价加载失败'); }
  finally { detailLoading.value = false; }
}
async function toggleFavorite(productId: number) {
  if (actionLoading.value) return;
  const isFavorite = favoriteIds.value.has(productId);
  const attemptId = `${productId}:${isFavorite ? 'remove' : 'add'}`;
  const key = favoriteAttempts.get(attemptId) || crypto.randomUUID();
  favoriteAttempts.set(attemptId, key); actionLoading.value = true; feedback.value = '';
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
  } catch (reason) { feedback.value = errorText(reason, '收藏操作失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function add(row: Record<string, any>) {
  if (actionLoading.value) return;
  actionLoading.value = true; feedback.value = '';
  try { await cartApi.add(Number(row.id), 1); feedback.value = '已加入购物车'; }
  catch (reason) { feedback.value = errorText(reason, '加入购物车失败'); }
  finally { actionLoading.value = false; }
}
async function createReview() {
  const attempt = reviewAttempt.value;
  if (actionLoading.value || !attempt.productId) return;
  actionLoading.value = true; feedback.value = '';
  try {
    await buyerApi.createReview(attempt.productId, { content: attempt.content, rating: attempt.rating }, attempt.key);
    reviewForm.content = ''; reviewForm.rating = 5; feedback.value = '评价已提交';
    await loadReviews(attempt.productId);
  } catch (reason) { feedback.value = errorText(reason, '评价提交失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function reportReview() {
  const attempt = reportAttempt.value;
  if (actionLoading.value || !attempt.productId || !attempt.reviewId) return;
  if (!attempt.reason || attempt.reason.length > 200) { feedback.value = '举报原因需要 1–200 个字符'; return; }
  actionLoading.value = true; feedback.value = '';
  try {
    await buyerApi.reportReview(attempt.productId, attempt.reviewId, attempt.reason, attempt.key);
    reportForm.reason = ''; reportForm.reviewId = undefined;
    feedback.value = '举报已提交，等待平台处理';
  } catch (reason) { feedback.value = errorText(reason, '举报提交失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function changePage(pagination: { current?: number }) { filters.page = (pagination.current || 1) - 1; await load(); }
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
    <section class="market-panel" aria-label="商品浏览">
      <Form :model="filters" class="filters" layout="inline" @finish="search">
        <Form.Item label="关键词"><Input v-model:value="filters.keyword" allow-clear /></Form.Item>
        <Form.Item label="分类">
          <Select v-model:value="filters.categoryId" class="category-select" allow-clear :field-names="{ label: 'name', value: 'id' }" :options="categories" />
        </Form.Item>
        <Space class="filter-actions">
          <Button html-type="submit" type="primary">搜索</Button>
          <Button @click="Object.assign(filters, { categoryId: undefined, keyword: '' }); search()">重置</Button>
        </Space>
      </Form>
      <Alert v-if="error" :message="error" show-icon type="error" class="notice" />
      <Alert v-if="feedback" :message="feedback" show-icon type="info" class="notice" />
      <Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: filters.page + 1, pageSize: filters.size, total, showSizeChanger: false }" :scroll="{ x: 680 }" row-key="id" @change="changePage">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'price'">¥{{ record.promotionPrice || record.originalPrice }}</template>
          <Space v-else-if="column.key === 'actions'">
            <Button type="link" @click="openDetail(record)">详情与评价</Button>
            <Button type="link" :disabled="actionLoading" @click="toggleFavorite(record.id)">{{ favoriteIds.has(record.id) ? '取消收藏' : '收藏' }}</Button>
            <Button type="link" :disabled="actionLoading" @click="add(record)">加入购物车</Button>
          </Space>
        </template>
      </Table>
    </section>
    <Drawer :open="!!detail" :title="detail?.name || '商品详情'" width="min(600px, 100vw)" @close="detail = undefined">
      <template v-if="detail">
        <div class="product-summary"><span class="market-overline">商品 · #{{ detail.id }}</span><strong>¥{{ detail.promotionPrice || detail.originalPrice }}</strong><p>商家 ID {{ detail.partyId }}<span v-if="detail.specification"> · {{ detail.specification }}</span></p><p v-if="detail.content">{{ detail.content }}</p></div>
        <Space class="detail-actions">
          <Button :disabled="actionLoading" @click="toggleFavorite(detail.id)">{{ favoriteIds.has(detail.id) ? '取消收藏' : '收藏商品' }}</Button>
          <Button type="primary" :disabled="actionLoading" @click="add(detail)">加入购物车</Button>
        </Space>
        <Alert v-if="error" :message="error" show-icon type="error" class="notice" />
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
.filters { margin-bottom: 18px; }
.notice, .detail-actions { margin: 14px 0; }
.product-summary { display: grid; gap: 7px; }
.product-summary strong { font-size: 28px; font-weight: 650; letter-spacing: -.04em; }
.product-summary p { margin: 0; color: var(--market-muted); font-size: 12px; line-height: 1.6; }
.drawer-section { min-width: 0; margin-top: 26px; padding-top: 22px; border-top: 1px solid var(--market-line); }
.drawer-section h2 { margin: 0 0 14px; font-size: 15px; font-weight: 620; }
@media (max-width: 640px) {
  .filters :deep(.ant-form-item), .filter-actions { width: 100%; margin-right: 0; margin-bottom: 12px; }
  .filters :deep(.ant-form-item-control), .filters :deep(.ant-form-item-control-input), .filters :deep(.ant-form-item-control-input-content), .filters :deep(.ant-input), .category-select { width: 100%; }
}
</style>
