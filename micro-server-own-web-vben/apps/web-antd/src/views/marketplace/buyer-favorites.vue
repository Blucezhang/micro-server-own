<script lang="ts" setup>
import type { BuyerFavorite } from '#/api/marketplace';

import { onMounted, ref } from 'vue';

import { Alert, Button, Card, Drawer, Space, Table, Tag } from 'ant-design-vue';

import { buyerApi, cartApi, catalogApi } from '#/api/marketplace';

type Product = { id: number; name: string; originalPrice: string; promotionPrice?: string; content?: string; specification?: string };
const favorites = ref<BuyerFavorite[]>([]);
const detail = ref<Product>();
const loading = ref(false);
const actionLoading = ref(false);
const error = ref('');
const feedback = ref('');
const removeAttempts = new Map<number, string>();
const columns = [
  { dataIndex: 'productId', key: 'productId', title: '商品 ID' },
  { dataIndex: 'createdAt', key: 'createdAt', title: '收藏时间' },
  { key: 'actions', title: '操作' },
];
function errorText(reason: unknown, fallback: string) { return reason instanceof Error ? reason.message : fallback; }
async function load() {
  loading.value = true; error.value = '';
  try { favorites.value = await buyerApi.favorites(); }
  catch (reason) { error.value = errorText(reason, '收藏列表加载失败'); }
  finally { loading.value = false; }
}
async function openDetail(productId: number) {
  loading.value = true; error.value = '';
  try { detail.value = await catalogApi.product(productId) as Product; }
  catch (reason) { error.value = errorText(reason, '商品已不可售或详情加载失败，仍可取消收藏'); }
  finally { loading.value = false; }
}
async function remove(productId: number) {
  if (actionLoading.value) return;
  const key = removeAttempts.get(productId) || crypto.randomUUID();
  removeAttempts.set(productId, key); actionLoading.value = true; feedback.value = '';
  try {
    await buyerApi.removeFavorite(productId, key);
    removeAttempts.delete(productId);
    if (detail.value?.id === productId) detail.value = undefined;
    favorites.value = favorites.value.filter((item) => item.productId !== productId);
    feedback.value = '已取消收藏';
  } catch (reason) { error.value = errorText(reason, '取消收藏失败，请重试'); }
  finally { actionLoading.value = false; }
}
async function addToCart() {
  if (!detail.value || actionLoading.value) return;
  actionLoading.value = true; feedback.value = '';
  try { await cartApi.add(detail.value.id, 1); feedback.value = '已加入购物车'; }
  catch (reason) { error.value = errorText(reason, '加入购物车失败'); }
  finally { actionLoading.value = false; }
}
onMounted(load);
</script>

<template>
  <main class="page">
    <Card :bordered="false"><Tag color="cyan">买家中心</Tag><h1>我的收藏</h1><p>收藏的商品若已下架，仍可从这里取消收藏。</p></Card>
    <Card :bordered="false" title="收藏清单">
      <Alert v-if="error" :message="error" show-icon type="error" class="notice" />
      <Alert v-if="feedback" :message="feedback" show-icon type="success" class="notice" />
      <Button :loading="loading" class="refresh" @click="load">刷新</Button>
      <Table :columns="columns" :data-source="favorites" :loading="loading" :pagination="{ pageSize: 10, showSizeChanger: false }" :scroll="{ x: 560 }" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'createdAt'">{{ new Date(record.createdAt).toLocaleString('zh-CN') }}</template>
          <Space v-else-if="column.key === 'actions'"><Button type="link" @click="openDetail(record.productId)">查看商品</Button><Button type="link" danger :disabled="actionLoading" @click="remove(record.productId)">取消收藏</Button></Space>
        </template>
      </Table>
    </Card>
    <Drawer :open="!!detail" :title="detail?.name || '商品详情'" width="min(480px, 100vw)" @close="detail = undefined">
      <template v-if="detail"><p>¥{{ detail.promotionPrice || detail.originalPrice }}</p><p v-if="detail.specification">{{ detail.specification }}</p><p v-if="detail.content">{{ detail.content }}</p><Button type="primary" :loading="actionLoading" @click="addToCart">加入购物车</Button></template>
    </Drawer>
  </main>
</template>

<style scoped>
.page { display: grid; grid-template-columns: minmax(0, 1fr); gap: 16px; min-width: 0; padding: 24px; }
.page > :deep(.ant-card) { min-width: 0; }
h1 { font-size: 24px; margin: 10px 0 6px; }
p { color: #64748b; margin: 0; }
.notice, .refresh { margin-bottom: 16px; }
@media (max-width: 640px) { .page { padding: 16px; } }
</style>
