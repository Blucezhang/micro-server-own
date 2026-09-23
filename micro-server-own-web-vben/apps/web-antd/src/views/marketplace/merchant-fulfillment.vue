<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { Button, Card, Form, Input, Space, Table, Tag } from 'ant-design-vue';
import { merchantApi } from '#/api/marketplace';

const loading = ref(false);
const rows = ref<Record<string, any>[]>([]);
const form = reactive({ logisticsCompany: '', subOrderNo: '', trackingNo: '' });
const columns = [{ dataIndex: 'subOrderNo', key: 'subOrderNo', title: '子订单号' }, { dataIndex: 'status', key: 'status', title: '状态' }, { dataIndex: 'amount', key: 'amount', title: '金额' }, { dataIndex: 'createdAt', key: 'createdAt', title: '创建时间' }];
function unwrap(data: any) { return Array.isArray(data) ? data : data?.items || []; }
async function load() { loading.value = true; try { rows.value = unwrap(await merchantApi.subOrders({ page: 0, size: 20 })); } finally { loading.value = false; } }
async function ship() { loading.value = true; try { await merchantApi.ship(form.subOrderNo, { logisticsCompany: form.logisticsCompany, trackingNo: form.trackingNo }); await load(); } finally { loading.value = false; } }
onMounted(load);
</script>
<template><main class="page"><Card :bordered="false"><Tag color="purple">商家运营</Tag><h1>发货履约</h1><p>填写物流信息后提交；状态流转由订单服务执行。</p></Card><Card :bordered="false" title="待发货订单"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="false" row-key="subOrderNo" /></Card><Card :bordered="false" title="录入物流"><Form layout="vertical" @finish="ship"><Form.Item label="子订单号" required><Input v-model:value="form.subOrderNo" /></Form.Item><Form.Item label="物流公司" required><Input v-model:value="form.logisticsCompany" /></Form.Item><Form.Item label="物流单号" required><Input v-model:value="form.trackingNo" /></Form.Item><Space><Button :loading="loading" html-type="submit" type="primary">确认发货</Button><Button @click="load">刷新订单</Button></Space></Form></Card></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}@media(max-width:640px){.page{padding:16px}}</style>
