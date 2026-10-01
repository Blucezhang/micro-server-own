<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';

import { Alert, Button, Form, Input, Space, Table, Tag } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

type SubOrder = {
  amount?: number | string;
  createdAt?: number | string;
  status?: string;
  subOrderNo: string;
};

const loading = ref(false);
const rows = ref<SubOrder[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const form = reactive({ logisticsCompany: '', subOrderNo: '', trackingNo: '' });
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const listError = ref('');

const columns = [
  { dataIndex: 'subOrderNo', key: 'subOrderNo', title: '子订单号' },
  { dataIndex: 'status', key: 'status', title: '状态' },
  { dataIndex: 'amount', key: 'amount', title: '金额' },
  { key: 'createdAt', title: '创建时间' },
];

function message(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

function formatDate(value?: number | string) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

async function load() {
  loading.value = true;
  listError.value = '';
  try {
    const result = (await merchantApi.subOrders({ page: page.value, size: pageSize, status: 'TO_SHIP' })) as {
      items?: SubOrder[];
      total?: number;
    };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (error) {
    listError.value = message(error, '待发货订单加载失败');
  } finally {
    loading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

async function ship() {
  if (loading.value) return;
  const subOrderNo = form.subOrderNo.trim();
  const logisticsCompany = form.logisticsCompany.trim();
  const trackingNo = form.trackingNo.trim();
  feedback.value = null;
  if (!subOrderNo || !logisticsCompany || !trackingNo) {
    feedback.value = { text: '请填写子订单号、物流公司和物流单号', type: 'error' };
    return;
  }
  loading.value = true;
  try {
    await merchantApi.ship(subOrderNo, { logisticsCompany, trackingNo });
    Object.assign(form, { logisticsCompany: '', subOrderNo: '', trackingNo: '' });
    feedback.value = { text: '发货信息已提交，订单状态由服务端更新。', type: 'success' };
    await load();
  } catch (error) {
    feedback.value = { text: message(error, '发货失败，请稍后重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · FULFILLMENT</span>
        <h1 class="market-heading">发货履约</h1>
        <p class="market-subtitle">仅展示待发货子订单。录入物流信息后，订单服务负责状态流转与追踪记录。</p>
      </div>
      <Button :loading="loading" @click="load">刷新订单</Button>
    </header>

    <section class="market-panel" aria-labelledby="to-ship-title">
      <h2 id="to-ship-title" class="market-panel-title">待发货订单</h2>
      <Alert v-if="listError" :message="listError" class="notice" show-icon type="error" />
      <Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" :scroll="{ x: 680 }" row-key="subOrderNo" @change="changePage">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'createdAt'">{{ formatDate(record.createdAt) }}</template>
          <Tag v-else-if="column.key === 'status'" color="gold">{{ record.status }}</Tag>
        </template>
      </Table>
    </section>

    <section class="market-panel" aria-labelledby="shipment-title">
      <h2 id="shipment-title" class="market-panel-title">录入物流</h2>
      <Form :disabled="loading" :model="form" class="shipment-form" layout="vertical" @finish="ship">
        <Form.Item label="子订单号" name="subOrderNo" :rules="[{ required: true, message: '请填写子订单号' }]">
          <Input v-model:value="form.subOrderNo" />
        </Form.Item>
        <div class="form-pair">
          <Form.Item label="物流公司" name="logisticsCompany" :rules="[{ required: true, message: '请填写物流公司' }]">
            <Input v-model:value="form.logisticsCompany" />
          </Form.Item>
          <Form.Item label="物流单号" name="trackingNo" :rules="[{ required: true, message: '请填写物流单号' }]">
            <Input v-model:value="form.trackingNo" />
          </Form.Item>
        </div>
        <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="notice" show-icon />
        <Space><Button :loading="loading" html-type="submit" type="primary">确认发货</Button></Space>
      </Form>
    </section>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.shipment-form { max-width: 680px; }
.form-pair { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.notice { margin-bottom: 16px; }
@media (max-width: 640px) { .form-pair { grid-template-columns: 1fr; gap: 0; } }
</style>
