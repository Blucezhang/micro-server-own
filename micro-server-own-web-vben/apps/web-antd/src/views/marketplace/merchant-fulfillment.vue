<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { Button, Card, Form, Input, Space, Table, Tag } from 'ant-design-vue';
import { merchantApi } from '#/api/marketplace';

const loading = ref(false);
const rows = ref<Record<string, any>[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const form = reactive({ logisticsCompany: '', subOrderNo: '', trackingNo: '' });
const columns = [{ dataIndex: 'subOrderNo', key: 'subOrderNo', title: '子订单号' }, { dataIndex: 'status', key: 'status', title: '状态' }, { dataIndex: 'amount', key: 'amount', title: '金额' }, { dataIndex: 'createdAt', key: 'createdAt', title: '创建时间' }];
async function load() { loading.value = true; try { const result = await merchantApi.subOrders({ page: page.value, size: pageSize, status: 'TO_SHIP' }) as any; rows.value = result.items || []; total.value = Number(result.total || 0); } finally { loading.value = false; } }
async function changePage(pagination: { current?: number }) { page.value = (pagination.current || 1) - 1; await load(); }
async function ship() { loading.value = true; try { await merchantApi.ship(form.subOrderNo, { logisticsCompany: form.logisticsCompany, trackingNo: form.trackingNo }); await load(); } finally { loading.value = false; } }
onMounted(load);
</script>
<template><main class="page"><Card :bordered="false"><Tag color="purple">商家运营</Tag><h1>发货履约</h1><p>填写物流信息后提交；状态流转由订单服务执行。</p></Card><Card :bordered="false" title="待发货订单"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" row-key="subOrderNo" @change="changePage" /></Card><Card :bordered="false" title="录入物流"><Form layout="vertical" @finish="ship"><Form.Item label="子订单号" required><Input v-model:value="form.subOrderNo" /></Form.Item><Form.Item label="物流公司" required><Input v-model:value="form.logisticsCompany" /></Form.Item><Form.Item label="物流单号" required><Input v-model:value="form.trackingNo" /></Form.Item><Space><Button :loading="loading" html-type="submit" type="primary">确认发货</Button><Button @click="load">刷新订单</Button></Space></Form></Card></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}@media(max-width:640px){.page{padding:16px}}</style>
