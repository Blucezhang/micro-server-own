<script lang="ts" setup>
import { computed, ref } from 'vue';
import { Button, Card, Form, InputNumber, Table, Tag } from 'ant-design-vue';
import { merchantApi } from '#/api/marketplace';
const loading = ref(false); const productId = ref<number>(); const rows = ref<Record<string, any>[]>([]); const page = ref(0); const total = ref(0); const pageSize = 20;
const columns = computed(() => Object.keys(rows.value[0] || {}).map((key) => ({ dataIndex: key, key, title: key })));
async function load() { if (!productId.value) return; loading.value = true; try { const result = await merchantApi.priceAudits(productId.value, page.value) as any; rows.value = result.items || []; total.value = Number(result.total || 0); } finally { loading.value = false; } }
async function search() { page.value = 0; await load(); }
async function changePage(pagination: { current?: number }) { page.value = (pagination.current || 1) - 1; await load(); }
</script>
<template><main class="page"><Card :bordered="false"><Tag color="purple">商家运营</Tag><h1>商品价格审计</h1><p>查看商品价格变更前后数值及提交时记录的原因。</p></Card><Card :bordered="false"><Form layout="inline" @finish="search"><Form.Item label="商品 ID" required><InputNumber v-model:value="productId" /></Form.Item><Button html-type="submit" type="primary">查询审计记录</Button></Form></Card><Card :bordered="false"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" row-key="id" @change="changePage" /></Card></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}@media(max-width:640px){.page{padding:16px}}</style>
