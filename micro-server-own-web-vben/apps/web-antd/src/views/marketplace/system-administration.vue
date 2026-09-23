<script lang="ts" setup>
import { computed, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Alert, Button, Card, Form, Input, InputNumber, Space, Tag } from 'ant-design-vue';
import { systemApi } from '#/api/marketplace';

const route = useRoute();
const loading = ref(false);
const form = reactive({ functionIds: '', loginUserId: undefined as number | undefined, roleId: undefined as number | undefined });
const isAccount = computed(() => route.name === 'SystemAccounts');
async function submit() {
  loading.value = true;
  try {
    if (isAccount.value && form.loginUserId) await systemApi.grantMerchantRole(form.loginUserId);
    if (!isAccount.value && form.roleId) await systemApi.grantRoleFunctions(form.roleId, form.functionIds.split(',').map((id) => Number(id.trim())).filter(Number.isFinite));
  } finally { loading.value = false; }
}
</script>
<template><main class="page"><Card :bordered="false"><Tag color="red">系统治理</Tag><h1>{{ isAccount ? '账号与商家授权' : '角色权限授权' }}</h1><p>所有写操作由服务端以 SYSTEM 身份、权限和幂等键再次校验。</p></Card><Alert :message="isAccount ? '账号列表接口尚未由后端提供' : '角色/功能列表接口尚未由后端提供'" :description="'此页保留当前后端已交付的受控授权能力；请从已审核的数据源填入 ID。'" show-icon type="info"/><Card :bordered="false"><Form layout="vertical" @finish="submit"><template v-if="isAccount"><Form.Item label="登录账号 ID" required><InputNumber v-model:value="form.loginUserId" class="full" /></Form.Item><p>授予后，该账号将具备商家角色。</p></template><template v-else><Form.Item label="角色 ID" required><InputNumber v-model:value="form.roleId" class="full" /></Form.Item><Form.Item label="功能 ID（逗号分隔）" required><Input v-model:value="form.functionIds" placeholder="例如：12, 18, 25" /></Form.Item></template><Space><Button :loading="loading" html-type="submit" type="primary">{{ isAccount ? '授予商家角色' : '保存角色功能' }}</Button></Space></Form></Card></main></template>
<style scoped>.page{display:grid;gap:16px;padding:24px}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}.full{width:100%}@media(max-width:640px){.page{padding:16px}}</style>
