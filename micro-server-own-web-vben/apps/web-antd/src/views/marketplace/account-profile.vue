<script lang="ts" setup>
import type { AccountProfile, AccountSession } from '#/api/marketplace';

import { computed, onMounted, reactive, ref } from 'vue';

import { Alert, Button, Form, Input, Popconfirm, Space, Table, Tag } from 'ant-design-vue';

import { accountApi } from '#/api/marketplace';
import { useAuthStore } from '#/store';

const authStore = useAuthStore();
const profile = ref<AccountProfile>();
const sessions = ref<AccountSession[]>([]);
const form = reactive({ email: '', name: '', phone: '' });
const loading = ref(false);
const saving = ref(false);
const sessionBusy = ref(false);
const error = ref('');
const sessionError = ref('');
const feedback = ref('');
const updateAttempt = computed(() => ({ data: { email: form.email.trim(), name: form.name.trim(), phone: form.phone.trim() }, key: crypto.randomUUID() }));
const revokeKeys = new Map<number, string>();
const allSessionsKey = ref(crypto.randomUUID());
const columns = [
  { dataIndex: 'actorType', key: 'actorType', title: '登录端' },
  { key: 'createdAt', title: '创建时间' },
  { key: 'expiresAt', title: '到期时间' },
  { key: 'actions', title: '操作' },
];
function errorText(reason: unknown, fallback: string) { return reason instanceof Error ? reason.message : fallback; }
function formatDate(value: string) { const date = new Date(value); return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN'); }
async function loadProfile() {
  loading.value = true; error.value = '';
  try {
    const current = await accountApi.profile();
    profile.value = current;
    Object.assign(form, { email: current.email || '', name: current.name || '', phone: current.phone || '' });
  } catch (reason) { error.value = errorText(reason, '个人资料加载失败'); }
  finally { loading.value = false; }
}
async function loadSessions() {
  sessionError.value = '';
  try { sessions.value = await accountApi.sessions(); }
  catch (reason) { sessionError.value = errorText(reason, '登录会话加载失败'); }
}
async function save() {
  if (saving.value) return;
  const attempt = updateAttempt.value;
  saving.value = true; feedback.value = ''; error.value = '';
  try {
    profile.value = await accountApi.updateProfile(attempt.data, attempt.key);
    feedback.value = '资料已保存';
  } catch (reason) { error.value = errorText(reason, '保存失败，请在当前页面重试'); }
  finally { saving.value = false; }
}
async function revoke(id: number) {
  if (sessionBusy.value) return;
  const key = revokeKeys.get(id) || crypto.randomUUID();
  revokeKeys.set(id, key); sessionBusy.value = true; sessionError.value = '';
  try {
    await accountApi.revokeSession(id, key);
    revokeKeys.delete(id);
    sessions.value = sessions.value.filter((session) => session.id !== id);
    feedback.value = '会话已撤销';
  } catch (reason) { sessionError.value = errorText(reason, '撤销失败，请重试'); }
  finally { sessionBusy.value = false; }
}
async function revokeAll() {
  if (sessionBusy.value) return;
  sessionBusy.value = true; sessionError.value = '';
  try {
    await accountApi.revokeAllSessions(allSessionsKey.value);
    await authStore.logout();
  } catch (reason) { sessionError.value = errorText(reason, '撤销失败，请重试'); }
  finally { sessionBusy.value = false; }
}
onMounted(() => { void loadProfile(); void loadSessions(); });
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div><span class="market-overline">ACCOUNT · 账号设置</span><h1 class="market-heading">个人资料与会话</h1><p class="market-subtitle">只修改自己的联系资料；登录名、角色与所属商家不在此处变更。</p></div>
    </header>
    <div class="market-grid">
      <section class="market-panel" aria-labelledby="profile-title">
        <h2 id="profile-title" class="market-panel-title">基本资料</h2>
        <Alert v-if="error" :message="error" type="error" show-icon class="notice" />
        <Alert v-if="feedback" :message="feedback" type="success" show-icon class="notice" />
        <div v-if="profile" class="identity"><span>登录名</span><strong>{{ profile.loginName }}</strong><span>账号 ID</span><strong>{{ profile.loginUserId }}</strong></div>
        <Form :model="form" layout="vertical" :disabled="loading || saving" @finish="save">
          <Form.Item label="姓名" name="name" :rules="[{ required: true, message: '请填写姓名' }]"><Input v-model:value="form.name" :maxlength="100" /></Form.Item>
          <Form.Item label="邮箱" name="email" :rules="[{ required: true, type: 'email', message: '请输入有效邮箱' }]"><Input v-model:value="form.email" :maxlength="254" /></Form.Item>
          <Form.Item label="电话" name="phone" :rules="[{ required: true, pattern: /^[0-9+() -]{6,32}$/, message: '请输入 6–32 位电话' }]"><Input v-model:value="form.phone" :maxlength="32" /></Form.Item>
          <Button html-type="submit" type="primary" :loading="saving">保存资料</Button>
        </Form>
      </section>
      <section class="market-panel" aria-labelledby="sessions-title">
        <div class="session-head"><div><h2 id="sessions-title" class="market-panel-title">登录会话</h2><p class="market-note">撤销全部会话后会退出当前账号。撤销单个会话可能包括当前设备。</p></div><Button :disabled="sessionBusy" @click="loadSessions">刷新</Button></div>
        <Alert v-if="sessionError" :message="sessionError" type="error" show-icon class="notice" />
        <Table :columns="columns" :data-source="sessions" :pagination="{ pageSize: 8, showSizeChanger: false }" :scroll="{ x: 620 }" row-key="id">
          <template #bodyCell="{ column, record }">
            <Tag v-if="column.key === 'actorType'">{{ record.actorType }}</Tag>
            <span v-else-if="column.key === 'createdAt'">{{ formatDate(record.createdAt) }}</span>
            <span v-else-if="column.key === 'expiresAt'">{{ formatDate(record.expiresAt) }}</span>
            <Popconfirm v-else-if="column.key === 'actions'" title="撤销这个登录会话？" @confirm="revoke(record.id)"><Button type="link" danger :disabled="sessionBusy">撤销</Button></Popconfirm>
          </template>
        </Table>
        <Space class="danger-zone"><Popconfirm title="撤销所有登录会话并退出当前账号？" @confirm="revokeAll"><Button danger :loading="sessionBusy">退出所有设备</Button></Popconfirm></Space>
      </section>
    </div>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.identity { display: grid; grid-template-columns: 68px minmax(0, 1fr); gap: 6px 12px; margin-bottom: 22px; padding: 14px 16px; border: 1px solid var(--market-line); border-radius: 10px; font-size: 12px; }
.identity span { color: var(--market-muted); }
.identity strong { overflow: hidden; font-weight: 550; text-overflow: ellipsis; white-space: nowrap; }
.session-head { display: flex; align-items: start; justify-content: space-between; gap: 14px; }
.session-head .market-panel-title { margin-bottom: 4px; }
.danger-zone { margin-top: 18px; }
.notice { margin-bottom: 16px; }
@media (max-width: 760px) { .session-head { flex-direction: column; } }
</style>
