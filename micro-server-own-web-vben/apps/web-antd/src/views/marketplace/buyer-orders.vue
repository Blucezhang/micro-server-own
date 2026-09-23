<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { Button, Card, Descriptions, Form, Input, Popconfirm, Select, Space, Table, Tag } from 'ant-design-vue';
import { buyerApi, paymentApi } from '#/api/marketplace';

const loading = ref(false);
const rows = ref<Record<string, any>[]>([]);
const detail = ref<Record<string, any>>();
const payment = ref<Record<string, any>>();
const selectedOrderNo = ref('');
const paymentNo = ref('');
const payForm = reactive({ channel: 'MOCK', orderNo: '' });
const columns = computed(() => [...Object.keys(rows.value[0] || {}).slice(0, 6).map((key) => ({ dataIndex: key, key, title: key })), { key: 'actions', title: '操作' }]);
function unwrap(data: any) { return Array.isArray(data) ? data : data?.items || []; }
async function load() { loading.value = true; try { rows.value = unwrap(await buyerApi.orders({ page: 0, size: 20 })); } finally { loading.value = false; } }
async function openDetail(no: string) { loading.value = true; try { selectedOrderNo.value = no; detail.value = await buyerApi.orderDetail(no) as Record<string, any>; payForm.orderNo = no; } finally { loading.value = false; } }
async function cancel(no: string) { loading.value = true; try { await buyerApi.cancelOrder(no); await load(); } finally { loading.value = false; } }
async function receive(no: string) { loading.value = true; try { await buyerApi.receiveSubOrder(no); await openDetail(selectedOrderNo.value); } finally { loading.value = false; } }
async function createPayment() { loading.value = true; try { payment.value = await paymentApi.create(payForm) as Record<string, any>; paymentNo.value = String(payment.value.paymentNo || ''); } finally { loading.value = false; } }
async function queryPayment() { if (!paymentNo.value) return; loading.value = true; try { payment.value = await paymentApi.detail(paymentNo.value) as Record<string, any>; } finally { loading.value = false; } }
onMounted(load);
</script>
<template><main class="page"><Card :bordered="false"><Tag color="cyan">买家中心</Tag><h1>订单、物流与支付</h1><p>支付仅查询模拟支付单状态；不调用服务端的模拟成功接口。</p></Card><Card :bordered="false" title="我的订单"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="false" row-key="orderNo"><template #bodyCell="{ column, record }"><Space v-if="column.key === 'actions'"><Button type="link" @click="openDetail(record.orderNo)">详情</Button><Popconfirm title="确认取消订单？" @confirm="cancel(record.orderNo)"><Button danger type="link">取消</Button></Popconfirm></Space></template></Table></Card><Card v-if="detail" :bordered="false" title="订单详情"><Descriptions :column="1" bordered><Descriptions.Item label="订单号">{{ selectedOrderNo }}</Descriptions.Item><Descriptions.Item label="详情"><pre>{{ JSON.stringify(detail, null, 2) }}</pre></Descriptions.Item></Descriptions><Form layout="inline" class="actions" @finish="createPayment"><Form.Item label="支付渠道"><Select v-model:value="payForm.channel" :options="[{label:'模拟支付',value:'MOCK'}]" style="width:120px" /></Form.Item><Button html-type="submit" type="primary">创建支付单</Button></Form><Form layout="inline" class="actions" @finish="queryPayment"><Form.Item label="支付单号"><Input v-model:value="paymentNo" /></Form.Item><Button html-type="submit">查询支付结果</Button></Form><pre v-if="payment">{{ JSON.stringify(payment, null, 2) }}</pre></Card><Card v-if="detail" :bordered="false" title="签收子订单"><Form layout="inline" @finish="receive(selectedOrderNo)"><Form.Item label="子订单号"><Input :value="selectedOrderNo" disabled /></Form.Item><Popconfirm title="确认已收到商品？" @confirm="receive(selectedOrderNo)"><Button>确认签收</Button></Popconfirm></Form></Card></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}.actions{margin-top:16px}pre{background:#f8fafc;border-radius:8px;max-height:280px;overflow:auto;padding:12px;white-space:pre-wrap}@media(max-width:640px){.page{padding:16px}}</style>
