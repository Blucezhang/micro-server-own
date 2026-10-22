<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMediaQuery } from '@vueuse/core';

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

const total = computed(() =>
  rows.value
    .filter((row) => selectedKeys.value.includes(row.id))
    .reduce((sum, row) => sum + Number(row.unitPrice) * row.quantity, 0),
);

const isAllSelected = computed(
  () => rows.value.length > 0 && selectedKeys.value.length === rows.value.length,
);

const desktopColumns: ColumnType[] = [
  { dataIndex: 'productName', key: 'productName', title: '商品名称', minWidth: 200, ellipsis: true },
  { dataIndex: 'merchantId', key: 'merchantId', title: '归属商家', width: 130 },
  { dataIndex: 'unitPrice', key: 'unitPrice', title: '单价', width: 110 },
  { key: 'quantity', title: '数量', width: 130 },
  { key: 'subtotal', title: '小计', width: 120 },
  { key: 'actions', title: '操作', width: 90, align: 'right' },
];

const mobileColumns: ColumnType[] = [
  { dataIndex: 'productName', key: 'productName', title: '商品信息' },
  { key: 'actions', title: '操作', width: 80, align: 'right' },
];

const columns = computed<ColumnType[]>(() => (phoneLayout.value ? mobileColumns : desktopColumns));

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
        <h1 class="market-heading">购物车</h1>
        <p class="market-subtitle">管理待结算商品并核对数量；勾选后进入结算试算优惠。</p>
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
            <Empty description="购物车暂无待结算商品">
              <Button type="primary" @click="router.push('/buyer/catalog')">去挑选商品</Button>
            </Empty>
          </div>
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'productName'">
            <div class="product-info">
              <span class="product-name">{{ record.productName }}</span>
              <span class="product-id">商品编号 #{{ record.id }}</span>
              <div v-if="phoneLayout" class="cart-mobile-row">
                <span class="price-val">¥{{ Number(record.unitPrice).toFixed(2) }}</span>
                <span class="merchant-mini-tag">商家 #{{ record.merchantId }}</span>
                <InputNumber
                  :min="1"
                  :value="record.quantity"
                  size="small"
                  aria-label="商品数量"
                  class="quantity-input-mobile"
                  @change="(value) => updateQuantity(record as CartRow, value)"
                />
              </div>
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
          <span class="market-note">合计金额：</span>
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

.panel-header .market-panel-title {
  margin: 0;
}

.loading-wrap,
.empty-wrap,
.error-wrap {
  padding: 32px 0;
  text-align: center;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.product-name {
  color: var(--market-text);
  font-weight: 600;
  font-size: 14px;
}

.product-id {
  color: var(--market-muted);
  font-size: 12px;
}

.cart-mobile-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}

.merchant-mini-tag {
  color: var(--market-muted);
  font-size: 11px;
}

.quantity-input-mobile {
  max-width: 80px;
}

.merchant-tag {
  border-radius: var(--mall-radius-base);
}

.price-val {
  color: var(--market-text);
  font-variant-numeric: tabular-nums;
  font-size: 13.5px;
}

.subtotal-val {
  color: hsl(var(--primary));
  font-weight: 650;
  font-variant-numeric: tabular-nums;
  font-size: 14px;
}

.quantity-input {
  max-width: 96px;
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
  padding: 16px 20px;
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
  color: hsl(var(--primary));
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.checkout-btn {
  min-width: 124px;
  font-weight: 600;
}

@media (max-width: 640px) {
  .settlement {
    align-items: stretch;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
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
</style>
