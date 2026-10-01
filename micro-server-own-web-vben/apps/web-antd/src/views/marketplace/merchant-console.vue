<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Alert, Button, Form, Input, InputNumber, Space, Table, Tag } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

type ProductRow = Record<string, number | string | undefined>;
type ProductForm = {
  categoryId?: number;
  name: string;
  originalPrice?: number;
  partyId?: number;
  priceChangeReason: string;
  promotionPrice?: number;
  skuCode: string;
};

const router = useRouter();
const loading = ref(false);
const rows = ref<ProductRow[]>([]);
const page = ref(0);
const total = ref(0);
const editingId = ref<number>();
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const pageSize = 20;
const form = reactive<ProductForm>(emptyForm());

const columns = [
  { dataIndex: 'id', key: 'id', title: '商品 ID' },
  { dataIndex: 'name', key: 'name', title: '商品名称' },
  { dataIndex: 'skuCode', key: 'skuCode', title: 'SKU' },
  { dataIndex: 'originalPrice', key: 'originalPrice', title: '原价' },
  { dataIndex: 'promotionPrice', key: 'promotionPrice', title: '促销价' },
  { dataIndex: 'saleStatus', key: 'saleStatus', title: '状态' },
  { key: 'actions', title: '操作' },
];

function emptyForm(): ProductForm {
  return { categoryId: undefined, name: '', originalPrice: undefined, partyId: undefined, priceChangeReason: '', promotionPrice: undefined, skuCode: '' };
}

function message(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

function reset() {
  editingId.value = undefined;
  Object.assign(form, emptyForm());
}

async function load() {
  loading.value = true;
  feedback.value = null;
  try {
    const result = (await merchantApi.products({ page: page.value, size: pageSize })) as {
      items?: ProductRow[];
      total?: number;
    };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (error) {
    feedback.value = { text: message(error, '商品列表加载失败，请刷新重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

function edit(row: ProductRow) {
  editingId.value = Number(row.id);
  Object.assign(form, {
    categoryId: Number(row.categoryId) || undefined,
    name: String(row.name || ''),
    originalPrice: Number(row.originalPrice) || undefined,
    partyId: Number(row.partyId) || undefined,
    priceChangeReason: '',
    promotionPrice: row.promotionPrice == null ? undefined : Number(row.promotionPrice),
    skuCode: String(row.skuCode || ''),
  });
}

async function save() {
  if (loading.value) return;
  loading.value = true;
  feedback.value = null;
  try {
    await merchantApi.saveProduct({ ...form }, editingId.value, crypto.randomUUID());
    reset();
    feedback.value = { text: '商品信息已保存；改价记录可在价格审计中查看。', type: 'success' };
    await load();
  } catch (error) {
    feedback.value = { text: message(error, '保存失败，请稍后重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function changeStatus(row: ProductRow) {
  if (loading.value) return;
  loading.value = true;
  feedback.value = null;
  try {
    const id = Number(row.id);
    await merchantApi.changeProductStatus(id, row.saleStatus === 'AVAILABLE' ? 'OFF_SHELF' : 'AVAILABLE');
    await load();
  } catch (error) {
    feedback.value = { text: message(error, '商品状态更新失败，请稍后重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · CATALOG</span>
        <h1 class="market-heading">商品管理</h1>
        <p class="market-subtitle">维护店铺商品与上下架状态；价格变更由服务端留存审计记录。</p>
      </div>
      <Button :loading="loading" @click="load">刷新商品</Button>
    </header>

    <div class="market-grid">
      <section class="market-panel" aria-labelledby="product-form-title">
        <h2 id="product-form-title" class="market-panel-title">{{ editingId ? '编辑商品' : '发布商品' }}</h2>
        <Form :disabled="loading" :model="form" layout="vertical" @finish="save">
          <div class="form-pair">
            <Form.Item label="商家 ID" name="partyId" :rules="[{ required: true, message: '请填写商家 ID' }]">
              <InputNumber v-model:value="form.partyId" class="full" />
            </Form.Item>
            <Form.Item label="类目 ID" name="categoryId" :rules="[{ required: true, message: '请填写类目 ID' }]">
              <InputNumber v-model:value="form.categoryId" class="full" />
            </Form.Item>
          </div>
          <Form.Item label="商品名称" name="name" :rules="[{ required: true, message: '请填写商品名称' }]">
            <Input v-model:value="form.name" />
          </Form.Item>
          <Form.Item label="SKU 编码" name="skuCode" :rules="[{ required: true, message: '请填写 SKU 编码' }]">
            <Input v-model:value="form.skuCode" />
          </Form.Item>
          <div class="form-pair">
            <Form.Item label="原价" name="originalPrice" :rules="[{ required: true, message: '请填写原价' }]">
              <InputNumber v-model:value="form.originalPrice" :min="0" class="full" />
            </Form.Item>
            <Form.Item label="促销价"><InputNumber v-model:value="form.promotionPrice" :min="0" class="full" /></Form.Item>
          </div>
          <Form.Item v-if="editingId" label="改价原因"><Input v-model:value="form.priceChangeReason" /></Form.Item>
          <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="notice" show-icon />
          <Space>
            <Button :loading="loading" html-type="submit" type="primary">{{ editingId ? '保存编辑' : '发布商品' }}</Button>
            <Button v-if="editingId" :disabled="loading" @click="reset">取消编辑</Button>
          </Space>
        </Form>
      </section>

      <section class="market-panel" aria-labelledby="product-list-title">
        <h2 id="product-list-title" class="market-panel-title">商品列表</h2>
        <Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" :scroll="{ x: 900 }" row-key="id" @change="changePage">
          <template #bodyCell="{ column, record }">
            <Tag v-if="column.key === 'saleStatus'" :color="record.saleStatus === 'AVAILABLE' ? 'green' : 'default'">{{ record.saleStatus }}</Tag>
            <Space v-else-if="column.key === 'actions'">
              <Button type="link" @click="edit(record)">编辑</Button>
              <Button type="link" @click="router.push({ path: '/merchant/price-audits', query: { productId: String(record.id) } })">价格审计</Button>
              <Button type="link" @click="router.push({ path: '/merchant/reviews', query: { productId: String(record.id) } })">评价</Button>
              <Button type="link" @click="changeStatus(record)">{{ record.saleStatus === 'AVAILABLE' ? '下架' : '上架' }}</Button>
            </Space>
          </template>
        </Table>
      </section>
    </div>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.form-pair { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.full { width: 100%; }
.notice { margin-bottom: 16px; }
@media (max-width: 640px) { .form-pair { grid-template-columns: 1fr; gap: 0; } }
</style>
