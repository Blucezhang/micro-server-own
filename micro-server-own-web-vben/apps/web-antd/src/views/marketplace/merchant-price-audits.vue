<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Alert, Button, Form, InputNumber, Table } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

type AuditRow = Record<string, null | number | string | undefined>;

const route = useRoute();
const loading = ref(false);
const rows = ref<AuditRow[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const error = ref('');
const query = reactive({ productId: Number(route.query.productId) || undefined as number | undefined });
const columns = computed(() => Object.keys(rows.value[0] || {}).map((key) => ({ dataIndex: key, key, title: key })));

function message(reason: unknown, fallback: string) {
  return reason instanceof Error ? reason.message : fallback;
}

async function load() {
  if (!query.productId) return;
  loading.value = true;
  error.value = '';
  try {
    const result = (await merchantApi.priceAudits(query.productId, page.value)) as { items?: AuditRow[]; total?: number };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (reason) {
    error.value = message(reason, '价格审计记录加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function search() {
  page.value = 0;
  await load();
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

onMounted(() => {
  if (query.productId) void search();
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · AUDIT</span>
        <h1 class="market-heading">商品价格审计</h1>
        <p class="market-subtitle">查看商品的价格变更前后数值，以及提交变更时记录的原因。</p>
      </div>
    </header>

    <section class="market-panel" aria-labelledby="audit-query-title">
      <h2 id="audit-query-title" class="market-panel-title">查询记录</h2>
      <Form :disabled="loading" :model="query" class="query-form" layout="inline" @finish="search">
        <Form.Item label="商品 ID" name="productId" :rules="[{ required: true, message: '请填写商品 ID' }]">
          <InputNumber v-model:value="query.productId" />
        </Form.Item>
        <Form.Item><Button :loading="loading" html-type="submit" type="primary">查询审计记录</Button></Form.Item>
      </Form>
    </section>

    <section class="market-panel" aria-labelledby="audit-list-title">
      <h2 id="audit-list-title" class="market-panel-title">变更记录</h2>
      <Alert v-if="error" :message="error" class="notice" show-icon type="error" />
      <Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" :scroll="{ x: 760 }" row-key="id" @change="changePage" />
    </section>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.query-form { align-items: flex-end; }
.notice { margin-bottom: 16px; }
</style>
