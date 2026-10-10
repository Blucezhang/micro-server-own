<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMediaQuery, useWindowSize } from '@vueuse/core';

import {
  Alert,
  Button,
  Empty,
  InputNumber,
  Popconfirm,
  Space,
  Spin,
  Table,
  Tag,
} from 'ant-design-vue';
import type { ColumnType } from 'ant-design-vue/es/table';

import { cartApi } from '#/api/marketplace';

interface CartRow {
  id: number;
  merchantId: number;
  productName: string;
  quantity: number;
  unitPrice: string;
}

const router = useRouter();
const loading = ref(false);
const rows = ref<CartRow[]>([]);
const selectedKeys = ref<number[]>([]);
const error = ref('');
const phoneLayout = useMediaQuery('(max-width: 680px)');
const { width: viewportWidth } = useWindowSize();

const total = computed(() =>
  rows.value
    .filter((row) => selectedKeys.value.includes(row.id))
    .reduce((sum, row) => sum + Number(row.unitPrice) * row.quantity, 0),
);

const isAllSelected = computed(
  () => rows.value.length > 0 && selectedKeys.value.length === rows.value.length,
);

const desktopColumns: ColumnType[] = [
  { dataIndex: 'productName', key: 'productName', title: '商品名称', width: 220, ellipsis: true },
  { dataIndex: 'merchantId', key: 'merchantId', title: '归属商家', width: 130 },
  { dataIndex: 'unitPrice', key: 'unitPrice', title: '单价', width: 120 },
  { key: 'quantity', title: '数量', width: 140 },
  { key: 'subtotal', title: '小计', width: 130 },
  { key: 'actions', title: '操作', width: 100, align: 'right' },
];
const columns = computed<ColumnType[]>(() => phoneLayout.value
  ? [
      { dataIndex: 'productName', key: 'productName', title: '商品信息', width: Math.max(100, viewportWidth.value - 204) },
      { key: 'actions', title: '操作', width: 100, align: 'right' },
    ]
  : desktopColumns);

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

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const list = (await cartApi.list()) as CartRow[];
    rows.value = list || [];
    selectedKeys.value = rows.value.map((row) => row.id);
  } catch (reason) {
    error.value = humanizeError(reason, '购物车加载失败，请重试');
  } finally {
    loading.value = false;
  }
}

async function updateQuantity(row: CartRow, value: null | number | string) {
  const quantity = Number(value);
  if (!quantity || quantity < 1 || quantity === row.quantity) return;
  const original = row.quantity;
  row.quantity = quantity;
  error.value = '';
  try {
    await cartApi.update(row.id, quantity);
  } catch (reason) {
    row.quantity = original;
    error.value = humanizeError(reason, '修改商品数量失败，请重试');
  }
}

async function remove(id: number) {
  error.value = '';
  try {
    await cartApi.remove(id);
    await load();
  } catch (reason) {
    error.value = humanizeError(reason, '移除商品失败，请重试');
  }
}

function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedKeys.value = [];
  } else {
    selectedKeys.value = rows.value.map((row) => row.id);
  }
}

