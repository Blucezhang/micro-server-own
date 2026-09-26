<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { Button, Card, Descriptions, Form, Input, Popconfirm, Select, Space, Table, Tag } from 'ant-design-vue';
import { buyerApi, paymentApi } from '#/api/marketplace';

const loading = ref(false);
const rows = ref<Record<string, any>[]>([]);
const detail = ref<{ subOrders: { status: string; subOrderNo: string }[] }>();
const payment = ref<Record<string, any>>();
const selectedOrderNo = ref('');
const paymentNo = ref('');
const payForm = reactive({ channel: 'MOCK', orderNo: '' });
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const columns = computed(() => [...Object.keys(rows.value[0] || {}).slice(0, 6).map((key) => ({ dataIndex: key, key, title: key })), { key: 'actions', title: '操作' }]);
const subOrderColumns = [{ dataIndex: 'subOrderNo', key: 'subOrderNo', title: '子订单号' }, { dataIndex: 'status', key: 'status', title: '状态' }, { key: 'actions', title: '操作' }];
async function load() { loading.value = true; try { const result = await buyerApi.orders({ page: page.value, size: pageSize }) as any; rows.value = result.items || []; total.value = Number(result.total || 0); } finally { loading.value = false; } }
async function onPageChange(pagination: { current?: number }) { page.value = (pagination.current || 1) - 1; await load(); }
async function openDetail(no: string) { loading.value = true; try { selectedOrderNo.value = no; detail.value = await buyerApi.orderDetail(no) as { subOrders: { status: string; subOrderNo: string }[] }; payForm.orderNo = no; } finally { loading.value = false; } }
async function cancel(no: string) { loading.value = true; try { await buyerApi.cancelOrder(no); await load(); } finally { loading.value = false; } }
async function receive(subOrderNo: string) { loading.value = true; try { await buyerApi.receiveSubOrder(subOrderNo); await openDetail(selectedOrderNo.value); await load(); } finally { loading.value = false; } }
async function createPayment() { loading.value = true; try { payment.value = await paymentApi.create(payForm) as Record<string, any>; paymentNo.value = String(payment.value.paymentNo || ''); } finally { loading.value = false; } }
async function queryPayment() { if (!paymentNo.value) return; loading.value = true; try { payment.value = await paymentApi.detail(paymentNo.value) as Record<string, any>; } finally { loading.value = false; } }
onMounted(load);
</script>
<template>
  <main class="page">
    <Card :bordered="false"><Tag color="cyan">买家中心</Tag><h1>订单、物流与支付</h1><p>支付仅查询模拟支付单状态；不调用服务端的模拟成功接口。</p></Card>
    <Card :bordered="false" title="我的订单">
      <Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" row-key="orderNo" @change="onPageChange">
        <template #bodyCell="{ column, record }"><Space v-if="column.key === 'actions'"><Button type="link" @click="openDetail(record.orderNo)">详情</Button><Popconfirm title="确认取消订单？" @confirm="cancel(record.orderNo)"><Button danger type="link">取消</Button></Popconfirm></Space></template>
      </Table>
    </Card>
    <Card v-if="detail" :bordered="false" title="订单详情">
      <Descriptions :column="1" bordered><Descriptions.Item label="订单号">{{ selectedOrderNo }}</Descriptions.Item><Descriptions.Item label="详情"><pre>{{ JSON.stringify(detail, null, 2) }}</pre></Descriptions.Item></Descriptions>
      <Form layout="inline" class="actions" @finish="createPayment"><Form.Item label="支付渠道"><Select v-model:value="payForm.channel" :options="[{label:'模拟支付',value:'MOCK'}]" style="width:120px" /></Form.Item><Button html-type="submit" type="primary">创建支付单</Button></Form>
      <Form layout="inline" class="actions" @finish="queryPayment"><Form.Item label="支付单号"><Input v-model:value="paymentNo" /></Form.Item><Button html-type="submit">查询支付结果</Button></Form><pre v-if="payment">{{ JSON.stringify(payment, null, 2) }}</pre>
    </Card>
    <Card v-if="detail" :bordered="false" title="签收子订单">
      <Table :columns="subOrderColumns" :data-source="detail.subOrders" :pagination="false" row-key="subOrderNo">
        <template #bodyCell="{ column, record }"><Popconfirm v-if="column.key === 'actions' && record.status === 'SHIPPED'" title="确认已收到商品？" @confirm="receive(record.subOrderNo)"><Button>确认签收</Button></Popconfirm></template>
      </Table>
    </Card>
  </main>
</template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}.actions{margin-top:16px}pre{background:#f8fafc;border-radius:8px;max-height:280px;overflow:auto;padding:12px;white-space:pre-wrap}@media(max-width:640px){.page{padding:16px}}</style>
