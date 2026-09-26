<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { Button, Card, Form, Input, InputNumber, Select, Space, Table, Tag } from 'ant-design-vue';
import { buyerApi } from '#/api/marketplace';

const loading = ref(false);
const rows = ref<Record<string, any>[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const form = reactive({ orderItemId: undefined as number | undefined, quantity: 1, reason: '', subOrderNo: '', type: 'REFUND' });
const shipment = reactive({ afterSaleNo: '', logisticsCompany: '', trackingNo: '' });
const columns = computed(() => Object.keys(rows.value[0] || {}).slice(0, 7).map((key) => ({ dataIndex: key, key, title: key })));
async function load() { loading.value = true; try { const result = await buyerApi.afterSales({ page: page.value, size: pageSize }) as any; rows.value = result.items || []; total.value = Number(result.total || 0); } finally { loading.value = false; } }
async function changePage(pagination: { current?: number }) { page.value = (pagination.current || 1) - 1; await load(); }
async function submit() { loading.value = true; try { await buyerApi.createAfterSale({ reason: form.reason, subOrderNo: form.subOrderNo, type: form.type, items: [{ orderItemId: form.orderItemId, quantity: form.quantity }] }); await load(); } finally { loading.value = false; } }
async function returnShipment() { loading.value = true; try { await buyerApi.returnShipment(shipment.afterSaleNo, { logisticsCompany: shipment.logisticsCompany, trackingNo: shipment.trackingNo }); await load(); } finally { loading.value = false; } }
onMounted(load);
</script>
<template><main class="page"><Card :bordered="false"><Tag color="cyan">买家中心</Tag><h1>售后服务</h1><p>提交申请后，售后状态和退款处理以订单服务为准。</p></Card><Card :bordered="false" title="我的售后"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" row-key="afterSaleNo" @change="changePage" /></Card><Card :bordered="false" title="申请售后"><Form layout="vertical" @finish="submit"><Form.Item label="子订单号" required><Input v-model:value="form.subOrderNo" /></Form.Item><Form.Item label="售后类型" required><Select v-model:value="form.type" :options="[{ label: '退款', value: 'REFUND' }, { label: '退货退款', value: 'RETURN_REFUND' }, { label: '换货', value: 'EXCHANGE' }]" /></Form.Item><Form.Item label="订单明细 ID" required><InputNumber v-model:value="form.orderItemId" class="full" /></Form.Item><Form.Item label="数量" required><InputNumber v-model:value="form.quantity" :min="1" class="full" /></Form.Item><Form.Item label="申请原因" required><Input v-model:value="form.reason" /></Form.Item><Space><Button :loading="loading" html-type="submit" type="primary">提交申请</Button><Button @click="load">刷新</Button></Space></Form></Card><Card :bordered="false" title="退货物流"><Form layout="vertical" @finish="returnShipment"><Form.Item label="售后单号" required><Input v-model:value="shipment.afterSaleNo" /></Form.Item><Form.Item label="物流公司" required><Input v-model:value="shipment.logisticsCompany" /></Form.Item><Form.Item label="物流单号" required><Input v-model:value="shipment.trackingNo" /></Form.Item><Button :loading="loading" html-type="submit">提交退货物流</Button></Form></Card></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}.full{width:100%}@media(max-width:640px){.page{padding:16px}}</style>
