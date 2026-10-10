<script lang="ts" setup>
import type { AccountProfile, AccountSession } from '#/api/marketplace';

import { computed, onMounted, reactive, ref, watch } from 'vue';

import { Alert, Button, Empty, Form, Input, Popconfirm, Space, Spin, Table, Tag } from 'ant-design-vue';

import { accountApi } from '#/api/marketplace';
import { useAuthStore } from '#/store';

const authStore = useAuthStore();
const profile = ref<AccountProfile>();
const sessions = ref<AccountSession[]>([]);
const form = reactive({ email: '', name: '', phone: '' });

const loading = ref(false);
const saving = ref(false);
const profileError = ref('');
const profileErrorAction = ref<'load' | 'save'>('load');
const profileFeedback = ref('');
const profileAttemptKey = ref(crypto.randomUUID());

const sessionLoading = ref(false);
const revokingId = ref<number | null>(null);
const revokingAll = ref(false);
const sessionError = ref('');
const sessionFeedback = ref('');
const sessionActionBusy = computed(() => revokingId.value !== null || revokingAll.value);

const revokeKeys = new Map<number, string>();
const allSessionsKey = ref(crypto.randomUUID());

let sessionSeq = 0;

const columns = [
  { dataIndex: 'actorType', key: 'actorType', title: '登录端' },
  { key: 'createdAt', title: '创建时间' },
  { key: 'expiresAt', title: '到期时间' },
  { key: 'actions', title: '操作' },
];

function isRawError(msg: string): boolean {
  if (!msg) return true;
  if (/^request failed with status code/i.test(msg)) return true;
  if (/network\s*error/i.test(msg)) return true;
  if (/timeout of \d+ms exceeded/i.test(msg)) return true;
  if (/axioserror/i.test(msg)) return true;
  if (/^(bad gateway|gateway timeout|internal server error|service unavailable)$/i.test(msg)) return true;
  if (/^(econnrefused|etimedout|enotfound|err_)/i.test(msg)) return true;
  return false;
}

function cleanFallback(fallback: string): string {
  return fallback.replace(/，?请(稍后)?重试$/, '');
}

function formatError(reason: unknown, fallback: string): string {
  if (!reason) return fallback;
  const err = reason as any;
  const data = err?.response?.data;
  const serverMsg = typeof data === 'string' ? '' : (data?.message || data?.msg || data?.error);
  if (typeof serverMsg === 'string' && serverMsg.trim() && !isRawError(serverMsg.trim())) {
    return serverMsg.trim();
  }
  const status = err?.response?.status;
  const rawMsg = String(err?.message || (reason instanceof Error ? reason.message : '')).trim();
  const base = cleanFallback(fallback);
  if (status === 502 || status === 504 || /status code 50[24]/i.test(rawMsg)) {
    return `${base}，网关响应超时或异常，请稍后重试`;
  }
  if ((typeof status === 'number' && status >= 500) || /status code 5\d\d/i.test(rawMsg)) {
    return `${base}，服务暂时不可用，请稍后重试`;
  }
  if (status === 404 || /status code 404/i.test(rawMsg)) {
    return `${base}，请求的资源不存在`;
  }
  if (status === 403 || /status code 403/i.test(rawMsg)) {
    return `${base}，暂无操作权限`;
  }
  if (status === 401 || /status code 401/i.test(rawMsg)) {
    return '登录状态已失效，请重新登录';
  }
  if (/network\s*error/i.test(rawMsg)) {
    return `${base}，网络连接异常，请检查网络后重试`;
  }
  if (/timeout/i.test(rawMsg)) {
    return `${base}，请求超时，请稍后重试`;
  }
  if (rawMsg && !isRawError(rawMsg)) {
    return rawMsg;
  }
  return fallback;
}

function formatDate(value: string) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

function actorTypeLabel(type?: string): string {
  if (!type) return '—';
  if (type === 'BUYER') return '买家端';
  if (type === 'MERCHANT') return '商家端';
  if (type === 'SYSTEM') return '系统端';
  return type;
}

const serializedPayload = computed(() =>
  JSON.stringify({
    email: form.email.trim(),
    name: form.name.trim(),
    phone: form.phone.trim(),
  }),
);

