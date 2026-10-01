<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';

import { Alert, Button, Form, Input, InputNumber, Select, Space, Table } from 'ant-design-vue';

import { systemApi } from '#/api/marketplace';

type ReviewReport = Record<string, null | number | string | undefined>;

const loading = ref(false);
const rows = ref<ReviewReport[]>([]);
const page = ref(0);
const total = ref(0);
const pageSize = 20;
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive({ note: '', reportId: undefined as number | undefined, status: 'RESOLVED' });
const columns = computed(() => Object.keys(rows.value[0] || {}).slice(0, 7).map((key) => ({ dataIndex: key, key, title: key })));

function message(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

async function load() {
  loading.value = true;
  feedback.value = null;
  try {
    const result = (await systemApi.reviewReports({ page: page.value, size: pageSize, status: 'PENDING' })) as {
      items?: ReviewReport[];
      total?: number;
    };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (error) {
    feedback.value = { text: message(error, '待处置举报加载失败，请刷新重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

async function resolve() {
  if (loading.value || !form.reportId) {
    feedback.value = { text: '请填写待处置的举报 ID', type: 'error' };
    return;
  }
  loading.value = true;
  feedback.value = null;
  try {
    await systemApi.resolveReviewReport(form.reportId, { note: form.note.trim(), status: form.status });
    Object.assign(form, { note: '', reportId: undefined, status: 'RESOLVED' });
    feedback.value = { text: '处置结果已提交', type: 'success' };
    await load();
  } catch (error) {
    feedback.value = { text: message(error, '处置提交失败，请稍后重试'), type: 'error' };
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
        <span class="market-overline">SYSTEM · MODERATION</span>
        <h1 class="market-heading">评价举报处置</h1>
        <p class="market-subtitle">仅处理待办举报；最终处置和内容规则由服务端执行并留存结果。</p>
      </div>
      <Button :loading="loading" @click="load">刷新待办</Button>
    </header>

    <section class="market-panel" aria-labelledby="report-list-title">
      <h2 id="report-list-title" class="market-panel-title">待处置举报</h2>
      <Table :columns="columns" :data-source="rows" :loading="loading" :pagination="{ current: page + 1, pageSize, total, showSizeChanger: false }" :scroll="{ x: 760 }" row-key="id" @change="changePage" />
    </section>

    <section class="market-panel" aria-labelledby="report-action-title">
      <h2 id="report-action-title" class="market-panel-title">提交处置</h2>
      <Form :disabled="loading" :model="form" class="resolve-form" layout="vertical" @finish="resolve">
        <Form.Item label="举报 ID" name="reportId" :rules="[{ required: true, message: '请填写举报 ID' }]">
          <InputNumber v-model:value="form.reportId" class="full" />
        </Form.Item>
        <Form.Item label="处置状态" name="status" :rules="[{ required: true, message: '请选择处置状态' }]">
          <Select v-model:value="form.status" :options="[{ label: '已解决', value: 'RESOLVED' }, { label: '已驳回', value: 'DISMISSED' }]" />
        </Form.Item>
        <Form.Item label="处置说明"><Input v-model:value="form.note" /></Form.Item>
        <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="notice" show-icon />
        <Space><Button :loading="loading" html-type="submit" type="primary">提交处置</Button></Space>
      </Form>
    </section>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.resolve-form { max-width: 560px; }
.full { width: 100%; }
.notice { margin-bottom: 16px; }
</style>
