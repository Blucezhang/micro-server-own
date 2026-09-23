<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { Button, Card, Drawer, Form, Input, Select, Space, Table, Tag } from 'ant-design-vue';
import { cartApi, catalogApi } from '#/api/marketplace';

const loading = ref(false);
const rows = ref<Record<string, any>[]>([]);
const categories = ref<Record<string, any>[]>([]);
const detail = ref<Record<string, any>>();
const filters = reactive({ categoryId: undefined as number | undefined, keyword: '', page: 0, size: 20 });
const columns = computed(() => [...Object.keys(rows.value[0] || {}).slice(0, 6).map((key) => ({ dataIndex: key, key, title: key })), { key: 'actions', title: '操作' }]);
function unwrap(data: any) { return Array.isArray(data) ? data : data?.items || []; }
async function load() { loading.value = true; try { rows.value = unwrap(await catalogApi.products(filters)); } finally { loading.value = false; } }
async function openDetail(row: Record<string, any>) { loading.value = true; try { detail.value = await catalogApi.product(Number(row.id)) as Record<string, any>; } finally { loading.value = false; } }
async function add(row: Record<string, any>) { loading.value = true; try { await cartApi.add(Number(row.id), 1); } finally { loading.value = false; } }
onMounted(async () => { categories.value = await catalogApi.categories() as Record<string, any>[]; await load(); });
</script>
<template><main class="page"><Card :bordered="false"><Tag color="cyan">买家中心</Tag><h1>商品浏览</h1><p>仅展示当前可售商品，价格与库存以提交订单时的服务端校验为准。</p></Card><Card :bordered="false"><Form layout="inline" @finish="load"><Form.Item label="关键词"><Input v-model:value="filters.keyword" allow-clear /></Form.Item><Form.Item label="分类"><Select v-model:value="filters.categoryId" allow-clear :field-names="{label:'name',value:'id'}" :options="categories" style="width:160px" /></Form.Item><Space><Button html-type="submit" type="primary">搜索</Button><Button @click="Object.assign(filters,{categoryId:undefined,keyword:''});load()">重置</Button></Space></Form></Card><Card :bordered="false"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="false" row-key="id"><template #bodyCell="{ column, record }"><Space v-if="column.key === 'actions'"><Button type="link" @click="openDetail(record)">详情</Button><Button type="link" @click="add(record)">加入购物车</Button></Space></template></Table></Card><Drawer :open="!!detail" title="商品详情" width="560" @close="detail=undefined"><pre v-if="detail">{{ JSON.stringify(detail, null, 2) }}</pre></Drawer></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}pre{background:#f8fafc;border-radius:8px;padding:12px;white-space:pre-wrap}@media(max-width:640px){.page{padding:16px}}</style>