watch(serializedPayload, (next, prev) => {
  if (prev !== undefined && next !== prev) {
    profileAttemptKey.value = crypto.randomUUID();
    profileFeedback.value = '';
  }
});

async function loadProfile() {
  if (loading.value) return;
  loading.value = true;
  profileError.value = '';
  try {
    const current = await accountApi.profile();
    const previous = profile.value;
    const nameDirty = previous ? form.name.trim() !== (previous.name || '').trim() : false;
    const emailDirty = previous ? form.email.trim() !== (previous.email || '').trim() : false;
    const phoneDirty = previous ? form.phone.trim() !== (previous.phone || '').trim() : false;

    profile.value = current;

    if (!nameDirty) form.name = current.name || '';
    if (!emailDirty) form.email = current.email || '';
    if (!phoneDirty) form.phone = current.phone || '';

    profileAttemptKey.value = crypto.randomUUID();
  } catch (reason) {
    profileError.value = formatError(reason, '个人资料加载失败');
    profileErrorAction.value = 'load';
  } finally {
    loading.value = false;
  }
}

async function loadSessions() {
  if (sessionLoading.value) return;
  sessionLoading.value = true;
  sessionError.value = '';
  const seq = ++sessionSeq;
  try {
    const data = await accountApi.sessions();
    if (seq === sessionSeq) {
      sessions.value = data;
    }
  } catch (reason) {
    if (seq === sessionSeq) {
      sessionError.value = formatError(reason, '登录会话加载失败');
    }
  } finally {
    if (seq === sessionSeq) {
      sessionLoading.value = false;
    }
  }
}

async function save() {
  if (!profile.value || saving.value || loading.value) return;
  saving.value = true;
  profileFeedback.value = '';
  profileError.value = '';
  const payload = {
    email: form.email.trim(),
    name: form.name.trim(),
    phone: form.phone.trim(),
  };
  const key = profileAttemptKey.value;
  try {
    profile.value = await accountApi.updateProfile(payload, key);
    profileFeedback.value = '资料已保存';
    profileAttemptKey.value = crypto.randomUUID();
  } catch (reason) {
    profileError.value = formatError(reason, '保存失败，请在当前页面重试');
    profileErrorAction.value = 'save';
  } finally {
    saving.value = false;
  }
}

async function revoke(id: number) {
  if (sessionActionBusy.value || sessionLoading.value) return;
  const key = revokeKeys.get(id) || crypto.randomUUID();
  revokeKeys.set(id, key);
  revokingId.value = id;
  sessionError.value = '';
  sessionFeedback.value = '';
  try {
    await accountApi.revokeSession(id, key);
    revokeKeys.delete(id);
    sessions.value = sessions.value.filter((session) => session.id !== id);
    sessionFeedback.value = '会话已撤销';
  } catch (reason) {
    sessionError.value = formatError(reason, '撤销失败，请重试');
  } finally {
    revokingId.value = null;
  }
}

async function revokeAll() {
  if (sessionActionBusy.value || sessionLoading.value) return;
  revokingAll.value = true;
  sessionError.value = '';
  sessionFeedback.value = '';
  try {
    await accountApi.revokeAllSessions(allSessionsKey.value);
    await authStore.logout();
  } catch (reason) {
    sessionError.value = formatError(reason, '撤销失败，请重试');
  } finally {
    revokingAll.value = false;
  }
}

