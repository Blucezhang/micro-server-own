<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';

import { Alert, Button, Form, InputNumber } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

interface FreightRuleConfig {
  fixedAmount: number | undefined;
  freeThreshold: number | undefined;
}

const loading = ref(false);
const saving = ref(false);
const loadedOnce = ref(false);
const loadError = ref<null | string>(null);
const refreshFailed = ref(false);
const sourceKind = ref<'read' | 'saved' | null>(null);
const lastLoadedConfig = ref<FreightRuleConfig | null>(null);

const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive<FreightRuleConfig>({ fixedAmount: undefined, freeThreshold: undefined });

const isBusy = computed(() => loading.value || saving.value);

const isDraftModified = computed(() => {
  if (!lastLoadedConfig.value) return false;
  return (
    form.fixedAmount !== lastLoadedConfig.value.fixedAmount ||
    form.freeThreshold !== lastLoadedConfig.value.freeThreshold
  );
});

const summaryFixedAmount = computed(() => {
  if (!loadedOnce.value) return '—';
  if (form.fixedAmount == null) return '未设置';
  return form.fixedAmount === 0 ? '0.00 元（免收运费）' : `¥${Number(form.fixedAmount).toFixed(2)} / 单`;
});

const summaryFreeThreshold = computed(() => {
  if (!loadedOnce.value) return '—';
  if (form.freeThreshold == null) return '未开启';
  return `满 ¥${Number(form.freeThreshold).toFixed(2)} 包邮`;
});

const summaryRuleStatus = computed(() => {
  if (!loadedOnce.value) return { class: 'status-error', text: '未获取' };
  if (refreshFailed.value) {
    return { class: 'status-draft', text: '刷新未同步（保留草稿）' };
  }
  if (isDraftModified.value) {
    return { class: 'status-draft', text: '草稿修改中（未保存）' };
  }
  return { class: 'status-active', text: sourceKind.value === 'saved' ? '已保存生效' : '当前已生效' };
});

function message(error: unknown, fallback: string) {
  if (error && typeof error === 'object') {
    const err = error as Record<string, unknown>;
    const response = err.response as Record<string, unknown> | undefined;
    const resData = response?.data as Record<string, unknown> | undefined;
    const directMsg = (resData?.message || response?.message || err.message) as string | undefined;
    if (typeof directMsg === 'string' && directMsg.trim()) {
      const trimmed = directMsg.trim();
      const isRawTransport = /status code|network error|timeout|failed to fetch|econnrefused|gateway|abort/i.test(trimmed);
      if (!isRawTransport && /[\u4e00-\u9fa5]/.test(trimmed)) {
        return trimmed;
      }
    }
  }
  return fallback;
}

