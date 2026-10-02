<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';

import { Alert, Button, Form, InputNumber, Space } from 'ant-design-vue';

import { merchantApi } from '#/api/marketplace';

const loading = ref(false);
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive({ fixedAmount: undefined as number | undefined, freeThreshold: undefined as number | undefined });

function message(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

async function load() {
  loading.value = true;
  feedback.value = null;
  try {
    const rule = (await merchantApi.freight()) as Record<string, null | number>;
    form.fixedAmount = rule.fixedAmount ?? undefined;
    form.freeThreshold = rule.freeThreshold ?? undefined;
  } catch (error) {
    feedback.value = { text: message(error, '运费规则加载失败，请刷新重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (loading.value) return;
  if (form.fixedAmount == null || form.fixedAmount < 0) {
    feedback.value = { text: '请填写不小于 0 的固定运费', type: 'error' };
    return;
  }
  if (form.freeThreshold != null && form.freeThreshold < 0) {
    feedback.value = { text: '包邮门槛不能小于 0', type: 'error' };
    return;
  }
  loading.value = true;
  feedback.value = null;
  try {
    await merchantApi.saveFreight({ fixedAmount: form.fixedAmount, freeThreshold: form.freeThreshold });
    feedback.value = { text: '运费规则已保存，后续结算将由服务端实时试算', type: 'success' };
  } catch (error) {
    feedback.value = { text: message(error, '保存失败，请稍后重试'), type: 'error' };
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
        <span class="market-overline">MERCHANT · DELIVERY</span>
        <h1 class="market-heading">运费规则</h1>
        <p class="market-subtitle">设置固定运费与包邮门槛，订单结算时以服务端实时试算结果为准。</p>
      </div>
      <Button :loading="loading" @click="load">刷新规则</Button>
    </header>

    <section class="market-panel" aria-labelledby="freight-rule-title">
      <h2 id="freight-rule-title" class="market-panel-title">配送计费</h2>
      <Form :disabled="loading" :model="form" class="freight-form" layout="vertical" @finish="save">
        <Form.Item label="固定运费（元）" name="fixedAmount" :rules="[{ required: true, message: '请填写固定运费' }]">
          <InputNumber v-model:value="form.fixedAmount" :min="0" :precision="2" class="full" />
        </Form.Item>
        <Form.Item label="包邮门槛（元）" extra="留空表示不启用包邮门槛。">
          <InputNumber v-model:value="form.freeThreshold" :min="0" :precision="2" class="full" />
        </Form.Item>
        <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="notice" show-icon />
        <Space>
          <Button :loading="loading" html-type="submit" type="primary">保存规则</Button>
          <Button :disabled="loading" @click="load">恢复当前规则</Button>
        </Space>
      </Form>
    </section>
  </main>
</template>

<style scoped>
.freight-form { max-width: 520px; }
.full { width: 100%; }
.notice { margin-bottom: 16px; }
</style>
