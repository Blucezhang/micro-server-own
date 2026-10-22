<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';

import {
  Alert,
  Button,
  Form,
  InputNumber,
  Spin,
  Tag,
} from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

interface SettlementBalance {
  availableAmount?: number | string | null;
}

type BalanceStatus = 'failed' | 'loading' | 'positive' | 'unknown' | 'zero';

const loading = ref(false);
const submitting = ref(false);
const balance = ref<SettlementBalance | null>(null);
const balanceLoaded = ref(false);
const loadError = ref<string | null>(null);

const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive({
  amount: undefined as number | undefined,
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

const numericBalance = computed<number | null>(() => {
  if (!balanceLoaded.value || !balance.value || balance.value.availableAmount == null) {
    return null;
  }
  const n = Number(balance.value.availableAmount);
  return Number.isFinite(n) ? n : null;
});

const formattedBalance = computed(() => {
  if (!balanceLoaded.value || numericBalance.value == null) {
    return '—';
  }
  return `¥${numericBalance.value.toFixed(2)}`;
});

const balanceStatus = computed<BalanceStatus>(() => {
  if (loading.value && !balanceLoaded.value) {
    return 'loading';
  }
  if (!balanceLoaded.value) {
    return 'failed';
  }
  if (numericBalance.value === null) {
    return 'unknown';
  }
  if (numericBalance.value <= 0) {
    return 'zero';
  }
  return 'positive';
});

const hasPositiveBalance = computed(() => balanceStatus.value === 'positive');
const isZeroBalance = computed(() => balanceStatus.value === 'zero');

const statusTagConfig = computed(() => {
  switch (balanceStatus.value) {
    case 'positive':
      return { color: 'green', text: '可申请提现' };
    case 'zero':
      return { color: 'default', text: '暂无可提现金额' };
    case 'unknown':
      return { color: 'default', text: '数据异常' };
    case 'failed':
      return { color: 'default', text: '未获取' };
    case 'loading':
    default:
      return { color: 'default', text: '查询中…' };
  }
});

const withdrawDisabledReason = computed(() => {
  if (loading.value) {
    return '正在获取账户余额，请稍候…';
  }
  if (!balanceLoaded.value) {
    return '需成功获取有效余额方可提现';
  }
  if (numericBalance.value === null) {
    return '账户余额数据异常，暂不可提交提现';
  }
  if (numericBalance.value <= 0) {
    return '当前账户暂无可提现余额';
  }
  return '';
});

function validateAmount(_rule: unknown, val: unknown): Promise<void> {
  if (!hasPositiveBalance.value || numericBalance.value == null || numericBalance.value <= 0) {
    return Promise.reject(new Error('当前无可提现的有效余额'));
  }
  if (val == null || val === '') {
    return Promise.resolve();
  }
  const num = Number(val);
  if (!Number.isFinite(num) || num <= 0) {
    return Promise.reject(new Error('提现金额必须大于 0'));
  }
  if (num > numericBalance.value) {
    return Promise.reject(new Error('提现金额不能超过当前可用余额'));
  }
  return Promise.resolve();
}

async function load(preserveFeedback = false) {
  loading.value = true;
  loadError.value = null;
  if (!preserveFeedback) {
    feedback.value = null;
  }
  try {
    const res = (await merchantApi.balance()) as SettlementBalance | null | undefined;
    balance.value = res ?? null;
    balanceLoaded.value = true;
  } catch (error) {
    balance.value = null;
    balanceLoaded.value = false;
    loadError.value = message(error, '结算余额服务暂不可用，请点击重试');
  } finally {
    loading.value = false;
  }
}

async function withdraw() {
  if (submitting.value || loading.value) return;

  if (!hasPositiveBalance.value || numericBalance.value === null || numericBalance.value <= 0) {
    feedback.value = { text: '当前无可提现的有效余额，无法提交提现申请', type: 'error' };
    return;
  }

  if (form.amount == null || form.amount <= 0) {
    feedback.value = { text: '请输入大于 0 的提现金额', type: 'error' };
    return;
  }

  if (form.amount > numericBalance.value) {
    feedback.value = {
      text: `提现金额 (¥${form.amount.toFixed(2)}) 超出当前可用余额 (¥${numericBalance.value.toFixed(2)})`,
      type: 'error',
    };
    return;
  }

  submitting.value = true;
  feedback.value = null;
  const submittedAmount = form.amount.toFixed(2);

  try {
    await merchantApi.withdraw(submittedAmount);
    form.amount = undefined;
    feedback.value = {
      text: `提现申请（¥${submittedAmount}）已成功提交，结算服务正在处理。`,
      type: 'success',
    };
    await load(true);
  } catch (error) {
    feedback.value = {
      text: message(error, '提现申请提交失败，请核对金额或稍后重试'),
      type: 'error',
    };
  } finally {
    submitting.value = false;
  }
}

function withdrawAll() {
  if (submitting.value || loading.value) return;
  if (hasPositiveBalance.value && numericBalance.value !== null && numericBalance.value > 0) {
    form.amount = numericBalance.value;
  }
}

onMounted(() => load(false));
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">结算与提现</h1>
        <p class="market-subtitle">
          查看可提现余额并提交提现申请；提现处理进度以结算服务确认状态为准。
        </p>
      </div>
      <Button :disabled="loading" :loading="loading" @click="() => load(false)">刷新余额</Button>
    </header>

    <div class="market-grid">
      <!-- 可提现余额区块 -->
      <section class="market-panel balance-panel" aria-labelledby="balance-title">
        <div class="panel-header">
          <h2 id="balance-title" class="market-panel-title">可提现余额</h2>
          <Tag :color="statusTagConfig.color">
            {{ statusTagConfig.text }}
          </Tag>
        </div>

        <div class="balance-card">
          <div class="balance-label-row">
            <span class="balance-label">当前账户可用金额</span>
          </div>

          <div class="balance-value-row">
            <Spin :spinning="loading">
              <strong
                class="balance-number"
                :class="{ 'inactive-balance': !hasPositiveBalance || isZeroBalance }"
              >
                {{ formattedBalance }}
              </strong>
            </Spin>
          </div>

          <p class="balance-status-desc">
            <span v-if="!balanceLoaded && !loading">余额状态：未知（未获取到服务数据）</span>
            <span v-else-if="loading">正在向结算服务同步最新余额…</span>
            <span v-else>以结算服务最终确认记录为准</span>
          </p>
        </div>

        <!-- 余额读取失败：单处错误说明与重试 -->
        <div v-if="!balanceLoaded && loadError" class="state-box">
          <div class="state-info">
            <span class="state-title">账户余额获取失败</span>
            <span class="state-desc">{{ loadError }}</span>
          </div>
          <Button :loading="loading" size="small" type="primary" ghost @click="() => load(false)">
            点击重试
          </Button>
        </div>
      </section>

      <!-- 提现申请表单 -->
      <section class="market-panel withdraw-panel" aria-labelledby="withdraw-title">
        <div class="panel-header">
          <h2 id="withdraw-title" class="market-panel-title">申请提现</h2>
        </div>

        <Alert
          v-if="feedback"
          :message="feedback.text"
          :type="feedback.type"
          class="notice"
          closable
          show-icon
          @close="feedback = null"
        />

        <Form
          :disabled="submitting || loading || !hasPositiveBalance"
          :model="form"
          layout="vertical"
          @finish="withdraw"
        >
          <Form.Item
            label="提现金额（元）"
            name="amount"
            :rules="[
              { required: true, message: '请填写提现金额' },
              { validator: validateAmount },
            ]"
          >
            <div class="amount-input-row">
              <InputNumber
                v-model:value="form.amount"
                :min="0.01"
                :max="hasPositiveBalance ? (numericBalance ?? undefined) : undefined"
                :precision="2"
                :step="100"
                class="full amount-input"
                placeholder="0.00"
                prefix="¥"
              />
              <Button
                v-if="hasPositiveBalance"
                class="all-in-btn"
                size="middle"
                @click="withdrawAll"
              >
                全部提现
              </Button>
            </div>
          </Form.Item>

          <div class="rule-hint-box">
            <h4 class="hint-title">提现须知</h4>
            <ul class="hint-list">
              <li>提现金额请精确至两位小数（分）。</li>
              <li>起提门槛与手续费以结算服务端校验为准，超出可用余额将无法提交。</li>
            </ul>
          </div>

          <div class="submit-action-row">
            <Button
              :disabled="submitting || loading || !hasPositiveBalance"
              :loading="submitting"
              class="submit-btn"
              html-type="submit"
              size="large"
              type="primary"
            >
              提交提现申请
            </Button>
            <p v-if="withdrawDisabledReason" class="submit-disabled-tip">
              {{ withdrawDisabledReason }}
            </p>
          </div>
        </Form>
      </section>
    </div>
  </main>
</template>

<style scoped src="./merchant-settlement.css"></style>
