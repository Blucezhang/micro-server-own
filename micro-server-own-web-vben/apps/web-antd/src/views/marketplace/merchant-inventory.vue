<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';

import {
  Alert, Button, Descriptions, Empty, Form,
  Input, InputNumber, Select, Space, Statistic, Switch, Table, Tag,
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

const isOutOfStock = computed(() => {
  if (!stock.value || stock.value.availableQuantity == null) return false;
  return Number(stock.value.availableQuantity) <= 0;
});

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
      <div>
        <span class="market-overline">MERCHANT · INVENTORY</span>
        <h1 class="market-heading">库存管理</h1>
        <p class="market-subtitle">
          查询商品实时库存、执行出入库与盘点校准，并配置低库存预警规则；所有库存以库存服务计算结果为准。
        </p>
      </div>
      <Space>
        <Button :loading="alertsLoading" @click="loadAlerts">刷新预警</Button>
      </Space>
    </header>

    <Alert
      v-if="feedback"
      :message="feedback.text"
      :type="feedback.type"
      class="notice"
      show-icon
      closable
      @close="feedback = null"
    />

    <!-- 库存查询面板 -->
    <section class="market-panel" aria-labelledby="stock-query-title">
      <div class="panel-header">
        <h2 id="stock-query-title" class="market-panel-title">库存查询</h2>
        <p class="market-note">输入商品 ID 与商家 ID 查询当前实物与可用库存。</p>
      </div>

      <Form :disabled="loading" :model="query" class="query-form" layout="inline" @finish="() => loadStock(false)">
        <Form.Item label="商品 ID" name="productId" :rules="[{ required: true, message: '请填写商品 ID' }]">
          <InputNumber v-model:value="query.productId" :min="1" :precision="0" placeholder="商品 ID" style="width: 160px" />
        </Form.Item>
        <Form.Item label="商家 ID" name="merchantId" :rules="[{ required: true, message: '请填写商家 ID' }]">
          <InputNumber v-model:value="query.merchantId" :min="1" :precision="0" placeholder="商家 ID" style="width: 160px" />
        </Form.Item>
        <Form.Item>
          <Button :loading="loading" html-type="submit" type="primary">查询库存</Button>
        </Form.Item>
      </Form>

      <div v-if="queryFeedback" class="query-feedback-wrap">
        <Alert :message="queryFeedback.text" :type="queryFeedback.type" class="query-feedback" show-icon />
        <div v-if="stockLoadError" class="state-actions stock-retry-actions">
          <Button size="small" type="primary" ghost :loading="loading" @click="() => loadStock(true)">
            重试查询库存
          </Button>
        </div>
      </div>

      <!-- 查询结果展示 -->
      <div v-if="stock" class="stock-result-wrap">
        <div class="stock-metrics-strip">
          <div class="metric-item">
            <Statistic title="可用库存" :value="stock.availableQuantity ?? 0" :value-style="{ color: isOutOfStock ? 'hsl(var(--destructive))' : 'hsl(var(--primary))', fontWeight: '700' }" />
            <span class="metric-hint">可供买家直接下单的数量</span>
          </div>
          <div class="metric-item">
            <Statistic title="预留库存" :value="stock.reservedQuantity ?? 0" :value-style="{ color: 'hsl(var(--foreground))', fontWeight: '700' }" />
            <span class="metric-hint">下单待支付或待履约锁定量</span>
          </div>
          <div class="metric-item">
            <Statistic title="已售数量" :value="stock.soldQuantity ?? 0" :value-style="{ color: 'hsl(var(--foreground))', fontWeight: '700' }" />
            <span class="metric-hint">累计已确认售出数量</span>
          </div>
        </div>

        <Descriptions bordered size="small" class="stock-desc" :column="{ xs: 1, sm: 3 }">
          <Descriptions.Item label="商品 ID">{{ stock.productId ?? '—' }}</Descriptions.Item>
          <Descriptions.Item label="商家 ID">{{ stock.merchantId ?? '—' }}</Descriptions.Item>
          <Descriptions.Item label="库存状态">
            <Tag :color="isOutOfStock ? 'red' : 'green'">{{ isOutOfStock ? '已售罄' : '有可用库存' }}</Tag>
          </Descriptions.Item>
        </Descriptions>
      </div>
      <Empty v-else-if="stockQueried && !loading && !stockLoadError" description="未查询到该商品的库存信息" class="empty-state" />
    </section>

    <!-- 操作区域：库存调整与低库存规则 -->
    <div class="market-grid">
      <!-- 调整库存 -->
      <section class="market-panel" aria-labelledby="adjust-title">
        <div class="panel-header">
          <h2 id="adjust-title" class="market-panel-title">库存调整</h2>
          <p class="market-note">提交出入库单或盘点校准；请确认上方已选定目标商品与商家。</p>
        </div>

        <Form :disabled="adjustLoading" :model="adjust" layout="vertical" @finish="submitAdjust">
          <Form.Item label="调整类型" name="adjustmentType" :rules="[{ required: true, message: '请选择调整类型' }]">
            <Select v-model:value="adjust.adjustmentType" :options="adjustTypeOptions" />
          </Form.Item>
          <Form.Item label="调整数量" name="quantity" :rules="[{ required: true, message: '请填写调整数量' }]" extra="正整数，入库增加，出库扣减，盘点为实际数量。">
            <InputNumber v-model:value="adjust.quantity" :min="1" :precision="0" class="full" placeholder="请输入正整数" />
          </Form.Item>
          <Form.Item label="调整原因" name="reason" :rules="[{ required: true, message: '请填写调整原因' }]">
            <Input v-model:value="adjust.reason" placeholder="例如：供应商到货、破损出库、日常盘点校准" :maxlength="100" show-count />
          </Form.Item>
          <div class="form-action-row">
            <Button :loading="adjustLoading" :disabled="!hasScope()" html-type="submit" type="primary">提交调整</Button>
            <span v-if="!hasScope()" class="field-hint">需先在上方指定商品与商家 ID</span>
          </div>
        </Form>
      </section>

      <!-- 低库存预警配置 -->
      <section class="market-panel" aria-labelledby="rule-title">
        <div class="panel-header">
          <h2 id="rule-title" class="market-panel-title">低库存预警规则</h2>
          <p class="market-note">配置商品可用库存的警戒线，低于阈值时将在此页面列表中提醒。</p>
        </div>

        <Form :disabled="ruleLoading" :model="rule" layout="vertical" @finish="submitRule">
          <Form.Item label="预警状态" name="enabled">
            <div class="switch-row">
              <Switch v-model:checked="rule.enabled" />
              <span class="switch-label">{{ rule.enabled ? '已开启预警' : '已停用预警' }}</span>
            </div>
          </Form.Item>
          <Form.Item label="预警阈值数量" name="thresholdQuantity" :rules="[{ required: true, message: '请填写预警阈值' }]" extra="当可用库存小于等于该阈值时触发低库存提醒。">
            <InputNumber v-model:value="rule.thresholdQuantity" :min="0" :precision="0" class="full" placeholder="例如：10" />
          </Form.Item>
          <div class="form-action-row">
            <Button :loading="ruleLoading" :disabled="!hasProduct()" html-type="submit" type="primary">保存预警规则</Button>
            <span v-if="!hasProduct()" class="field-hint">需先在上方指定商品 ID</span>
          </div>
        </Form>
      </section>
    </div>

    <!-- 低库存提醒列表 -->
    <section class="market-panel alert-list-panel" aria-labelledby="alerts-title">
      <div class="list-heading">
        <div>
          <h2 id="alerts-title" class="market-panel-title">低库存预警列表</h2>
          <p class="market-note">当前店铺所有达到或低于预警阈值的商品清单，请及时补货。</p>
        </div>
        <Tag
          v-if="alertsLoaded && !alertsError"
          class="alert-count"
          :class="{ 'alert-count-zero': alerts.length === 0 }"
        >
          {{ alerts.length }} 件需关注
        </Tag>
      </div>

      <div v-if="alertsError" class="list-state-error">
        <Alert :message="alertsError" type="error" show-icon />
        <div class="state-actions">
          <Button size="small" type="primary" ghost :loading="alertsLoading" @click="loadAlerts">
            重试加载
          </Button>
        </div>
      </div>

      <Table
        v-else
        :columns="alertColumns"
        :data-source="alerts"
        :loading="alertsLoading"
        :pagination="{ pageSize: 10, showSizeChanger: false }"
        :scroll="{ x: 800 }"
        row-key="productId"
      >
        <template #emptyText>
          <Empty description="暂无低库存预警商品" />
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
    </section>
  </main>
</template>

<style scoped>
.notice { margin-bottom: 16px; }
.query-feedback-wrap { margin-top: 14px; }
.query-feedback { margin-bottom: 8px; }
.panel-header { margin-bottom: 16px; }
.query-form { display: flex; flex-wrap: wrap; gap: 12px 16px; align-items: flex-end; }
.stock-result-wrap { margin-top: 20px; }
.stock-metrics-strip { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
.metric-item { padding: 12px 16px; border: 1px solid var(--market-line); border-radius: 8px; background: hsl(var(--muted) / 0.3); }
.metric-hint { display: block; margin-top: 4px; color: hsl(var(--muted-foreground)); font-size: 11px; }
.stock-desc { margin-top: 12px; }
.empty-state { margin: 24px 0 12px; }
.full { width: 100%; }
.form-action-row { display: flex; align-items: center; gap: 12px; margin-top: 8px; }
.field-hint { color: hsl(var(--muted-foreground)); font-size: 12px; }
.switch-row { display: flex; align-items: center; gap: 10px; }
.switch-label { color: hsl(var(--foreground)); font-size: 13px; }
.list-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.alert-list-panel { padding-bottom: 10px; }
.alert-count { margin: 0; padding: 4px 10px; border: 0; border-radius: 999px; background: hsl(38 92% 90%); color: hsl(32 81% 29%); font-weight: 600; }
.alert-count-zero { background: var(--market-surface, #f1f5f9); color: var(--market-muted, #64748b); font-weight: 500; }
.tabular-num { font-variant-numeric: tabular-nums; }
.warn-stock { color: hsl(var(--destructive)); }
.list-state-error { padding: 4px 0 16px; display: flex; flex-direction: column; gap: 12px; }
.state-actions { display: flex; justify-content: flex-start; }
.stock-retry-actions { margin-top: 8px; }

@media (max-width: 768px) {
  .stock-metrics-strip { grid-template-columns: 1fr; }
  .query-form { flex-direction: column; align-items: stretch; }
  .query-form :deep(.ant-form-item) { margin-inline-end: 0; width: 100%; }
  .query-form :deep(.ant-input-number) { width: 100% !important; }
}
</style>
