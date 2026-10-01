<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Button, InputNumber, Popconfirm, Table } from 'ant-design-vue';

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
const total = computed(() =>
  rows.value
    .filter((row) => selectedKeys.value.includes(row.id))
    .reduce((sum, row) => sum + Number(row.unitPrice) * row.quantity, 0),
);

const columns = [
  { dataIndex: 'productName', key: 'productName', title: '商品' },
  { dataIndex: 'merchantId', key: 'merchantId', title: '商家' },
  { dataIndex: 'unitPrice', key: 'unitPrice', title: '单价' },
  { key: 'quantity', title: '数量' },
  { key: 'subtotal', title: '小计' },
  { key: 'actions', title: '操作' },
];

async function load() {
  loading.value = true;
  try {
    rows.value = (await cartApi.list()) as CartRow[];
    selectedKeys.value = rows.value.map((row) => row.id);
  } finally {
    loading.value = false;
  }
}

async function updateQuantity(row: CartRow, value: null | number | string) {
  const quantity = Number(value);
  if (!quantity || quantity < 1) return;
  await cartApi.update(row.id, quantity);
  row.quantity = quantity;
}

async function remove(id: number) {
  await cartApi.remove(id);
  await load();
}

function checkout() {
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
      <Button :loading="loading" @click="load">刷新</Button>
    </header>
    <section class="market-panel" aria-labelledby="cart-list-title">
      <h2 id="cart-list-title" class="market-panel-title">待结算商品</h2>
      <Table
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="false"
        :row-selection="{ selectedRowKeys: selectedKeys, onChange: (keys) => { selectedKeys = keys.map(Number) } }"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'quantity'">
            <InputNumber :min="1" :value="record.quantity" @change="(value) => updateQuantity(record as CartRow, value)" />
          </template>
          <template v-else-if="column.key === 'subtotal'">¥{{ (Number(record.unitPrice) * record.quantity).toFixed(2) }}</template>
          <template v-else-if="column.key === 'actions'">
            <Popconfirm title="确认从购物车移除该商品？" @confirm="remove(record.id)"><Button danger type="link">移除</Button></Popconfirm>
          </template>
        </template>
      </Table>
    </section>
    <section class="market-panel settlement" aria-label="结算汇总">
      <span class="market-note">已选 {{ selectedKeys.length }} 件商品</span>
      <strong>¥{{ total.toFixed(2) }}</strong>
      <Button :disabled="!selectedKeys.length" type="primary" @click="checkout">去结算</Button>
    </section>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.settlement { align-items: center; display: flex; gap: 16px; justify-content: flex-end; }
.settlement strong { font-size: 26px; letter-spacing: -.04em; margin-right: 8px; }
@media (max-width: 640px) { .settlement { align-items: flex-start; flex-direction: column; } }
</style>
