<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Alert, Button, Form, Input, InputNumber, Space } from 'ant-design-vue';

import { systemApi } from '#/api/marketplace';

const route = useRoute();
const loading = ref(false);
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive({
  functionIds: '',
  loginUserId: undefined as number | undefined,
  roleId: undefined as number | undefined,
});
const isAccount = computed(() => route.name === 'SystemAccounts');
const changeNotes = computed(() => isAccount.value
  ? [
      { title: '授权内容', description: '追加 ROLE_MERCHANT，不会移除该账号已有角色。' },
      { title: '登录影响', description: '服务端会撤销该账号的现存刷新会话，账号需重新登录。' },
    ]
  : [
      { title: '授权方式', description: '仅追加功能关联，保留角色已有权限；重复功能 ID 会自动去重。' },
      { title: '提交校验', description: '服务端校验角色与功能 ID 是否存在；任一目标无效时整批拒绝。' },
    ]);

watch(
  () => [form.loginUserId, form.roleId, form.functionIds, isAccount.value],
  () => {
    feedback.value = null;
  },
);

function parseFunctionIds(value: string): null | number[] {
  const parts = value.split(',').map((part) => part.trim());
  if (parts.some((part) => !/^[1-9]\d*$/.test(part))) return null;
  const ids = parts.map(Number);
  if (ids.some((id) => !Number.isSafeInteger(id))) return null;
  return [...new Set(ids)];
}

function resetForm() {
  form.loginUserId = undefined;
  form.roleId = undefined;
  form.functionIds = '';
  feedback.value = null;
}

watch(isAccount, resetForm);

function errorMessage(error: unknown, fallback: string) {
  if (!(error instanceof Error)) return fallback;
  const status = error.message.match(/(?:status code|HTTP)\s*:?\s*(\d{3})/i)?.[1];
  if (!status) return error.message || fallback;
  return Number(status) >= 500
    ? `服务暂时不可用（HTTP ${status}），请稍后重试`
    : `请求失败（HTTP ${status}），请检查操作后重试`;
}

async function submit() {
  if (loading.value) return;
  feedback.value = null;
  const targetId = isAccount.value ? form.loginUserId : form.roleId;
  if (!Number.isSafeInteger(targetId) || !targetId || targetId <= 0) {
    feedback.value = { text: '请输入正整数 ID', type: 'error' };
    return;
  }
  const functionIds = isAccount.value ? null : parseFunctionIds(form.functionIds);
  if (!isAccount.value && !functionIds) {
    feedback.value = { text: '请输入正整数功能 ID，多个 ID 用逗号分隔', type: 'error' };
    return;
  }

  loading.value = true;
  try {
    if (isAccount.value) {
      await systemApi.grantMerchantRole(targetId);
      feedback.value = { text: '商家角色授权成功，该账号刷新会话已撤销，请重新登录', type: 'success' };
    } else {
      await systemApi.grantRoleFunctions(targetId, functionIds!);
      feedback.value = { text: '角色功能追加成功', type: 'success' };
    }
  } catch (error) {
    feedback.value = {
      text: errorMessage(error, '授权失败，请稍后重试'),
      type: 'error',
    };
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">{{ isAccount ? '账号与商家授权' : '角色功能授权' }}</h1>
        <p class="market-subtitle">核对目标 ID 后提交授权；操作范围由服务端校验。</p>
      </div>
    </header>

    <div class="grant-layout">
      <section class="market-panel grant-panel" aria-labelledby="grant-form-title">
        <h2 id="grant-form-title" class="market-panel-title">
          {{ isAccount ? '商家角色分配' : '角色功能权限追加' }}
        </h2>

        <p class="id-check-note">本页不提供目录检索，请先核实目标 ID；目标是否存在由服务端在提交时校验。</p>

        <Form :model="form" class="grant-form" layout="vertical" @finish="submit">
          <template v-if="isAccount">
            <Form.Item label="登录账号 ID" name="loginUserId" required>
              <InputNumber
                v-model:value="form.loginUserId"
                class="full"
                :min="1"
                :precision="0"
                placeholder="请输入正整数账号 ID，如：1001"
              />
              <div class="field-hint">请输入登录账号 ID，不是商家主体 ID。</div>
            </Form.Item>
          </template>
          <template v-else>
            <Form.Item label="角色 ID" name="roleId" required>
              <InputNumber
                v-model:value="form.roleId"
                class="full"
                :min="1"
                :precision="0"
                placeholder="请输入正整数角色 ID，如：2001"
              />
              <div class="field-hint">指定需追加功能权限的目标系统角色。</div>
            </Form.Item>
            <Form.Item label="功能 ID 列表" name="functionIds" required>
              <Input
                v-model:value="form.functionIds"
                placeholder="例如：12, 18, 25"
              />
              <div class="field-hint">多个正整数 ID 用逗号分隔，重复项会自动去重。</div>
            </Form.Item>
          </template>

          <div class="change-summary" aria-labelledby="guide-title">
            <h3 id="guide-title">提交前请确认</h3>
            <dl class="change-list">
              <div v-for="note in changeNotes" :key="note.title" class="change-item">
                <dt>{{ note.title }}</dt>
                <dd>{{ note.description }}</dd>
              </div>
            </dl>
            <p class="permission-note">此操作需系统管理员权限。</p>
          </div>

          <Alert
            v-if="feedback"
            :message="feedback.text"
            :type="feedback.type"
            class="feedback"
            show-icon
          />

          <Space class="actions-space">
            <Button :loading="loading" html-type="submit" type="primary">
              {{ isAccount ? '授予商家角色' : '追加角色功能' }}
            </Button>
            <Button :disabled="loading" @click="resetForm">
              重置
            </Button>
          </Space>
        </Form>
      </section>
    </div>
  </main>
</template>

<style scoped>
.grant-layout {
  max-width: 760px;
}

.grant-form {
  max-width: 600px;
}

.full {
  width: 100%;
}

.feedback {
  margin: 0 0 16px;
}

.id-check-note {
  margin: -4px 0 20px;
  color: var(--market-muted);
  font-size: 13px;
  line-height: 1.6;
}

.field-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--market-muted);
  line-height: 1.4;
}

.actions-space {
  margin-top: 8px;
}

.change-summary {
  margin: 4px 0 20px;
  padding: 14px 16px 10px;
  border: 1px solid var(--market-line);
  border-radius: var(--mall-radius-base);
  background: hsl(var(--muted) / 0.18);
}

.change-summary h3 {
  margin: 0;
  color: var(--market-text);
  font-size: 13px;
  font-weight: 650;
}

.change-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  margin: 2px 0 0;
}

.change-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--market-line);
}

.change-item dt {
  font-size: 13px;
  font-weight: 600;
  color: var(--market-text);
}

.change-item dd {
  margin: 5px 0 0;
  font-size: 12.5px;
  color: var(--market-muted);
  line-height: 1.55;
}

.permission-note {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--market-muted);
}

@media (max-width: 640px) {
  .change-summary {
    padding: 12px 14px 8px;
  }
  .change-item {
    padding: 9px 0;
  }
}
</style>
