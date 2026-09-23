<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { Button, Card, Form, Input, InputNumber, Select, Space, Table, Tag } from 'ant-design-vue';
import { systemApi } from '#/api/marketplace';

const loading = ref(false);
const rows = ref<Record<string, any>[]>([]);
const form = reactive({ note: '', reportId: undefined as number | undefined, status: 'RESOLVED' });
const columns = computed(() => Object.keys(rows.value[0] || {}).slice(0, 7).map((key) => ({ dataIndex: key, key, title: key })));
function unwrap(data: any) { return Array.isArray(data) ? data : data?.items || []; }
async function load() { loading.value = true; try { rows.value = unwrap(await systemApi.reviewReports({ page: 0, size: 20, status: 'PENDING' })); } finally { loading.value = false; } }
async function resolve() { if (!form.reportId) return; loading.value = true; try { await systemApi.resolveReviewReport(form.reportId, { note: form.note, status: form.status }); await load(); } finally { loading.value = false; } }
onMounted(load);
</script>
<template><main class="page"><Card :bordered="false"><Tag color="red">系统治理</Tag><h1>评价举报处置</h1><p>仅处理待办举报；最终处置和内容规则由服务端执行。</p></Card><Card :bordered="false" title="待处置举报"><Table :columns="columns" :data-source="rows" :loading="loading" :pagination="false" row-key="id" /></Card><Card :bordered="false" title="提交处置"><Form layout="vertical" @finish="resolve"><Form.Item label="举报 ID" required><InputNumber v-model:value="form.reportId" class="full" /></Form.Item><Form.Item label="处置状态" required><Select v-model:value="form.status" :options="[{label:'已解决',value:'RESOLVED'},{label:'已驳回',value:'REJECTED'}]" /></Form.Item><Form.Item label="处置说明"><Input v-model:value="form.note" /></Form.Item><Space><Button :loading="loading" html-type="submit" type="primary">提交处置</Button><Button @click="load">刷新</Button></Space></Form></Card></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}.full{width:100%}@media(max-width:640px){.page{padding:16px}}</style>
