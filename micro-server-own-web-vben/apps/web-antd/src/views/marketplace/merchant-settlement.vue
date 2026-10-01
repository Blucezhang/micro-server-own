<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';

import { Alert, Button, Form, InputNumber, Space } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

const loading = ref(false);
const balance = ref<Record<string, number | string>>();
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive({ amount: undefined as number | undefined });

function message(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

function currency(value: unknown) {
  const amount = Number(value);
  return Number.isFinite(amount) ? `¥${amount.toFixed(2)}` : '—';
}

async function load() {
  loading.value = true;
  feedback.value = null;
  try {
    balance.value = (await merchantApi.balance()) as Record<string, number | string>;
  } catch (error) {
    feedback.value = { text: message(error, '结算余额加载失败，请刷新重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function withdraw() {
  if (loading.value || !form.amount || form.amount <= 0) {
    feedback.value = { text: '请输入大于 0 的提现金额', type: 'error' };
    return;
  }
  loading.value = true;
  feedback.value = null;
  try {
    await merchantApi.withdraw(form.amount.toFixed(2));
    form.amount = undefined;
    feedback.value = { text: '提现申请已提交，请以结算服务处理结果为准', type: 'success' };
    await load();
  } catch (error) {
    feedback.value = { text: message(error, '提现申请失败，请稍后重试'), type: 'error' };
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
        <span class="market-overline">MERCHANT · SETTLEMENT</span>
        <h1 class="market-heading">结算与提现</h1>
        <p class="market-subtitle">查看可提现余额并提交申请；审核、到账与实际余额均由结算服务确认。</p>
      </div>
      <Button :loading="loading" @click="load">刷新余额</Button>
    </header>

    <div class="market-grid">
      <section class="market-panel" aria-labelledby="balance-title">
        <h2 id="balance-title" class="market-panel-title">可提现余额</h2>
        <strong class="balance">{{ currency(balance?.availableAmount) }}</strong>
        <p class="market-note">该数值不等同于最终到账金额，实际处理状态请以结算服务为准。</p>
      </section>
      <section class="market-panel" aria-labelledby="withdraw-title">
        <h2 id="withdraw-title" class="market-panel-title">申请提现</h2>
        <Form :disabled="loading" :model="form" layout="vertical" @finish="withdraw">
          <Form.Item label="提现金额（元）" name="amount" :rules="[{ required: true, message: '请填写提现金额' }]">
            <InputNumber v-model:value="form.amount" :min="0.01" :precision="2" class="full" />
          </Form.Item>
          <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="notice" show-icon />
          <Space><Button :loading="loading" html-type="submit" type="primary">提交申请</Button></Space>
        </Form>
      </section>
    </div>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.balance { display: block; margin: 8px 0 12px; font-size: 32px; letter-spacing: -.04em; }
.full { width: 100%; }
.notice { margin-bottom: 16px; }
</style>