async function load() {
  loading.value = true;
  feedback.value = null;
  loadError.value = null;
  try {
    const rule = (await merchantApi.freight()) as Record<string, null | number>;
    const fixedAmount = rule?.fixedAmount ?? undefined;
    const freeThreshold = rule?.freeThreshold ?? undefined;
    lastLoadedConfig.value = { fixedAmount, freeThreshold };
    form.fixedAmount = fixedAmount;
    form.freeThreshold = freeThreshold;
    loadedOnce.value = true;
    sourceKind.value = 'read';
    refreshFailed.value = false;
  } catch (error) {
    if (!loadedOnce.value) {
      loadError.value = message(error, '运费规则读取失败，请点击重试');
    } else {
      refreshFailed.value = true;
      feedback.value = {
        text: message(error, '刷新失败，当前保留编辑草稿与最近已知规则'),
        type: 'error',
      };
    }
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (!loadedOnce.value || isBusy.value) return;
  if (form.fixedAmount == null || form.fixedAmount < 0) {
    feedback.value = { text: '请填写不小于 0 的固定运费', type: 'error' };
    return;
  }
  if (form.freeThreshold != null && form.freeThreshold < 0) {
    feedback.value = { text: '包邮门槛不能小于 0', type: 'error' };
    return;
  }
  saving.value = true;
  feedback.value = null;
  try {
    await merchantApi.saveFreight({ fixedAmount: form.fixedAmount, freeThreshold: form.freeThreshold });
    lastLoadedConfig.value = { fixedAmount: form.fixedAmount, freeThreshold: form.freeThreshold };
    sourceKind.value = 'saved';
    refreshFailed.value = false;
    feedback.value = { text: '运费规则已保存生效', type: 'success' };
  } catch (error) {
    feedback.value = { text: message(error, '保存失败，请稍后重试'), type: 'error' };
  } finally {
    saving.value = false;
  }
}

function restoreConfig() {
  if (!lastLoadedConfig.value) return;
  form.fixedAmount = lastLoadedConfig.value.fixedAmount;
  form.freeThreshold = lastLoadedConfig.value.freeThreshold;
  feedback.value = null;
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">运费规则</h1>
        <p class="market-subtitle">设置店铺基础运费与满额包邮策略；实际运费以订单结算时系统实时试算为准。</p>
      </div>
      <Button :disabled="isBusy" :loading="loading" @click="load">刷新规则</Button>
    </header>

    <div class="market-grid">
      <!-- 计费配置表单 -->
      <section class="market-panel" aria-labelledby="freight-rule-title">
        <div class="panel-header-row">
          <h2 id="freight-rule-title" class="market-panel-title">配送计费配置</h2>
        </div>

        <!-- 规则读取失败：单一集中说明与重试 -->
        <div v-if="!loadedOnce && loadError" class="state-box">
          <div class="state-info">
            <span class="state-title">运费规则获取失败</span>
            <span class="state-desc">{{ loadError }}</span>
          </div>
          <Button :loading="loading" size="small" type="primary" ghost @click="load">重试读取</Button>
        </div>

        <Alert
          v-else-if="feedback"
          :message="feedback.text"
          :type="feedback.type"
          class="notice"
          closable
          show-icon
          @close="feedback = null"
        />

        <Form :disabled="!loadedOnce || isBusy" :model="form" class="freight-form" layout="vertical" @finish="save">
          <div class="form-block">
            <Form.Item
              extra="订单基础运费；填 0 即全场免运费。"
              label="固定运费（元）"
              name="fixedAmount"
              :rules="[{ required: true, message: '请填写固定运费' }]"
            >
              <InputNumber
                v-model:value="form.fixedAmount"
                :min="0"
                :precision="2"
                class="full"
                placeholder="0.00"
                prefix="¥"
              />
            </Form.Item>

            <Form.Item
              extra="商品总额达到此金额免收固定运费；留空表示不开启满额包邮。"
              label="满额包邮门槛（元）"
            >
              <InputNumber
                v-model:value="form.freeThreshold"
                :min="0"
                :precision="2"
                class="full"
                placeholder="留空表示不开启"
                prefix="¥"
              />
            </Form.Item>
          </div>

          <div class="form-actions">
            <Button
              :disabled="!loadedOnce || isBusy"
              :loading="saving"
              html-type="submit"
              type="primary"
            >
              保存规则
            </Button>
            <Button
              :disabled="!loadedOnce || isBusy || !isDraftModified"
              @click="restoreConfig"
            >
              放弃草稿并恢复
            </Button>
          </div>
        </Form>
      </section>

      <!-- 规则生效预览 -->
      <section class="market-panel preview-panel" aria-labelledby="freight-summary-title">
        <div class="panel-header-row">
          <h2 id="freight-summary-title" class="market-panel-title">规则生效预览</h2>
        </div>

        <div v-if="!loadedOnce" class="preview-placeholder-box">
          <span class="placeholder-text">{{ loading ? '正在读取运费规则…' : '规则读取成功后在此实时预览计费策略' }}</span>
        </div>

        <div v-else class="summary-list">
          <div class="summary-item">
            <span class="summary-label">基础运费</span>
            <span class="summary-value">{{ summaryFixedAmount }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">满额包邮</span>
            <span class="summary-value">{{ summaryFreeThreshold }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">规则状态</span>
            <span class="status-badge" :class="summaryRuleStatus.class">
              {{ summaryRuleStatus.text }}
            </span>
          </div>
          <div class="summary-item">
            <span class="summary-label">结算计费机制</span>
            <span class="summary-value">以服务端实时试算为准</span>
          </div>
        </div>

        <p class="concise-tip">
          提示：运费规则保存后立即生效；买家提交结算时以服务端实时试算结果为准。
        </p>
      </section>
    </div>
  </main>
</template>

<style scoped src="./merchant-freight.css"></style>
