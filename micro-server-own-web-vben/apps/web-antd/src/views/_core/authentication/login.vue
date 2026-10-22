<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';

import { computed } from 'vue';

import { AuthenticationLogin, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { useAuthStore } from '#/store';

defineOptions({ name: 'Login' });

const authStore = useAuthStore();

const demoActors = [
  { label: '买家端演示', value: 'BUYER' },
  { label: '商家端演示', value: 'MERCHANT' },
  { label: '系统治理演示', value: 'SYSTEM' },
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
    sub-title="请选择所属业务端并使用已授权账号登录"
    title="账号密码登录"
    @submit="authStore.authLogin"
  >
    <template v-if="authStore.uiDemoEnabled" #to-register>
      <div class="demo-entry">
        <div class="demo-header">
          <span class="demo-badge">本地走查</span>
          <span class="demo-tip">UI 演示（未连接真实业务网关）</span>
        </div>
        <div class="demo-actions">
          <button
            v-for="actor in demoActors"
            :key="actor.value"
            class="demo-btn"
            type="button"
            @click="authStore.enterUiDemo(actor.value)"
          >
            {{ actor.label }}
          </button>
        </div>
      </div>
    </template>
  </AuthenticationLogin>
</template>

<style scoped src="./login.css"></style>