function checkout() {
  if (!selectedKeys.value.length) return;
  router.push({ path: '/buyer/checkout', query: { items: selectedKeys.value.join(',') } });
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">BUYER · CART</span>
        <h1 class="market-heading">购物车</h1>
        <p class="market-subtitle">商品按商家归集；金额与可售状态以结算服务试算结果为准。</p>
      </div>
      <Space wrap>
        <Button :loading="loading" @click="load">刷新</Button>
      </Space>
    </header>

    <Alert
      v-if="error && rows.length"
      :message="error"
      type="error"
      show-icon
      closable
      class="cart-alert"
      @close="error = ''"
    />

    <section class="market-panel" aria-labelledby="cart-list-title">
      <div class="panel-header">
        <h2 id="cart-list-title" class="market-panel-title">待结算商品</h2>
        <span v-if="rows.length" class="market-note">共 {{ rows.length }} 项商品</span>
      </div>

      <div v-if="loading && !rows.length" class="loading-wrap">
        <Spin tip="正在加载购物车..." />
      </div>

      <div v-else-if="error && !rows.length" class="error-wrap">
        <Empty description="购物车服务暂不可用，未能加载待结算商品">
          <Button :loading="loading" type="primary" @click="load">重新加载</Button>
        </Empty>
      </div>

      <Table
        v-else
        :class="{ 'compact-table': phoneLayout }"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="false"
        :row-selection="{
          selectedRowKeys: selectedKeys,
          onChange: (keys) => { selectedKeys = keys.map(Number); },
        }"
        :scroll="phoneLayout ? undefined : { x: 720 }"
        row-key="id"
      >
        <template #emptyText>
          <div class="empty-wrap">
            <Empty description="购物车暂无待结算商品" />
          </div>
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'productName'">
            <div class="product-info">
              <span class="product-name">{{ record.productName }}</span>
              <span class="product-id">商品编号 #{{ record.id }}</span>
              <span v-if="phoneLayout" class="cart-mobile-meta">
                ¥{{ Number(record.unitPrice).toFixed(2) }} · {{ record.quantity }} 件 · 商家 #{{ record.merchantId }}
              </span>
            </div>
          </template>

          <template v-else-if="column.key === 'merchantId'">
            <Tag class="merchant-tag">商家 #{{ record.merchantId }}</Tag>
          </template>

          <template v-else-if="column.key === 'unitPrice'">
            <span class="price-val">¥{{ Number(record.unitPrice).toFixed(2) }}</span>
          </template>

          <template v-else-if="column.key === 'quantity'">
            <InputNumber
              :min="1"
              :value="record.quantity"
              aria-label="商品数量"
              class="quantity-input"
              @change="(value) => updateQuantity(record as CartRow, value)"
            />
          </template>

          <template v-else-if="column.key === 'subtotal'">
            <strong class="subtotal-val">
              ¥{{ (Number(record.unitPrice) * record.quantity).toFixed(2) }}
            </strong>
          </template>

          <template v-else-if="column.key === 'actions'">
            <Popconfirm
              title="确认从购物车移除该商品？"
              ok-text="确认"
              cancel-text="取消"
              @confirm="remove(record.id)"
            >
              <Button danger type="link" class="action-btn">移除</Button>
            </Popconfirm>
          </template>
        </template>
      </Table>
    </section>

    <section v-if="rows.length" class="market-panel settlement" aria-label="结算汇总">
      <div class="settlement-left">
        <Button size="small" type="link" class="select-all-btn" @click="toggleSelectAll">
          {{ isAllSelected ? '取消全选' : '全选所有' }}
        </Button>
        <span class="market-note">
          已选 <strong>{{ selectedKeys.length }}</strong> / {{ rows.length }} 件
        </span>
      </div>

      <div class="settlement-right">
        <div class="price-summary">
          <span class="market-note">商品金额：</span>
          <strong class="total-price">¥{{ total.toFixed(2) }}</strong>
        </div>
        <Button
          :disabled="!selectedKeys.length"
          size="large"
          type="primary"
          class="checkout-btn"
          @click="checkout"
        >
          去结算 ({{ selectedKeys.length }})
        </Button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.cart-alert {
  margin-bottom: 16px;
}

.panel-header {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.loading-wrap,
.empty-wrap,
.error-wrap {
  padding: 36px 0;
  text-align: center;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-name {
  color: var(--market-text);
  font-weight: 500;
}

.product-id {
  color: var(--market-muted);
  font-size: 12px;
}

.cart-mobile-meta {
  color: var(--market-muted);
  font-size: 12px;
}

.merchant-tag {
  border-radius: 4px;
}

.price-val {
  color: var(--market-text);
  font-variant-numeric: tabular-nums;
}

.subtotal-val {
  color: var(--market-text);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.quantity-input {
  max-width: 100px;
}

.action-btn {
  padding: 0;
}

.settlement {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: space-between;
  margin-top: 16px;
}

.settlement-left {
  align-items: center;
  display: flex;
  gap: 12px;
}

.select-all-btn {
  padding: 0;
}

.settlement-right {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.price-summary {
  align-items: baseline;
  display: flex;
  gap: 6px;
}

.total-price {
  color: var(--market-text);
  font-size: 24px;
  font-weight: 650;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.checkout-btn {
  min-width: 120px;
}

@media (max-width: 640px) {
  .settlement {
    align-items: stretch;
    flex-direction: column;
    gap: 12px;
  }

  .settlement-right {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .price-summary {
    justify-content: space-between;
  }

  .checkout-btn {
    width: 100%;
  }
}

@media (min-width: 681px) {
  .cart-mobile-meta { display: none; }
}
</style>
