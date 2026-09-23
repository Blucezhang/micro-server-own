<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { Button, Card, Form, Input, InputNumber, Select, Switch, Table, Tag } from 'ant-design-vue';
import { merchantApi } from '#/api/marketplace';

const loading = ref(false);
const alerts = ref<Record<string, any>[]>([]);
const stock = ref<Record<string, any>>();
const query = reactive({ merchantId: undefined as number | undefined, productId: undefined as number | undefined });
const adjust = reactive({ adjustmentType: 'INCREASE', quantity: undefined as number | undefined, reason: '' });
const rule = reactive({ enabled: true, thresholdQuantity: undefined as number | undefined });
async function loadAlerts() { loading.value = true; try { alerts.value = await merchantApi.lowStockAlerts() as Record<string, any>[]; } finally { loading.value = false; } }
async function loadStock() { if (!query.productId || !query.merchantId) return; loading.value = true; try { stock.value = await merchantApi.stock(query.productId, query.merchantId) as Record<string, any>; } finally { loading.value = false; } }
async function submitAdjust() { if (!query.productId || !query.merchantId) return; loading.value = true; try { await merchantApi.adjustStock(query.productId, { ...adjust, merchantId: query.merchantId }); await loadStock(); await loadAlerts(); } finally { loading.value = false; } }
async function submitRule() { if (!query.productId) return; loading.value = true; try { await merchantApi.saveLowStockRule(query.productId, rule); await loadAlerts(); } finally { loading.value = false; } }
onMounted(loadAlerts);
</script>
<template><main class="page"><Card :bordered="false"><Tag color="purple">商家运营</Tag><h1>库存与低库存预警</h1><p>库存调整必须说明原因；库存数值以库存服务返回结果为准。</p></Card><Card :bordered="false" title="查询库存"><Form layout="inline" @finish="loadStock"><Form.Item label="商品 ID"><InputNumber v-model:value="query.productId" /></Form.Item><Form.Item label="商家 ID"><InputNumber v-model:value="query.merchantId" /></Form.Item><Button html-type="submit">查询</Button></Form><pre v-if="stock">{{ JSON.stringify(stock, null, 2) }}</pre></Card><Card :bordered="false" title="调整库存"><Form layout="vertical" @finish="submitAdjust"><Form.Item label="调整类型"><Select v-model:value="adjust.adjustmentType" :options="[{label:'入库',value:'INCREASE'},{label:'出库',value:'DECREASE'}]" /></Form.Item><Form.Item label="数量"><InputNumber v-model:value="adjust.quantity" :min="1" class="full" /></Form.Item><Form.Item label="原因"><Input v-model:value="adjust.reason" /></Form.Item><Button :loading="loading" html-type="submit" type="primary">提交调整</Button></Form></Card><Card :bordered="false" title="低库存阈值"><Form layout="inline" @finish="submitRule"><Form.Item label="启用"><Switch v-model:checked="rule.enabled" /></Form.Item><Form.Item label="阈值"><InputNumber v-model:value="rule.thresholdQuantity" :min="0" /></Form.Item><Button html-type="submit">保存阈值</Button></Form></Card><Card :bordered="false" title="低库存提醒"><Table :columns="Object.keys(alerts[0] || {}).map(key=>({dataIndex:key,key,title:key}))" :data-source="alerts" :loading="loading" :pagination="false" row-key="productId" /></Card></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}.full{width:100%}pre{background:#f8fafc;border-radius:8px;padding:12px;white-space:pre-wrap}@media(max-width:640px){.page{padding:16px}}</style>
