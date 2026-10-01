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
    sub-title="选择所属业务端并使用已授权账号登录"
    title="微服务商城运营台"
    @submit="authStore.authLogin"
  >
    <template v-if="authStore.uiDemoEnabled" #to-register>
      <div class="demo-entry">
        <p class="demo-entry-tip">
          本地 UI 走查入口（未连接真实业务网关）：
        </p>
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
    </template>
  </AuthenticationLogin>
</template>

<style scoped>
.demo-entry {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  padding: 12px 14px;
  border: 1px dashed hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--card) / 0.5);
}

.demo-entry-tip {
  width: 100%;
  margin: 0 0 4px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.5;
}

.demo-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 5px 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.16s ease;
}

.demo-btn:hover {
  border-color: hsl(var(--primary));
  background: hsl(var(--primary) / 0.08);
  color: hsl(var(--primary));
}

.demo-btn:focus-visible {
  outline: 2px solid hsl(var(--primary));
  outline-offset: 1px;
}
</style>
