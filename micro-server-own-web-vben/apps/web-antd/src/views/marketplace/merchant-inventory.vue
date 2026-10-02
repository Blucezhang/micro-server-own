<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';

import { Alert, Button, Descriptions, Form, Input, InputNumber, Select, Switch, Table } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

type Stock = Record<string, boolean | null | number | string | undefined>;

const loading = ref(false);
const alerts = ref<Stock[]>([]);
const stock = ref<Stock>();
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const query = reactive({ merchantId: undefined as number | undefined, productId: undefined as number | undefined });
const adjust = reactive({ adjustmentType: 'INCREASE', quantity: undefined as number | undefined, reason: '' });
const rule = reactive({ enabled: true, thresholdQuantity: undefined as number | undefined });

const stockItems = computed(() => Object.entries(stock.value || {}).map(([label, value]) => ({ label, value: value == null ? '—' : String(value) })));
const alertColumns = computed(() => Object.keys(alerts.value[0] || {}).map((key) => ({ dataIndex: key, key, title: key })));

function message(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

function hasProduct() {
  return Number.isSafeInteger(query.productId) && !!query.productId;
}

function hasScope() {
  return hasProduct() && Number.isSafeInteger(query.merchantId) && !!query.merchantId;
}

async function loadAlerts() {
  loading.value = true;
  try {
    alerts.value = (await merchantApi.lowStockAlerts()) as Stock[];
  } catch (error) {
    feedback.value = { text: message(error, '低库存提醒加载失败，请刷新重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function loadStock() {
  if (!hasScope()) {
    feedback.value = { text: '请先填写商品 ID 和商家 ID', type: 'error' };
    return;
  }
  loading.value = true;
  feedback.value = null;
  try {
    stock.value = (await merchantApi.stock(query.productId!, query.merchantId!)) as Stock;
  } catch (error) {
    feedback.value = { text: message(error, '库存加载失败，请稍后重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function submitAdjust() {
  if (!hasScope() || !adjust.quantity || !adjust.reason.trim()) {
    feedback.value = { text: '请先查询库存，再填写数量和调整原因', type: 'error' };
    return;
  }
  loading.value = true;
  feedback.value = null;
  try {
    await merchantApi.adjustStock(query.productId!, { ...adjust, merchantId: query.merchantId, reason: adjust.reason.trim() });
    feedback.value = { text: '库存调整已提交', type: 'success' };
    await Promise.all([loadStock(), loadAlerts()]);
  } catch (error) {
    feedback.value = { text: message(error, '库存调整失败，请稍后重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function submitRule() {
  if (!hasProduct() || rule.thresholdQuantity == null) {
    feedback.value = { text: '请填写商品 ID 和低库存阈值', type: 'error' };
    return;
  }
  loading.value = true;
  feedback.value = null;
  try {
    await merchantApi.saveLowStockRule(query.productId!, rule);
    feedback.value = { text: '低库存规则已保存', type: 'success' };
    await loadAlerts();
  } catch (error) {
    feedback.value = { text: message(error, '规则保存失败，请稍后重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

onMounted(loadAlerts);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · INVENTORY</span>
        <h1 class="market-heading">库存管理</h1>
        <p class="market-subtitle">查询库存、登记出入库并维护低库存预警；所有库存数值以库存服务结果为准。</p>
      </div>
      <Button :loading="loading" @click="loadAlerts">刷新预警</Button>
    </header>

    <section class="market-panel" aria-labelledby="stock-query-title">
      <h2 id="stock-query-title" class="market-panel-title">查询库存</h2>
      <Form :disabled="loading" :model="query" class="query-form" layout="inline" @finish="loadStock">
        <Form.Item label="商品 ID" name="productId" :rules="[{ required: true, message: '请填写商品 ID' }]">
          <InputNumber v-model:value="query.productId" />
        </Form.Item>
        <Form.Item label="商家 ID" name="merchantId" :rules="[{ required: true, message: '请填写商家 ID' }]">
          <InputNumber v-model:value="query.merchantId" />
        </Form.Item>
        <Form.Item><Button :loading="loading" html-type="submit" type="primary">查询</Button></Form.Item>
      </Form>
      <Descriptions v-if="stockItems.length" :column="{ sm: 1, xl: 2 }" :items="stockItems" class="stock-result" bordered size="small" />
    </section>

    <div class="market-grid">
      <section class="market-panel" aria-labelledby="adjust-title">
        <h2 id="adjust-title" class="market-panel-title">调整库存</h2>
        <Form :disabled="loading" :model="adjust" layout="vertical" @finish="submitAdjust">
          <Form.Item label="调整类型"><Select v-model:value="adjust.adjustmentType" :options="[{ label: '入库', value: 'INCREASE' }, { label: '出库', value: 'DECREASE' }]" /></Form.Item>
          <Form.Item label="数量" name="quantity" :rules="[{ required: true, message: '请填写调整数量' }]"><InputNumber v-model:value="adjust.quantity" :min="1" class="full" /></Form.Item>
          <Form.Item label="原因" name="reason" :rules="[{ required: true, message: '请填写调整原因' }]"><Input v-model:value="adjust.reason" /></Form.Item>
          <Button :loading="loading" html-type="submit" type="primary">提交调整</Button>
        </Form>
      </section>
      <section class="market-panel" aria-labelledby="rule-title">
        <h2 id="rule-title" class="market-panel-title">低库存阈值</h2>
        <Form :disabled="loading" :model="rule" layout="vertical" @finish="submitRule">
          <Form.Item label="启用预警"><Switch v-model:checked="rule.enabled" /></Form.Item>
          <Form.Item label="阈值" name="thresholdQuantity" :rules="[{ required: true, message: '请填写阈值' }]"><InputNumber v-model:value="rule.thresholdQuantity" :min="0" class="full" /></Form.Item>
          <Button :loading="loading" html-type="submit" type="primary">保存阈值</Button>
        </Form>
      </section>
    </div>

    <section class="market-panel" aria-labelledby="alerts-title">
      <h2 id="alerts-title" class="market-panel-title">低库存提醒</h2>
      <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="notice" show-icon />
      <Table :columns="alertColumns" :data-source="alerts" :loading="loading" :pagination="{ pageSize: 20, showSizeChanger: false }" :scroll="{ x: 720 }" row-key="productId" />
    </section>
  </main>
</template>

<style scoped>
.query-form { align-items: flex-end; }
.stock-result { margin-top: 18px; }
.full { width: 100%; }
.notice { margin-bottom: 16px; }
</style>
