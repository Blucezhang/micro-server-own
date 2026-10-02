<script lang="ts" setup>
import type { ProductReview } from '#/api/marketplace';

import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Alert, Button, Form, Input, InputNumber, Space, Table } from 'ant-design-vue';

import { catalogApi, merchantApi } from '#/api/marketplace';

const route = useRoute();
const router = useRouter();
const productId = ref<number>();
const currentProductId = ref<number>();
const rows = ref<ProductReview[]>([]);
const page = ref(0);
const total = ref(0);
const loading = ref(false);
const replyLoading = ref(false);
const error = ref('');
const feedback = ref('');
const replyForm = reactive({ content: '', reviewId: undefined as number | undefined });
const replyAttempt = computed(() => ({ content: replyForm.content.trim(), reviewId: replyForm.reviewId, key: crypto.randomUUID() }));
const columns = [
  { dataIndex: 'rating', key: 'rating', title: '评分' },
  { dataIndex: 'content', key: 'content', title: '评价' },
  { dataIndex: 'merchantReply', key: 'merchantReply', title: '商家回复' },
  { key: 'actions', title: '操作' },
];
function errorText(reason: unknown, fallback: string) { return reason instanceof Error ? reason.message : fallback; }
async function load(nextPage = 0) {
  const id = currentProductId.value;
  if (!id) return;
  loading.value = true; error.value = '';
  try {
    const result = await catalogApi.reviews(id, nextPage);
    if (currentProductId.value !== id) return;
    rows.value = result.items || []; total.value = Number(result.total || 0); page.value = nextPage;
  } catch (reason) { error.value = errorText(reason, '评价列表加载失败'); }
  finally { loading.value = false; }
}
async function selectProduct() {
  if (!Number.isSafeInteger(productId.value) || !productId.value || productId.value <= 0) {
    error.value = '请输入正整数商品 ID'; return;
  }
  currentProductId.value = productId.value;
  rows.value = []; total.value = 0; replyForm.reviewId = undefined;
  await router.replace({ query: { productId: String(productId.value) } });
  await load();
}
function beginReply(review: Record<string, any>) {
  replyForm.reviewId = review.id;
  replyForm.content = review.merchantReply || '';
  feedback.value = '';
}
async function submitReply() {
  const attempt = replyAttempt.value;
  if (replyLoading.value || !attempt.reviewId) return;
  if (!attempt.content || attempt.content.length > 500) { error.value = '回复内容需要 1–500 个字符'; return; }
  replyLoading.value = true; error.value = ''; feedback.value = '';
  try {
    await merchantApi.replyReview(attempt.reviewId, attempt.content, attempt.key);
    replyForm.reviewId = undefined; replyForm.content = '';
    feedback.value = '回复已保存';
    await load(page.value);
  } catch (reason) { error.value = errorText(reason, '回复失败，请在本页重试'); }
  finally { replyLoading.value = false; }
}
async function changePage(pagination: { current?: number }) { await load((pagination.current || 1) - 1); }
watch(() => route.query.productId, (value) => {
  const parsed = typeof value === 'string' && /^[1-9]\d*$/.test(value) ? Number(value) : undefined;
  if (!parsed || !Number.isSafeInteger(parsed) || parsed === currentProductId.value) return;
  productId.value = parsed; currentProductId.value = parsed; load();
}, { immediate: true });
onMounted(() => { if (!currentProductId.value) feedback.value = '可从商品管理选择商品，或输入自己商品的 ID 查询评价。'; });
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · REVIEWS</span>
        <h1 class="market-heading">商品评价</h1>
        <p class="market-subtitle">按商品查看公开评价；回复时服务端会校验商品归属。</p>
      </div>
    </header>
    <section class="market-panel" aria-labelledby="review-query-title">
      <h2 id="review-query-title" class="market-panel-title">选择商品</h2>
      <Form :model="{ productId }" layout="inline" @finish="selectProduct">
        <Form.Item label="商品 ID"><InputNumber v-model:value="productId" :min="1" :precision="0" /></Form.Item>
        <Space><Button html-type="submit" type="primary">查看评价</Button><Button @click="router.push('/merchant/products')">前往商品管理</Button></Space>
      </Form>
    </section>
    <section class="market-panel" aria-labelledby="review-list-title">
      <h2 id="review-list-title" class="market-panel-title">{{ currentProductId ? `商品 ${currentProductId} 的评价` : '商品评价' }}</h2>
      <Alert v-if="error" :message="error" show-icon type="error" class="notice" />
      <Alert v-if="feedback" :message="feedback" show-icon type="success" class="notice" />
      <Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize: 10, total, showSizeChanger: false }" :scroll="{ x: 680 }" row-key="id" @change="changePage">
        <template #bodyCell="{ column, record }"><Button v-if="column.key === 'actions'" type="link" @click="beginReply(record)">{{ record.merchantReply ? '修改回复' : '回复' }}</Button></template>
      </Table>
    </section>
    <section v-if="replyForm.reviewId" class="market-panel" aria-labelledby="reply-title">
      <h2 id="reply-title" class="market-panel-title">回复评价 #{{ replyForm.reviewId }}</h2>
      <Form :model="replyForm" layout="vertical" @finish="submitReply">
        <Form.Item label="回复内容" name="content" :rules="[{ required: true, message: '请填写回复内容' }]"><Input.TextArea v-model:value="replyForm.content" :maxlength="500" :rows="3" show-count /></Form.Item>
        <Space><Button html-type="submit" type="primary" :loading="replyLoading">保存回复</Button><Button :disabled="replyLoading" @click="replyForm.reviewId = undefined">取消</Button></Space>
      </Form>
    </section>
  </main>
</template>

<style scoped>
.notice { margin-bottom: 16px; }
</style>
