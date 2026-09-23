<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import { computed } from 'vue';

import { AuthenticationLogin, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { useAuthStore } from '#/store';

defineOptions({ name: 'Login' });

const authStore = useAuthStore();

const demoActors = [
  { label: '买家演示', value: 'BUYER' },
  { label: '商家演示', value: 'MERCHANT' },
  { label: '系统演示', value: 'SYSTEM' },
] as const;

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'VbenSelect',
      componentProps: {
        options: [
          { label: '买家端', value: 'BUYER' },
          { label: '商家端', value: 'MERCHANT' },
          { label: '系统端', value: 'SYSTEM' },
        ],
        placeholder: '选择登录端',
      },
      defaultValue: 'BUYER',
      fieldName: 'actorType',
      label: '登录端',
      rules: z.string().min(1, { message: '请选择登录端' }),
    },
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: $t('authentication.usernameTip'),
      },
      fieldName: 'username',
      label: $t('authentication.username'),
      rules: z.string().min(1, { message: $t('authentication.usernameTip') }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: $t('authentication.password'),
      },
      fieldName: 'password',
      label: $t('authentication.password'),
      rules: z.string().min(1, { message: $t('authentication.passwordTip') }),
    },
  ];
});
</script>

<template>
  <AuthenticationLogin
    :form-schema="formSchema"
    :loading="authStore.loginLoading"
    :show-code-login="false"
    :show-forget-password="false"
    :show-qrcode-login="false"
    :show-register="false"
    :show-third-party-login="false"
    sub-title="使用已授权的商城账号进入对应业务端"
    title="欢迎使用商城运营台"
    @submit="authStore.authLogin"
  >
    <template v-if="authStore.uiDemoEnabled" #to-register>
      <div class="demo-entry">
        <p>仅用于本地 UI 验收，不连接业务网关。</p>
        <button v-for="actor in demoActors" :key="actor.value" type="button" @click="authStore.enterUiDemo(actor.value)">
          {{ actor.label }}
        </button>
      </div>
    </template>
  </AuthenticationLogin>
</template>

<style scoped>
.demo-entry { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; }
.demo-entry p { color: #64748b; font-size: 12px; margin: 0 0 4px; width: 100%; }
.demo-entry button { background: transparent; border: 1px solid #cbd5e1; border-radius: 6px; color: #334155; cursor: pointer; font-size: 12px; padding: 6px 10px; }
.demo-entry button:hover { border-color: #1677ff; color: #1677ff; }
</style>
