<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useMediaQuery } from '@vueuse/core';
import type { ColumnType } from 'ant-design-vue/es/table';
import { Alert, Button, Drawer, Empty, Form, Input, Pagination, Space, Table, Tag } from 'ant-design-vue';
import { merchantApi } from '#/api/marketplace';

type SubOrder = {
  amount?: number | string;
  createdAt?: number | string;
  status?: string;
  subOrderNo: string;
};

const isMobile = useMediaQuery('(max-width: 768px)');
const listLoading = ref(false);
const actionLoading = ref(false);
const rows = ref<SubOrder[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;

const shippingOpen = ref(false);
const form = reactive({ logisticsCompany: '', subOrderNo: '', trackingNo: '' });
const actionFeedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const drawerFeedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const listError = ref('');

const drawerWidth = computed(() => (isMobile.value ? '100%' : 520));

const columns: ColumnType[] = [
  { dataIndex: 'subOrderNo', key: 'subOrderNo', title: '子订单号', minWidth: 220 },
  { dataIndex: 'status', key: 'status', title: '履约状态', width: 120 },
  { dataIndex: 'amount', key: 'amount', title: '订单金额', width: 140 },
  { key: 'createdAt', title: '创建时间', width: 180 },
  { key: 'actions', title: '操作', width: 110, fixed: 'right' as const },
];

function message(error: unknown, fallback: string) {
  if (!(error instanceof Error) || /^request failed with status code \d{3}$/i.test(error.message)) {
    return fallback;
  }
  return error.message;
}

function formatDate(value?: number | string) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

function formatAmount(value?: number | string) {
  if (value === undefined || value === null || value === '') return '—';
  const amount = Number(value);
  return Number.isFinite(amount) ? `¥${amount.toFixed(2)}` : '—';
}

function openShipping(subOrderNo: string) {
  Object.assign(form, { logisticsCompany: '', subOrderNo, trackingNo: '' });
  drawerFeedback.value = null;
  shippingOpen.value = true;
}

function closeShipping() {
  shippingOpen.value = false;
  drawerFeedback.value = null;
  Object.assign(form, { logisticsCompany: '', subOrderNo: '', trackingNo: '' });
}

async function load(clearActionFeedback = false) {
  listLoading.value = true;
  listError.value = '';
  if (clearActionFeedback) actionFeedback.value = null;
  try {
    const result = (await merchantApi.subOrders({ page: page.value, size: pageSize, status: 'TO_SHIP' })) as {
      items?: SubOrder[];
      total?: number;
    };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (error) {
    listError.value = message(error, '服务暂未响应，请稍后重试');
  } finally {
    listLoading.value = false;
  }
}

async function changePage(pagination: number | { current?: number }) {
  const targetPage = typeof pagination === 'number' ? pagination : (pagination.current || 1);
  page.value = targetPage - 1;
  await load();
}

async function ship() {
  if (actionLoading.value) return;
  const subOrderNo = form.subOrderNo.trim();
  const logisticsCompany = form.logisticsCompany.trim();
  const trackingNo = form.trackingNo.trim();
  drawerFeedback.value = null;
  if (!subOrderNo || !logisticsCompany || !trackingNo) {
    drawerFeedback.value = { text: '请填写子订单号、物流公司和物流单号', type: 'error' };
    return;
  }
  actionLoading.value = true;
  try {
    await merchantApi.ship(subOrderNo, { logisticsCompany, trackingNo });
    closeShipping();
    actionFeedback.value = { text: '发货信息已提交，订单状态由服务端更新。', type: 'success' };
    await load();
  } catch (error) {
    drawerFeedback.value = { text: message(error, '发货失败，请稍后重试'), type: 'error' };
  } finally {
    actionLoading.value = false;
  }
}

onMounted(() => load());
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div class="market-head-text">
        <div class="head-title-row">
          <h1 class="market-heading">待发货订单</h1>
          <span v-if="!listError && total > 0" class="catalog-badge">
            共 {{ total }} 笔待发货
          </span>
        </div>
        <p class="market-subtitle">核对待发货订单并录入物流承运单号，提交后即进入物流履约链路。</p>
      </div>
      <div class="market-head-actions">
        <Button :loading="listLoading" @click="load(true)">刷新订单</Button>
      </div>
    </header>

    <Alert
      v-if="actionFeedback"
      :message="actionFeedback.text"
      :type="actionFeedback.type"
      class="action-alert"
      closable
      show-icon
      @close="actionFeedback = null"
    />

    <section class="market-panel fulfillment-panel" aria-label="待发货订单列表">
      <div v-if="listError" class="catalog-error-row" role="alert">
        <div class="error-main">
          <span class="error-badge">加载失败</span>
          <span class="error-detail">{{ listError }}</span>
        </div>
        <Button :loading="listLoading" size="small" type="primary" @click="load(true)">重新加载</Button>
      </div>

      <template v-else>
        <div v-if="!isMobile" class="table-container">
          <Table
            :columns="columns"
            :data-source="rows"
            :loading="listLoading"
            :pagination="total > pageSize ? { current: page + 1, pageSize, total, showSizeChanger: false } : false"
            row-key="subOrderNo"
            @change="changePage"
          >
            <template #emptyText>
              <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无待发货订单" />
            </template>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'subOrderNo'">
                <span class="mono-code">{{ record.subOrderNo }}</span>
              </template>
              <template v-else-if="column.key === 'createdAt'">
                <span class="date-text">{{ formatDate(record.createdAt) }}</span>
              </template>
              <template v-else-if="column.key === 'amount'">
                <span class="order-amount">{{ formatAmount(record.amount) }}</span>
              </template>
              <template v-else-if="column.key === 'status'">
                <Tag color="gold">待发货</Tag>
              </template>
              <template v-else-if="column.key === 'actions'">
                <Button size="small" type="primary" @click="openShipping(String(record.subOrderNo))">
                  录入物流
                </Button>
              </template>
            </template>
          </Table>
        </div>

        <div v-else class="mobile-card-list">
          <div v-if="listLoading" class="mobile-loading-tip">正在载入订单数据...</div>
          <div v-else-if="rows.length === 0" class="mobile-empty">
            <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无待发货订单" />
          </div>
          <article v-for="item in (listLoading ? [] : rows)" :key="item.subOrderNo" class="mobile-card">
            <div class="mobile-card-top">
              <span class="mono-code">{{ item.subOrderNo }}</span>
              <Tag color="gold">待发货</Tag>
            </div>
            <div class="mobile-card-mid">
              <div class="mobile-field">
                <span class="field-label">订单金额</span>
                <span class="order-amount">{{ formatAmount(item.amount) }}</span>
              </div>
              <div class="mobile-field" style="text-align: right">
                <span class="field-label">下单时间</span>
                <span class="date-text">{{ formatDate(item.createdAt) }}</span>
              </div>
            </div>
            <div class="mobile-card-bot">
              <span />
              <Button size="small" type="primary" @click="openShipping(String(item.subOrderNo))">
                录入物流
              </Button>
            </div>
          </article>
          <div v-if="total > pageSize" class="mobile-pagination-wrap">
            <Pagination :current="page + 1" :page-size="pageSize" :total="total" size="small" simple @change="changePage" />
          </div>
        </div>
      </template>
    </section>

    <!-- 录入物流抽屉 -->
    <Drawer
      v-model:open="shippingOpen"
      :closable="false"
      :width="drawerWidth"
      destroy-on-close
      placement="right"
      @close="closeShipping"
    >
      <template #title>
        <div class="editor-heading">
          <h2>录入物流</h2>
          <p>请核对承运公司与面单号，确认后提交至履约系统。</p>
        </div>
      </template>
      <Form :disabled="actionLoading" :model="form" layout="vertical" @finish="ship">
        <Form.Item label="子订单号" name="subOrderNo">
          <Input v-model:value="form.subOrderNo" disabled />
        </Form.Item>
        <Form.Item label="物流公司" name="logisticsCompany" :rules="[{ required: true, message: '请填写物流公司' }]">
          <Input v-model:value="form.logisticsCompany" placeholder="例如：顺丰速运、中通快递" />
        </Form.Item>
        <Form.Item label="物流单号" name="trackingNo" :rules="[{ required: true, message: '请填写物流单号' }]">
          <Input v-model:value="form.trackingNo" placeholder="请输入物流快递单号" />
        </Form.Item>
        <Alert v-if="drawerFeedback?.type === 'error'" :message="drawerFeedback.text" class="notice" show-icon type="error" />
        <div class="editor-footer">
          <Space>
            <Button :disabled="actionLoading" @click="closeShipping">取消</Button>
            <Button :loading="actionLoading" html-type="submit" type="primary">确认发货</Button>
          </Space>
        </div>
      </Form>
    </Drawer>
  </main>
</template>

<style scoped src="./merchant-fulfillment.css"></style>
