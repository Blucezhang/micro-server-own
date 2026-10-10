<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';

import { Alert, Button, Form, InputNumber, Space } from 'ant-design-vue';

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
  if (!loadedOnce.value) {
    return loading.value ? '读取中…' : '未知（规则尚未读取）';
  }
  if (form.fixedAmount == null) {
    return '未设置';
  }
  return form.fixedAmount === 0 ? '0.00 元（免收运费）' : `¥${Number(form.fixedAmount).toFixed(2)} / 单`;
});

const summaryFreeThreshold = computed(() => {
  if (!loadedOnce.value) {
    return loading.value ? '读取中…' : '未知（规则尚未读取）';
  }
  if (form.freeThreshold == null) {
    return '未开启';
  }
  return `满 ¥${Number(form.freeThreshold).toFixed(2)}`;
});

const summaryRuleStatus = computed(() => {
  if (!loadedOnce.value) {
    return loading.value ? '读取中…' : '未知（规则尚未读取）';
  }
  if (refreshFailed.value) {
    if (isDraftModified.value) {
      return '草稿编辑中（最新刷新失败）';
    }
    return sourceKind.value === 'saved'
      ? '已保存配置（最新刷新失败）'
      : '来自最近一次成功读取（刷新失败）';
  }
  if (isDraftModified.value) {
    return '草稿编辑中（未保存）';
  }
  return sourceKind.value === 'saved' ? '已保存当前规则' : '来自最近一次成功读取';
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
    feedback.value = { text: '运费规则已保存，后续结算将由服务端实时试算', type: 'success' };
  } catch (error) {
    feedback.value = { text: message(error, '保存失败，请稍后重试'), type: 'error' };
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · DELIVERY</span>
        <h1 class="market-heading">运费规则</h1>
        <p class="market-subtitle">设置固定运费与包邮门槛，订单结算时以服务端实时试算结果为准。</p>
      </div>
      <Button v-if="loadedOnce || !loadError" :disabled="isBusy" :loading="loading" @click="load">
        {{ !loadedOnce && loadError ? '重试读取' : '刷新规则' }}
      </Button>
    </header>

    <div class="market-grid">
      <section class="market-panel" aria-labelledby="freight-rule-title">
        <h2 id="freight-rule-title" class="market-panel-title">配送计费配置</h2>
        <Alert
          v-if="!loadedOnce && loadError"
          :description="`${loadError}。当前未获取到已有运费规则，表单已锁定，请手动重试。`"
          class="notice"
          message="运费规则尚未读取成功"
          show-icon
          type="error"
        >
          <template #action>
            <Button :loading="loading" ghost size="small" type="primary" @click="load">
              手动重试
            </Button>
          </template>
        </Alert>
        <Alert
          v-else-if="!loadedOnce && loading"
          class="notice"
          description="正在从服务端同步当前配送配置，请稍候。"
          message="正在读取运费规则…"
          show-icon
          type="info"
        />
        <Alert
          v-else-if="feedback"
          :message="feedback.text"
          :type="feedback.type"
          class="notice"
          closable
          show-icon
          @close="feedback = null"
        >
          <template v-if="refreshFailed" #action>
            <Button :loading="loading" size="small" @click="load">
              重试刷新
            </Button>
          </template>
        </Alert>

        <Form :disabled="!loadedOnce || isBusy" :model="form" class="freight-form" layout="vertical" @finish="save">
          <Form.Item label="固定运费（元）" name="fixedAmount" :rules="[{ required: true, message: '请填写固定运费' }]">
            <InputNumber v-model:value="form.fixedAmount" :min="0" :precision="2" class="full" placeholder="0.00" prefix="¥" />
          </Form.Item>
          <Form.Item extra="留空表示不启用包邮门槛。" label="包邮门槛（元）">
            <InputNumber v-model:value="form.freeThreshold" :min="0" :precision="2" class="full" placeholder="留空不启用包邮" prefix="¥" />
          </Form.Item>
          <Space :wrap="true">
            <Button :disabled="!loadedOnce || isBusy" :loading="saving" html-type="submit" type="primary">
              保存规则
            </Button>
            <Button :disabled="!loadedOnce || isBusy" @click="load">
              恢复当前规则
            </Button>
          </Space>
        </Form>
      </section>

      <section class="market-panel" aria-labelledby="freight-summary-title">
        <h2 id="freight-summary-title" class="market-panel-title">当前表单配置预览</h2>
        <div class="summary-list">
          <div class="summary-item">
            <span class="summary-label">表单基础运费</span>
            <span class="summary-value">
              {{ summaryFixedAmount }}
            </span>
          </div>
          <div class="summary-item">
            <span class="summary-label">表单包邮门槛</span>
            <span class="summary-value">
              {{ summaryFreeThreshold }}
            </span>
          </div>
          <div class="summary-item">
            <span class="summary-label">规则来源状态</span>
            <span :class="{ 'text-warn': refreshFailed }" class="summary-value">
              {{ summaryRuleStatus }}
            </span>
          </div>
          <div class="summary-item">
            <span class="summary-label">计费机制</span>
            <span class="summary-value">以服务端实时试算为准</span>
          </div>
        </div>
        <div class="market-note rule-notes">
          <p>1. 上方为当前表单编辑值的本地预览，并非已保存生效的规则；最终运费金额以服务端结算试算为准。</p>
          <p>2. 修改后需点击“保存规则”提交；如需放弃未保存的草稿修改，可点击“恢复当前规则”重新同步。</p>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.freight-form {
  max-width: 520px;
}
.full {
  width: 100%;
}
.notice {
  margin-bottom: 16px;
}
.summary-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}
.summary-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  background: hsl(var(--muted) / 0.4);
  border: 1px solid var(--market-line);
}
.summary-label {
  font-size: 13px;
  color: var(--market-muted);
  flex-shrink: 0;
}
.summary-value {
  font-size: 13px;
  font-weight: 500;
  color: var(--market-text);
  text-align: right;
  word-break: break-word;
}
.text-warn {
  color: hsl(var(--warning, 38 92% 50%));
}
.rule-notes {
  display: flex;
  flex-direction: column;
  gap: 6px;
  line-height: 1.5;
}
.rule-notes p {
  margin: 0;
}
@media (max-width: 640px) {
  .freight-form {
    max-width: 100%;
  }
}
</style>
