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

watch(
  () => [form.loginUserId, form.roleId, form.functionIds, isAccount.value],
  () => { feedback.value = null; },
);

function parseFunctionIds(value: string): null | number[] {
  const parts = value.split(',').map((part) => part.trim());
  if (parts.some((part) => !/^[1-9]\d*$/.test(part))) return null;
  const ids = parts.map(Number);
  if (ids.some((id) => !Number.isSafeInteger(id))) return null;
  return [...new Set(ids)];
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
      feedback.value = { text: '商家角色授权成功', type: 'success' };
    } else {
      await systemApi.grantRoleFunctions(targetId, functionIds!);
      feedback.value = { text: '角色功能授权成功', type: 'success' };
    }
  } catch (error) {
    feedback.value = {
      text: error instanceof Error ? error.message : '授权失败，请稍后重试',
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
        <span class="market-overline">SYSTEM · ACCESS</span>
        <h1 class="market-heading">{{ isAccount ? '账号与商家授权' : '角色权限授权' }}</h1>
        <p class="market-subtitle">所有写操作由服务端以 SYSTEM 身份和权限再次校验。</p>
      </div>
    </header>
    <section class="market-panel">
      <Alert
        :message="isAccount ? '账号列表接口尚未由后端提供' : '角色/功能列表接口尚未由后端提供'"
        description="此页仅保留后端已交付的受控授权能力；请从已审核的数据源填入 ID。"
        class="notice"
        show-icon
        type="info"
      />
      <Form :model="form" class="grant-form" layout="vertical" @finish="submit">
        <template v-if="isAccount">
          <Form.Item label="登录账号 ID" required>
            <InputNumber v-model:value="form.loginUserId" class="full" />
          </Form.Item>
          <p>授予后，该账号将具备商家角色。</p>
        </template>
        <template v-else>
          <Form.Item label="角色 ID" required>
            <InputNumber v-model:value="form.roleId" class="full" />
          </Form.Item>
          <Form.Item label="功能 ID（逗号分隔）" required>
            <Input v-model:value="form.functionIds" placeholder="例如：12, 18, 25" />
          </Form.Item>
        </template>
        <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="feedback" show-icon />
        <Space>
          <Button :loading="loading" html-type="submit" type="primary">
            {{ isAccount ? '授予商家角色' : '授予角色功能' }}
          </Button>
        </Space>
      </Form>
    </section>
  </main>
</template>

<style scoped>
.grant-form { max-width: 560px; }
.full { width: 100%; }
.notice,
.feedback { margin: 0 0 16px; }
</style>
