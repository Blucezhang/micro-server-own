<script lang="ts" setup>
import type { BuyerFavorite } from '#/api/marketplace';

import { onMounted, ref } from 'vue';

import {
  Alert,
  Button,
  Drawer,
  Empty,
  Popconfirm,
  Space,
  Spin,
  Table,
  Tag,
} from 'ant-design-vue';

import { buyerApi, cartApi, catalogApi } from '#/api/marketplace';

interface Product {
  id: number;
  name: string;
  originalPrice: string;
  promotionPrice?: string;
  content?: string;
  specification?: string;
}

const favorites = ref<BuyerFavorite[]>([]);
const detail = ref<Product>();
const selectedProductId = ref<number>();
const loading = ref(false);
const detailLoading = ref(false);
const actionLoading = ref(false);
const error = ref('');
const detailError = ref('');
const feedback = ref('');
const drawerOpen = ref(false);
const removeAttempts = new Map<number, string>();

const columns = [
  { dataIndex: 'productId', key: 'productId', title: '商品编号', width: 140 },
  { key: 'createdAt', title: '收藏时间', minWidth: 180 },
  { key: 'actions', title: '操作', width: 160, align: 'right' as const },
];

function humanizeError(reason: unknown, fallback: string): string {
  if (!reason) return fallback;
  const msg = reason instanceof Error ? reason.message : String(reason);
  if (/502|Bad Gateway/i.test(msg)) {
    return '服务网关暂时不可用，请稍后重试';
  }
  if (/504|Gateway Timeout|timeout/i.test(msg)) {
    return '服务响应超时，请重试';
  }
  if (/500|Internal Server Error/i.test(msg)) {
    return '服务端处理异常，请稍后重试';
  }
  if (msg.includes('Request failed with status code')) {
    return '服务请求失败，请稍后重试';
  }
  return msg || fallback;
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
    const list = await buyerApi.favorites();
    favorites.value = list || [];
  } catch (reason) {
    error.value = humanizeError(reason, '收藏列表加载失败，请重试');
  } finally {
    loading.value = false;
  }
}

async function openDetail(productId: number) {
  selectedProductId.value = productId;
  drawerOpen.value = true;
  detailLoading.value = true;
  detail.value = undefined;
  detailError.value = '';
  try {
    detail.value = (await catalogApi.product(productId)) as Product;
  } catch (reason) {
    detailError.value = humanizeError(
      reason,
      '商品已不可售或详情加载失败，仍可直接取消收藏',
    );
  } finally {
    detailLoading.value = false;
  }
}

function closeDrawer() {
  drawerOpen.value = false;
  detail.value = undefined;
  detailError.value = '';
  selectedProductId.value = undefined;
}

async function remove(productId: number) {
  if (actionLoading.value) return;
  const key = removeAttempts.get(productId) || crypto.randomUUID();
  removeAttempts.set(productId, key);
  actionLoading.value = true;
  feedback.value = '';
  error.value = '';
  try {
    await buyerApi.removeFavorite(productId, key);
    removeAttempts.delete(productId);
    if (selectedProductId.value === productId) {
      closeDrawer();
    }
    favorites.value = favorites.value.filter((item) => item.productId !== productId);
    feedback.value = '已取消收藏';
  } catch (reason) {
    error.value = humanizeError(reason, '取消收藏失败，请重试');
  } finally {
    actionLoading.value = false;
  }
}