onMounted(() => {
  void loadProfile();
  void loadSessions();
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">ACCOUNT · 账号设置</span>
        <h1 class="market-heading">个人资料与会话</h1>
        <p class="market-subtitle">只修改自己的联系资料；登录名、角色与所属商家不在此处变更。</p>
      </div>
    </header>
    <div class="market-grid">
      <section class="market-panel" aria-labelledby="profile-title">
        <h2 id="profile-title" class="market-panel-title">基本资料</h2>
        <Alert v-if="profileError" :message="profileError" type="error" show-icon class="notice">
          <template #action>
            <Button size="small" danger ghost :loading="loading || saving" @click="profileErrorAction === 'save' ? save() : loadProfile()">
              重试
            </Button>
          </template>
        </Alert>
        <Alert v-if="profileFeedback" :message="profileFeedback" type="success" show-icon class="notice" />
        <Spin :spinning="loading">
          <template v-if="profile">
            <div class="identity">
              <span>登录名</span><strong>{{ profile.loginName }}</strong>
              <span>账号 ID</span><strong>{{ profile.loginUserId }}</strong>
            </div>
            <Form :model="form" layout="vertical" :disabled="loading || saving" @finish="save">
              <Form.Item label="姓名" name="name" :rules="[{ required: true, message: '请填写姓名' }]">
                <Input v-model:value="form.name" :maxlength="100" />
              </Form.Item>
              <Form.Item label="邮箱" name="email" :rules="[{ required: true, type: 'email', message: '请输入有效邮箱' }]">
                <Input v-model:value="form.email" :maxlength="254" />
              </Form.Item>
              <Form.Item label="电话" name="phone" :rules="[{ required: true, pattern: /^[0-9+() -]{6,32}$/, message: '请输入 6–32 位电话' }]">
                <Input v-model:value="form.phone" :maxlength="32" />
              </Form.Item>
              <Button html-type="submit" type="primary" :loading="saving" :disabled="loading">
                保存资料
              </Button>
            </Form>
          </template>
          <div v-else-if="loading" class="profile-loading-placeholder" />
        </Spin>
      </section>
      <section class="market-panel" aria-labelledby="sessions-title">
        <div class="session-head">
          <div>
            <h2 id="sessions-title" class="market-panel-title">登录会话</h2>
            <p class="market-note">撤销全部会话后会退出当前账号。撤销单个会话可能包括当前设备。</p>
          </div>
          <Button :loading="sessionLoading" :disabled="sessionActionBusy || sessionLoading" @click="loadSessions">刷新</Button>
        </div>
        <Alert v-if="sessionError" :message="sessionError" description="请点击上方“刷新”重试。" type="error" show-icon class="notice" />
        <Alert v-if="sessionFeedback" :message="sessionFeedback" type="success" show-icon class="notice" />
        <Table
          v-if="!sessionError || sessions.length"
          :columns="columns"
          :data-source="sessions"
          :loading="sessionLoading"
          :pagination="{ pageSize: 8, showSizeChanger: false }"
          :scroll="{ x: 620 }"
          row-key="id"
        >
          <template #emptyText>
            <Empty description="暂无活跃会话" />
          </template>
          <template #bodyCell="{ column, record }">
            <Tag v-if="column.key === 'actorType'">{{ actorTypeLabel(record.actorType) }}</Tag>
            <span v-else-if="column.key === 'createdAt'">{{ formatDate(record.createdAt) }}</span>
            <span v-else-if="column.key === 'expiresAt'">{{ formatDate(record.expiresAt) }}</span>
            <Popconfirm
              v-else-if="column.key === 'actions'"
              title="撤销这个登录会话？"
              :disabled="sessionActionBusy || sessionLoading"
              @confirm="revoke(record.id)"
            >
              <Button type="link" danger :disabled="sessionActionBusy || sessionLoading" :loading="revokingId === record.id">撤销</Button>
            </Popconfirm>
          </template>
        </Table>
        <Space class="danger-zone">
          <Popconfirm
            title="撤销所有登录会话并退出当前账号？"
            :disabled="sessionActionBusy || sessionLoading"
            @confirm="revokeAll"
          >
            <Button danger :loading="revokingAll" :disabled="sessionActionBusy || sessionLoading">退出所有设备</Button>
          </Popconfirm>
        </Space>
      </section>
    </div>
  </main>
</template>

<style scoped>
.identity { display: grid; grid-template-columns: 68px minmax(0, 1fr); gap: 6px 12px; margin-bottom: 22px; padding: 14px 16px; border: 1px solid var(--market-line); border-radius: 10px; font-size: 12px; }
.identity span { color: var(--market-muted); }
.identity strong { overflow: hidden; font-weight: 550; text-overflow: ellipsis; white-space: nowrap; }
.session-head { display: flex; align-items: start; justify-content: space-between; gap: 14px; }
.session-head .market-panel-title { margin-bottom: 4px; }
.profile-loading-placeholder { min-height: 220px; }
.danger-zone { margin-top: 18px; }
.notice { margin-bottom: 16px; }
@media (max-width: 760px) {
  .session-head { flex-direction: column; }
}
</style>
