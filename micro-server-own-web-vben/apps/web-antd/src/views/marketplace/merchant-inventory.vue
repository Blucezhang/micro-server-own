<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useMediaQuery } from '@vueuse/core';
import {
  Alert, Button, Empty, Form, Input, InputNumber,
  Radio, Select, Switch, Table, Tag,
} from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

interface InventoryStock {
  availableQuantity?: number | null;
  merchantId?: number | null;
  productId?: number | null;
  reservedQuantity?: number | null;
  soldQuantity?: number | null;
  version?: number | null;
}

interface LowStockAlert {
  availableQuantity?: number | null;
  merchantId?: number | null;
  productId?: number | null;
  reservedQuantity?: number | null;
  soldQuantity?: number | null;
  thresholdQuantity?: number | null;
}

type AdjustmentType = 'INBOUND' | 'OUTBOUND' | 'STOCKTAKE';

const adjustTypeOptions: { label: string; value: AdjustmentType }[] = [
  { label: '入库补货', value: 'INBOUND' },
  { label: '出库扣减', value: 'OUTBOUND' },
  { label: '盘点校准', value: 'STOCKTAKE' },
];

const alertColumns = [
  { dataIndex: 'productId', key: 'productId', title: '商品 ID', width: 110 },
  { dataIndex: 'merchantId', key: 'merchantId', title: '商家 ID', width: 110 },
  { dataIndex: 'thresholdQuantity', key: 'thresholdQuantity', title: '预警阈值', width: 110 },
  { dataIndex: 'availableQuantity', key: 'availableQuantity', title: '可用库存', width: 110 },
  { dataIndex: 'reservedQuantity', key: 'reservedQuantity', title: '预留锁定', width: 110 },
  { dataIndex: 'soldQuantity', key: 'soldQuantity', title: '累计已售', width: 110 },
  { key: 'status', title: '预警状态', width: 110 },
  { key: 'actions', title: '快捷操作', width: 110 },
];

const isMobile = useMediaQuery('(max-width: 768px)');
const activeOpTab = ref<'adjust' | 'rule'>('adjust');

const loading = ref(false);
const alertsLoading = ref(false);
const adjustLoading = ref(false);
const ruleLoading = ref(false);

const alerts = ref<LowStockAlert[]>([]);
const alertsLoaded = ref(false);
const alertsError = ref<string | null>(null);

const stock = ref<InventoryStock | null>(null);
const stockQueried = ref(false);
const stockLoadError = ref(false);

const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const queryFeedback = ref<null | { text: string; type: 'error' | 'success' }>(null);

const query = reactive({
  merchantId: undefined as number | undefined,
  productId: undefined as number | undefined,
});

const adjust = reactive<{
  adjustmentType: AdjustmentType;
  quantity: number | undefined;
  reason: string;
}>({
  adjustmentType: 'INBOUND',
  quantity: undefined,
  reason: '',
});

const rule = reactive({
  enabled: true,
  thresholdQuantity: undefined as number | undefined,
});

const isOutOfStock = computed(() => {
  if (!stock.value || stock.value.availableQuantity == null) return false;
  return Number(stock.value.availableQuantity) <= 0;
});

function formatNumber(val: unknown) {
  if (val === undefined || val === null || val === '') return '—';
  const num = Number(val);
  return Number.isFinite(num) ? num.toLocaleString('zh-CN') : '—';
}

function message(error: unknown, fallback: string) {
  if (!(error instanceof Error) || /^request failed with status code \d{3}$/i.test(error.message)) {
    return fallback;
  }
  return error.message;
}

function hasProduct() {
  return Number.isSafeInteger(query.productId) && !!query.productId && query.productId > 0;
}

function hasScope() {
  return hasProduct() && Number.isSafeInteger(query.merchantId) && !!query.merchantId && query.merchantId > 0;
}

async function loadAlerts() {
  alertsLoading.value = true;
  alertsError.value = null;
  try {
    const res = (await merchantApi.lowStockAlerts()) as LowStockAlert[];
    alerts.value = Array.isArray(res) ? res : [];
    alertsLoaded.value = true;
  } catch (error) {
    alertsError.value = message(error, '低库存提醒加载失败，请刷新重试');
  } finally {
    alertsLoading.value = false;
  }
}

