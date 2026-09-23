<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Button, Card, InputNumber, Popconfirm, Table, Tag } from 'ant-design-vue';

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
  <main class="cart-page">
    <Card :bordered="false">
      <div class="heading">
        <div>
          <Tag color="cyan">买家中心</Tag>
          <h1>购物车</h1>
          <p>商品按商家归集；金额与可售状态以结算试算为准。</p>
        </div>
        <Button @click="load">刷新</Button>
      </div>
    </Card>
    <Card :bordered="false">
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
    </Card>
    <Card :bordered="false" class="settlement">
      <span>已选 {{ selectedKeys.length }} 件商品</span>
      <strong>¥{{ total.toFixed(2) }}</strong>
      <Button :disabled="!selectedKeys.length" type="primary" @click="checkout">去结算</Button>
    </Card>
  </main>
</template>

<style scoped>
.cart-page { display: grid; gap: 16px; padding: 24px; }
.heading, .settlement { align-items: center; display: flex; justify-content: space-between; gap: 16px; }
h1 { font-size: 24px; font-weight: 700; margin: 10px 0 6px; }
p { color: #64748b; margin: 0; }
.settlement strong { color: #0f766e; font-size: 24px; margin-left: auto; }
@media (max-width: 640px) { .cart-page { padding: 16px; } .heading { align-items: stretch; flex-direction: column; } }
</style>