async function addToCart() {
  if (!detail.value || actionLoading.value) return;
  actionLoading.value = true;
  feedback.value = '';
  error.value = '';
  try {
    await cartApi.add(detail.value.id, 1);
    feedback.value = '已加入购物车';
  } catch (reason) {
    error.value = humanizeError(reason, '加入购物车失败，请重试');
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
        <h1 class="market-heading">我的收藏</h1>
        <p class="market-subtitle">查看已收藏的商品清单；支持查看详情并直接加入购物车。</p>
      </div>
      <Space wrap>
        <Button :loading="loading" @click="load">刷新</Button>
      </Space>
    </header>

    <Alert
      v-if="error && favorites.length"
      :message="error"
      type="error"
      show-icon
      closable
      class="notice-alert"
      @close="error = ''"
    />
    <Alert
      v-if="feedback"
      :message="feedback"
      type="info"
      show-icon
      closable
      class="notice-alert"
      @close="feedback = ''"
    />

    <section class="market-panel" aria-labelledby="favorites-title">
      <div class="panel-header">
        <h2 id="favorites-title" class="market-panel-title">收藏清单</h2>
        <span v-if="favorites.length" class="market-note">共 {{ favorites.length }} 项商品</span>
      </div>

      <div v-if="loading && !favorites.length" class="loading-wrap">
        <Spin tip="正在加载收藏清单..." />
      </div>

      <div v-else-if="error && !favorites.length" class="error-wrap">
        <Alert :message="error" show-icon type="error">
          <template #action>
            <Button :loading="loading" danger ghost size="small" type="primary" @click="load">
              重新加载
            </Button>
          </template>
        </Alert>
      </div>

      <Table
        v-else
        :columns="columns"
        :data-source="favorites"
        :loading="loading"
        :pagination="{ pageSize: 10, showSizeChanger: false, hideOnSinglePage: true }"
        :scroll="{ x: 480 }"
        row-key="productId"
      >
        <template #emptyText>
          <div class="empty-wrap">
            <Empty description="暂无收藏商品" />
          </div>
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'productId'">
            <div class="product-id-cell">
              <Tag class="id-tag">#{{ record.productId }}</Tag>
              <span class="product-id-sub">商品</span>
            </div>
          </template>

          <template v-else-if="column.key === 'createdAt'">
            <span class="date-text">{{ formatDate(record.createdAt) }}</span>
          </template>

          <template v-else-if="column.key === 'actions'">
            <Space>
              <Button type="link" class="action-btn" @click="openDetail(record.productId)">
                查看商品
              </Button>
              <Popconfirm
                title="确认取消收藏该商品？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="remove(record.productId)"
              >
                <Button danger :disabled="actionLoading" type="link" class="action-btn">
                  取消收藏
                </Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </section>

    <Drawer
      :open="drawerOpen"
      :title="detail?.name || (selectedProductId ? `商品详情 · #${selectedProductId}` : '商品详情')"
      width="min(500px, 100vw)"
      @close="closeDrawer"
    >
      <div v-if="detailLoading" class="drawer-loading">
        <Spin tip="正在加载商品详情..." />
      </div>

      <template v-else>
        <div v-if="detailError" class="drawer-error">
          <Alert :message="detailError" show-icon type="warning" class="notice-alert" />
          <div class="unavailable-actions">
            <Button
              v-if="selectedProductId"
              danger
              :disabled="actionLoading"
              @click="remove(selectedProductId)"
            >
              取消收藏
            </Button>
          </div>
        </div>

        <div v-else-if="detail" class="product-summary">
          <div class="summary-header">
            <Tag class="id-tag">商品编号 #{{ detail.id }}</Tag>
            <h3 class="product-name">{{ detail.name }}</h3>
          </div>

          <div class="price-row">
            <span class="price-symbol">¥</span>
            <strong class="price-primary">{{ detail.promotionPrice || detail.originalPrice }}</strong>
            <del
              v-if="detail.promotionPrice && detail.originalPrice"
              class="original-price"
            >
              ¥{{ detail.originalPrice }}
            </del>
          </div>

          <div v-if="detail.specification" class="meta-section">
            <span class="meta-label">规格参数</span>
            <p class="meta-value">{{ detail.specification }}</p>
          </div>

          <div v-if="detail.content" class="meta-section">
            <span class="meta-label">商品介绍</span>
            <p class="meta-value">{{ detail.content }}</p>
          </div>

          <Space class="detail-actions" wrap>
            <Button danger :disabled="actionLoading" @click="remove(detail.id)">
              取消收藏
            </Button>
            <Button :loading="actionLoading" type="primary" @click="addToCart">
              加入购物车
            </Button>
          </Space>
        </div>

        <Alert
          v-if="error"
          :message="error"
          show-icon
          type="error"
          closable
          class="notice-alert"
          @close="error = ''"
        />
        <Alert
          v-if="feedback"
          :message="feedback"
          show-icon
          type="info"
          closable
          class="notice-alert"
          @close="feedback = ''"
        />
      </template>
    </Drawer>
  </main>
</template>

<style scoped>
.notice-alert {
  margin-bottom: 16px;
}

.panel-header {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-header .market-panel-title {
  margin: 0;
}

.loading-wrap,
.empty-wrap,
.error-wrap {
  padding: 32px 0;
  text-align: center;
}

.product-id-cell {
  align-items: center;
  display: inline-flex;
  gap: 8px;
}

.id-tag {
  border-radius: var(--mall-radius-base);
  margin: 0;
}

.product-id-sub {
  color: var(--market-muted);
  font-size: 12px;
}

.date-text {
  color: var(--market-muted);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.action-btn {
  padding: 0 4px;
}

.drawer-loading {
  display: flex;
  justify-content: center;
  padding: 48px 0;
}

.drawer-error {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.unavailable-actions {
  display: flex;
  justify-content: flex-start;
}

.product-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.summary-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.product-name {
  color: var(--market-text);
  font-size: 17px;
  font-weight: 650;
  line-height: 1.4;
  margin: 0;
}

.price-row {
  align-items: baseline;
  display: flex;
  gap: 4px;
  color: hsl(var(--primary));
}

.price-symbol {
  font-size: 14px;
  font-weight: 600;
}

.price-primary {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.original-price {
  margin-left: 8px;
  color: var(--market-muted);
  font-size: 13px;
  text-decoration: line-through;
}

.meta-section {
  border-top: 1px solid var(--market-line);
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 12px;
}

.meta-label {
  color: var(--market-muted);
  font-size: 12px;
  font-weight: 500;
}

.meta-value {
  color: var(--market-text);
  font-size: 13.5px;
  line-height: 1.6;
  margin: 0;
}

.detail-actions {
  margin-top: 8px;
  padding-top: 14px;
  border-top: 1px solid var(--market-line);
}
</style>