async function loadStock(preserveFeedback = false) {
  if (!hasScope()) {
    queryFeedback.value = { text: '请填写有效的商品 ID 与商家 ID（正整数）', type: 'error' };
    return;
  }
  loading.value = true;
  queryFeedback.value = null;
  stockLoadError.value = false;
  if (!preserveFeedback) {
    feedback.value = null;
  }
  try {
    const res = (await merchantApi.stock(query.productId!, query.merchantId!)) as InventoryStock;
    stock.value = res || null;
    stockQueried.value = true;
  } catch (error) {
    stock.value = null;
    stockLoadError.value = true;
    queryFeedback.value = { text: message(error, '库存加载失败，请检查商品与商家 ID 是否匹配'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function submitAdjust() {
  if (!hasScope()) {
    feedback.value = { text: '请先完成上方库存查询（需商品 ID 和商家 ID）', type: 'error' };
    return;
  }
  if (adjust.quantity == null || adjust.quantity <= 0) {
    feedback.value = { text: '请输入大于 0 的调整数量', type: 'error' };
    return;
  }
  if (!adjust.reason.trim()) {
    feedback.value = { text: '请填写调整原因（便于库存追溯）', type: 'error' };
    return;
  }

  adjustLoading.value = true;
  feedback.value = null;
  try {
    await merchantApi.adjustStock(query.productId!, {
      adjustmentType: adjust.adjustmentType,
      merchantId: query.merchantId,
      quantity: adjust.quantity,
      reason: adjust.reason.trim(),
    });
    adjust.quantity = undefined;
    adjust.reason = '';
    feedback.value = { text: '库存调整已提交成功，最新库存已同步刷新', type: 'success' };
    await Promise.all([loadStock(true), loadAlerts()]);
  } catch (error) {
    feedback.value = { text: message(error, '库存调整提交失败，请稍后重试'), type: 'error' };
  } finally {
    adjustLoading.value = false;
  }
}

async function submitRule() {
  if (!hasProduct()) {
    feedback.value = { text: '请先在上方填写有效的商品 ID', type: 'error' };
    return;
  }
  if (rule.thresholdQuantity == null || rule.thresholdQuantity < 0) {
    feedback.value = { text: '请填写不小于 0 的低库存预警阈值', type: 'error' };
    return;
  }

  ruleLoading.value = true;
  feedback.value = null;
  try {
    await merchantApi.saveLowStockRule(query.productId!, {
      enabled: rule.enabled,
      thresholdQuantity: rule.thresholdQuantity,
    });
    feedback.value = { text: `商品 ${query.productId} 的低库存规则已保存成功`, type: 'success' };
    await loadAlerts();
  } catch (error) {
    feedback.value = { text: message(error, '低库存规则保存失败，请稍后重试'), type: 'error' };
  } finally {
    ruleLoading.value = false;
  }
}

function applyAlertTarget(item: LowStockAlert) {
  if (item.productId) query.productId = Number(item.productId);
  if (item.merchantId) query.merchantId = Number(item.merchantId);
  if (item.thresholdQuantity != null) rule.thresholdQuantity = Number(item.thresholdQuantity);
  void loadStock();
}

onMounted(loadAlerts);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div class="market-head-text">
        <h1 class="market-heading">库存管理</h1>
        <p class="market-subtitle">查询商品实时库存、执行出入库与盘点校准，并配置低库存预警规则。</p>
      </div>
      <div class="market-head-actions">
        <Button :loading="alertsLoading" @click="loadAlerts">刷新预警</Button>
      </div>
    </header>

    <Alert
      v-if="feedback"
      :message="feedback.text"
      :type="feedback.type"
      class="action-alert"
      show-icon
      closable
      @close="feedback = null"
    />

    <!-- 库存查询面板 -->
    <section class="market-panel query-panel" aria-label="实时库存查询">
      <h2 class="panel-title-sm">实时库存查询</h2>
      <Form :disabled="loading" :model="query" class="query-form" layout="inline" @finish="() => loadStock(false)">
        <Form.Item label="商品 ID" name="productId" class="query-form-item">
          <InputNumber v-model:value="query.productId" :min="1" :precision="0" placeholder="商品 ID" class="query-input" />
        </Form.Item>
        <Form.Item label="商家 ID" name="merchantId" class="query-form-item">
          <InputNumber v-model:value="query.merchantId" :min="1" :precision="0" placeholder="商家 ID" class="query-input" />
        </Form.Item>
        <Form.Item class="query-form-actions">
          <Button :loading="loading" html-type="submit" type="primary">查询库存</Button>
        </Form.Item>
      </Form>

      <div v-if="queryFeedback" class="query-error-box">
        <span class="query-error-text">{{ queryFeedback.text }}</span>
        <Button v-if="stockLoadError" size="small" type="primary" ghost :loading="loading" @click="() => loadStock(true)">
          重试查询
        </Button>
      </div>

      <!-- 查询结果展示 -->
      <div v-if="stock" class="stock-summary-strip">
        <div class="summary-metric">
          <span class="metric-label">可用库存</span>
          <strong :class="['metric-value', isOutOfStock ? 'val-danger' : 'val-primary']">
            {{ formatNumber(stock.availableQuantity) }}
          </strong>
          <span class="metric-desc">可供买家直接下单</span>
        </div>
        <div class="summary-metric">
          <span class="metric-label">预留锁定</span>
          <strong class="metric-value">{{ formatNumber(stock.reservedQuantity) }}</strong>
          <span class="metric-desc">下单待支付或履约中</span>
        </div>
        <div class="summary-metric">
          <span class="metric-label">累计已售</span>
          <strong class="metric-value">{{ formatNumber(stock.soldQuantity) }}</strong>
          <span class="metric-desc">已确认售出总量</span>
        </div>
        <div class="summary-metric">
          <span class="metric-label">库存状态</span>
          <span :class="['status-pill', isOutOfStock ? 'status-pill-offline' : 'status-pill-available']">
            <span class="status-dot" aria-hidden="true" />
            <span>{{ isOutOfStock ? '已缺货' : '库存充足' }}</span>
          </span>
          <span class="metric-desc">商品 #{{ stock.productId }} · 商家 #{{ stock.merchantId }}</span>
        </div>
      </div>

      <div v-else-if="stockQueried && !loading && !stockLoadError" class="query-hint-bar">
        未查询到该商品的库存信息，请核对商品与商家编号。
      </div>
      <div v-else-if="!stockQueried" class="query-hint-bar">
        输入商品 ID 与商家 ID 查询实时库存并进行调整或预警设置。
      </div>
    </section>

    <!-- 操作区域：库存调整与低库存规则 -->
    <section class="ops-section" aria-label="库存操作">
      <div v-if="isMobile" class="mobile-tab-switch">
        <Radio.Group v-model:value="activeOpTab" button-style="solid">
          <Radio.Button value="adjust">库存出入库调整</Radio.Button>
          <Radio.Button value="rule">低库存预警规则</Radio.Button>
        </Radio.Group>
      </div>

      <div class="ops-grid">
        <!-- 调整库存 -->
        <div v-show="!isMobile || activeOpTab === 'adjust'" class="market-panel op-panel">
          <div class="op-header">
            <h2 class="op-title">库存调整</h2>
            <p class="op-desc">提交出入库单或盘点校准；需先选定目标商品与商家。</p>
          </div>

          <Form :disabled="adjustLoading" :model="adjust" layout="vertical" @finish="submitAdjust">
            <Form.Item label="调整类型" name="adjustmentType" :rules="[{ required: true, message: '请选择调整类型' }]">
              <Select v-model:value="adjust.adjustmentType" :options="adjustTypeOptions" />
            </Form.Item>
            <Form.Item label="调整数量" name="quantity" :rules="[{ required: true, message: '请填写调整数量' }]">
              <InputNumber v-model:value="adjust.quantity" :min="1" :precision="0" class="full" placeholder="输入正整数（入库增/出库减/盘点实存）" />
            </Form.Item>
            <Form.Item label="调整原因" name="reason" :rules="[{ required: true, message: '请填写调整原因' }]">
              <Input v-model:value="adjust.reason" placeholder="例如：供应商到货、破损出库、日常盘点校准" :maxlength="100" show-count />
            </Form.Item>
            <div class="form-action-row">
              <Button :loading="adjustLoading" :disabled="!hasScope()" html-type="submit" type="primary">提交调整</Button>
              <span v-if="!hasScope()" class="field-hint">需先在上方查询指定商品与商家 ID</span>
            </div>
          </Form>
        </div>

        <!-- 低库存预警配置 -->
        <div v-show="!isMobile || activeOpTab === 'rule'" class="market-panel op-panel">
          <div class="op-header">
            <h2 class="op-title">低库存预警规则</h2>
            <p class="op-desc">配置可用库存警戒线，低于阈值时在下方列表中标红提醒。</p>
          </div>

          <Form :disabled="ruleLoading" :model="rule" layout="vertical" @finish="submitRule">
            <Form.Item label="预警状态" name="enabled">
              <div class="switch-row">
                <Switch v-model:checked="rule.enabled" />
                <span class="switch-label">{{ rule.enabled ? '已开启预警' : '已停用预警' }}</span>
              </div>
            </Form.Item>
            <Form.Item label="预警阈值数量" name="thresholdQuantity" :rules="[{ required: true, message: '请填写预警阈值' }]">
              <InputNumber v-model:value="rule.thresholdQuantity" :min="0" :precision="0" class="full" placeholder="例如：10（可用库存低于等于该值触发）" />
            </Form.Item>
            <div class="form-action-row">
              <Button :loading="ruleLoading" :disabled="!hasProduct()" html-type="submit" type="primary">保存预警规则</Button>
              <span v-if="!hasProduct()" class="field-hint">需先在上方指定商品 ID</span>
            </div>
          </Form>
        </div>
      </div>
    </section>

    <!-- 低库存提醒列表 -->
    <section class="market-panel alert-panel" aria-label="低库存预警列表">
      <div class="alert-panel-head">
        <div class="alert-head-text">
          <h2 class="panel-title-sm" style="margin: 0">低库存预警列表</h2>
          <span
            v-if="alertsLoaded && !alertsError"
            :class="['alert-badge', alerts.length === 0 ? 'alert-badge-zero' : '']"
          >
            {{ alerts.length }} 件需关注
          </span>
        </div>
      </div>

      <div v-if="alertsError" class="catalog-error-row" role="alert">
        <div class="error-main">
          <span class="error-badge">加载失败</span>
          <span class="error-detail">{{ alertsError }}</span>
        </div>
        <Button size="small" type="primary" :loading="alertsLoading" @click="loadAlerts">重新加载</Button>
      </div>

      <template v-else>
        <div v-if="!isMobile" class="table-container">
          <Table
            :columns="alertColumns"
            :data-source="alerts"
            :loading="alertsLoading"
            :pagination="{ pageSize: 10, showSizeChanger: false }"
            row-key="productId"
          >
            <template #emptyText>
              <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无低库存预警商品" />
            </template>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'productId'">
                <strong>#{{ record.productId }}</strong>
              </template>
              <template v-else-if="column.key === 'merchantId'">
                <span>{{ record.merchantId ?? '—' }}</span>
              </template>
              <template v-else-if="column.key === 'thresholdQuantity'">
                <span class="tabular-num">{{ formatNumber(record.thresholdQuantity) }}</span>
              </template>
              <template v-else-if="column.key === 'availableQuantity'">
                <strong class="tabular-num warn-stock">{{ formatNumber(record.availableQuantity) }}</strong>
              </template>
              <template v-else-if="column.key === 'reservedQuantity'">
                <span class="tabular-num">{{ formatNumber(record.reservedQuantity) }}</span>
              </template>
              <template v-else-if="column.key === 'soldQuantity'">
                <span class="tabular-num">{{ formatNumber(record.soldQuantity) }}</span>
              </template>
              <template v-else-if="column.key === 'status'">
                <Tag :color="Number(record.availableQuantity) <= 0 ? 'red' : 'gold'">
                  {{ Number(record.availableQuantity) <= 0 ? '已缺货' : '库存紧张' }}
                </Tag>
              </template>
              <template v-else-if="column.key === 'actions'">
                <Button size="small" type="link" @click="applyAlertTarget(record)">定位补货</Button>
              </template>
            </template>
          </Table>
        </div>

        <div v-else class="mobile-card-list">
          <div v-if="alertsLoading" class="mobile-loading-tip">正在载入预警数据...</div>
          <div v-else-if="alerts.length === 0" class="mobile-empty">
            <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无低库存预警商品" />
          </div>
          <article v-for="(item, idx) in (alertsLoading ? [] : alerts)" :key="String(item.productId ?? idx)" class="mobile-card">
            <div class="mobile-card-top">
              <strong>商品 #{{ item.productId }}</strong>
              <Tag :color="Number(item.availableQuantity) <= 0 ? 'red' : 'gold'">
                {{ Number(item.availableQuantity) <= 0 ? '已缺货' : '库存紧张' }}
              </Tag>
            </div>
            <div class="mobile-card-grid">
              <div class="mobile-grid-cell">
                <span class="cell-label">可用库存</span>
                <span class="cell-val warn-stock">{{ formatNumber(item.availableQuantity) }}</span>
              </div>
              <div class="mobile-grid-cell">
                <span class="cell-label">预警阈值</span>
                <span class="cell-val">{{ formatNumber(item.thresholdQuantity) }}</span>
              </div>
              <div class="mobile-grid-cell">
                <span class="cell-label">预留锁定</span>
                <span class="cell-val">{{ formatNumber(item.reservedQuantity) }}</span>
              </div>
              <div class="mobile-grid-cell">
                <span class="cell-label">累计已售</span>
                <span class="cell-val">{{ formatNumber(item.soldQuantity) }}</span>
              </div>
            </div>
            <div class="mobile-card-actions">
              <span class="field-hint">商家 #{{ item.merchantId ?? '—' }}</span>
              <Button size="small" type="primary" ghost @click="applyAlertTarget(item)">定位补货</Button>
            </div>
          </article>
        </div>
      </template>
    </section>
  </main>
</template>

<style scoped src="./merchant-inventory.css"></style>
