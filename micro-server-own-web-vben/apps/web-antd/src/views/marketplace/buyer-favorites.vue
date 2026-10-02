<script lang="ts" setup>
import type { BuyerFavorite } from '#/api/marketplace';

import { onMounted, ref } from 'vue';

import { Alert, Button, Drawer, Space, Table } from 'ant-design-vue';

import { buyerApi, cartApi, catalogApi } from '#/api/marketplace';

type Product = {
  id: number;
  name: string;
  originalPrice: string;
  promotionPrice?: string;
  content?: string;
  specification?: string;
};

const favorites = ref<BuyerFavorite[]>([]);
const detail = ref<Product>();
const loading = ref(false);
const actionLoading = ref(false);
const error = ref('');
const feedback = ref('');
const removeAttempts = new Map<number, string>();

const columns = [
  { dataIndex: 'productId', key: 'productId', title: '商品 ID' },
  { key: 'createdAt', title: '收藏时间' },
  { key: 'actions', title: '操作' },
];

function errorText(reason: unknown, fallback: string) {
  return reason instanceof Error ? reason.message : fallback;
}

function formatDate(value?: number | string) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    favorites.value = await buyerApi.favorites();
  } catch (reason) {
    error.value = errorText(reason, '收藏列表加载失败');
  } finally {
    loading.value = false;
  }
}

async function openDetail(productId: number) {
  loading.value = true;
  error.value = '';
  try {
    detail.value = (await catalogApi.product(productId)) as Product;
  } catch (reason) {
    error.value = errorText(reason, '商品已不可售或详情加载失败，仍可取消收藏');
  } finally {
    loading.value = false;
  }
}

async function remove(productId: number) {
  if (actionLoading.value) return;
  const key = removeAttempts.get(productId) || crypto.randomUUID();
  removeAttempts.set(productId, key);
  actionLoading.value = true;
  feedback.value = '';
  try {
    await buyerApi.removeFavorite(productId, key);
    removeAttempts.delete(productId);
    if (detail.value?.id === productId) detail.value = undefined;
    favorites.value = favorites.value.filter((item) => item.productId !== productId);
    feedback.value = '已取消收藏';
  } catch (reason) {
    error.value = errorText(reason, '取消收藏失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function addToCart() {
  if (!detail.value || actionLoading.value) return;
  actionLoading.value = true;
  feedback.value = '';
  try {
    await cartApi.add(detail.value.id, 1);
    feedback.value = '已加入购物车';
  } catch (reason) {
    error.value = errorText(reason, '加入购物车失败');
  } finally {
    actionLoading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">BUYER · 收藏</span>
        <h1 class="market-heading">我的收藏</h1>
        <p class="market-subtitle">收藏的商品若已下架，仍可从这里取消收藏。</p>
      </div>
      <Button :loading="loading" @click="load">刷新</Button>
    </header>

    <section class="market-panel" aria-labelledby="favorites-title">
      <h2 id="favorites-title" class="market-panel-title">收藏清单</h2>
      <Alert v-if="error" :message="error" show-icon type="error" class="notice" />
      <Alert v-if="feedback" :message="feedback" show-icon type="success" class="notice" />
      <Table
        :columns="columns"
        :data-source="favorites"
        :loading="loading"
        :pagination="{ pageSize: 10, showSizeChanger: false }"
        :scroll="{ x: 560 }"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'createdAt'">
            {{ formatDate(record.createdAt) }}
          </template>
          <Space v-else-if="column.key === 'actions'">
            <Button type="link" @click="openDetail(record.productId)">查看商品</Button>
            <Button danger :disabled="actionLoading" type="link" @click="remove(record.productId)">
              取消收藏
            </Button>
          </Space>
        </template>
      </Table>
    </section>

    <Drawer
      :open="!!detail"
      :title="detail?.name || '商品详情'"
      width="min(480px, 100vw)"
      @close="detail = undefined"
    >
      <template v-if="detail">
        <div class="product-summary">
          <span class="market-overline">商品 · #{{ detail.id }}</span>
          <strong>¥{{ detail.promotionPrice || detail.originalPrice }}</strong>
          <p v-if="detail.specification">规格 · {{ detail.specification }}</p>
          <p v-if="detail.content">{{ detail.content }}</p>
        </div>
        <Space class="detail-actions">
          <Button danger :disabled="actionLoading" @click="remove(detail.id)">取消收藏</Button>
          <Button :loading="actionLoading" type="primary" @click="addToCart">加入购物车</Button>
        </Space>
        <Alert v-if="error" :message="error" show-icon type="error" class="notice" />
        <Alert v-if="feedback" :message="feedback" show-icon type="success" class="notice" />
      </template>
    </Drawer>
  </main>
</template>

<style scoped>
.notice,
.detail-actions {
  margin: 14px 0;
}

.product-summary {
  display: grid;
  gap: 8px;
}

.product-summary strong {
  color: var(--market-text);
  font-size: 26px;
  font-weight: 650;
  letter-spacing: -0.03em;
}

.product-summary p {
  margin: 0;
  color: var(--market-muted);
  font-size: 13px;
  line-height: 1.6;
}
</style>
