<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';

import {
  Alert,
  Button,
  Descriptions,
  Form,
  InputNumber,
  Spin,
  Tag,
} from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

interface SettlementBalance {
  availableAmount?: number | string | null;
}

type BalanceStatus = 'loading' | 'failed' | 'unknown' | 'zero' | 'positive';

const loading = ref(false);
const submitting = ref(false);
const balance = ref<SettlementBalance | null>(null);
const balanceLoaded = ref(false);

const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive({
  amount: undefined as number | undefined,
});

function message(error: unknown, fallback: string) {
  if (
    !(error instanceof Error) ||
    !error.message ||
    /^request failed with status code \d{3}$/i.test(error.message)
  ) {
    return fallback;
  }
  return error.message;
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

const hasPositiveBalance = computed(() => {
  return balanceStatus.value === 'positive';
});

const isZeroBalance = computed(() => {
  return balanceStatus.value === 'zero';
});

const statusTagConfig = computed(() => {
  switch (balanceStatus.value) {
    case 'positive':
      return { text: '可申请提现', color: 'green' };
    case 'zero':
      return { text: '暂无可提现金额', color: 'default' };
    case 'unknown':
      return { text: '暂不可用', color: 'default' };
    case 'failed':
      return { text: '获取失败', color: 'default' };
    case 'loading':
    default:
      return { text: '查询中', color: 'default' };
  }
});

const statusDescText = computed(() => {
  switch (balanceStatus.value) {
    case 'positive':
      return '正常可提现';
    case 'zero':
      return '当前无可提现金额';
    case 'unknown':
      return '数据异常，暂不可用';
    case 'failed':
      return '获取失败，请刷新重试';
    case 'loading':
    default:
      return '查询中';
  }
});

const withdrawDisabledReason = computed(() => {
  if (loading.value) {
    return '正在获取账户余额，请稍候...';
  }
  if (!balanceLoaded.value) {
    return '余额暂未成功获取，请点击页面顶部的「刷新余额」重试';
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
    const errText = message(error, '');
    const reason = errText ? `（${errText}）` : '';
    feedback.value = {
      text: `结算余额加载失败${reason}，请使用顶部的「刷新余额」重试`,
      type: 'error',
    };
  } finally {
    loading.value = false;
  }
}

async function withdraw() {
  if (submitting.value || loading.value) return;

  if (!hasPositiveBalance.value || numericBalance.value === null || numericBalance.value <= 0) {
    feedback.value = {
      text: '当前无可提现的有效余额，无法提交提现申请',
      type: 'error',
    };
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
        <span class="market-overline">MERCHANT · SETTLEMENT</span>
        <h1 class="market-heading">结算与提现</h1>
        <p class="market-subtitle">
          查看可提现余额并提交提现申请；实际处理与结果以结算服务确认状态为准。
        </p>
      </div>
      <Button :loading="loading" @click="() => load(false)">刷新余额</Button>
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

    <div class="market-grid">
      <!-- 可提现余额区块 -->
      <section class="market-panel balance-panel" aria-labelledby="balance-title">
        <div class="panel-header">
          <h2 id="balance-title" class="market-panel-title">可提现余额</h2>
          <Tag :color="statusTagConfig.color">
            {{ statusTagConfig.text }}
          </Tag>
        </div>

        <div class="balance-display-box">
          <span class="balance-label">当前账户可用金额</span>
          <div class="balance-value-row">
            <Spin :spinning="loading">
              <strong
                class="balance-number"
                :class="{ 'inactive-balance': !hasPositiveBalance, 'zero-balance': isZeroBalance }"
              >
                {{ formattedBalance }}
              </strong>
            </Spin>
          </div>
          <span class="balance-tip">
            以结算服务记录为准
          </span>
        </div>

        <Descriptions bordered size="small" :column="1" class="balance-info-desc">
          <Descriptions.Item label="资金状态">
            {{ statusDescText }}
          </Descriptions.Item>
          <Descriptions.Item label="最低限额说明">
            服务端设有最低起提限制（以服务端校验规则为准）
          </Descriptions.Item>
        </Descriptions>

        <p class="market-note balance-disclaimer">
          提现处理状态由结算服务更新，最终结果以服务端为准。
        </p>
      </section>

      <!-- 提现申请表单 -->
      <section class="market-panel withdraw-panel" aria-labelledby="withdraw-title">
        <div class="panel-header">
          <h2 id="withdraw-title" class="market-panel-title">申请提现</h2>
          <p class="market-note">提交提现申请，由结算服务处理。</p>
        </div>

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
              <li>起提门槛由结算服务端动态校验，超出余额或低于门槛将返回提示。</li>
            </ul>
          </div>

          <div class="submit-action-row">
            <Button
              :loading="submitting"
              :disabled="submitting || loading || !hasPositiveBalance"
              html-type="submit"
              type="primary"
              size="large"
              class="submit-btn"
            >
              提交提现申请
            </Button>
            <p v-if="withdrawDisabledReason" class="market-note submit-disabled-tip">
              {{ withdrawDisabledReason }}
            </p>
          </div>
        </Form>
      </section>
    </div>
  </main>
</template>

<style scoped>
.notice {
  margin-bottom: 16px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}
.panel-header .market-panel-title {
  margin-bottom: 0;
}
.balance-panel {
  display: flex;
  flex-direction: column;
}
.balance-display-box {
  padding: 20px;
  border-radius: 8px;
  background: hsl(var(--muted) / 0.4);
  margin-bottom: 20px;
}
.balance-label {
  display: block;
  font-size: 13px;
  color: hsl(var(--muted-foreground));
  margin-bottom: 6px;
}
.balance-value-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.balance-number {
  font-size: 36px;
  font-weight: 750;
  line-height: 1.15;
  color: hsl(var(--primary));
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.04em;
}
.balance-number.inactive-balance,
.balance-number.zero-balance {
  color: hsl(var(--muted-foreground));
}
.balance-tip {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}
.balance-info-desc {
  margin-bottom: 16px;
}
.balance-disclaimer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px dashed hsl(var(--border));
  font-size: 12px;
  line-height: 1.6;
}
.withdraw-panel {
  display: flex;
  flex-direction: column;
}
.full {
  width: 100%;
}
.amount-input-row {
  display: flex;
  gap: 8px;
}
.amount-input {
  flex: 1;
}
.all-in-btn {
  white-space: nowrap;
}
.rule-hint-box {
  margin: 16px 0 24px;
  padding: 12px 16px;
  border-radius: 8px;
  background: hsl(var(--muted) / 0.3);
}
.hint-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: hsl(var(--foreground));
}
.hint-list {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
}
.submit-action-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.submit-btn {
  width: 100%;
}
.submit-disabled-tip {
  margin: 0;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
  text-align: center;
}
</style>
