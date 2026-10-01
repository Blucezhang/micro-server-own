<script lang="ts" setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

import { Fallback } from '@vben/common-ui';
import { LOGIN_PATH } from '@vben/constants';
import { useAccessStore, useUserStore } from '@vben/stores';

import { Button } from 'ant-design-vue';

defineOptions({ name: 'Fallback403Demo' });

const router = useRouter();
const accessStore = useAccessStore();
const userStore = useUserStore();

const homePath = computed(() => {
  if (accessStore.accessToken && userStore.userInfo?.homePath) {
    return userStore.userInfo.homePath;
  }
  return '/buyer/workspace';
});

function backToHome() {
  if (accessStore.accessToken) {
    router.push(homePath.value);
  } else {
    router.push(LOGIN_PATH);
  }
}

function backToLogin() {
  router.push(LOGIN_PATH);
}
</script>

<template>
  <div class="fallback-page-wrapper">
    <Fallback status="403">
      <template #title>
        <h1 class="fallback-title">403 · 无权访问该商城资源</h1>
      </template>
      <template #describe>
        <p class="fallback-desc">
          您当前的登录身份或角色未获得访问该微商城模块的授权。请核实您所在的业务端（买家端、商家端或系统治理端），或切换到具备权限的账号重试。
        </p>
      </template>
      <template #action>
        <div class="fallback-actions">
          <Button size="large" type="primary" @click="backToHome">
            返回工作台
          </Button>
          <Button size="large" @click="backToLogin">
            切换登录账号
          </Button>
        </div>
      </template>
    </Fallback>
  </div>
</template>

<style scoped>
.fallback-page-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  width: 100%;
  min-height: clamp(480px, 80vh, 760px);
  padding: 24px 16px;
}

.fallback-title {
  margin: 24px 0 0;
  color: hsl(var(--foreground));
  font-size: clamp(20px, 2.8vw, 30px);
  font-weight: 650;
  line-height: 1.3;
  text-align: center;
}

.fallback-desc {
  max-width: 560px;
  margin: 12px auto 0;
  color: hsl(var(--muted-foreground));
  font-size: 14px;
  line-height: 1.65;
  text-align: center;
}

.fallback-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

@media (max-width: 640px) {
  .fallback-page-wrapper {
    padding: 16px 12px;
  }

  .fallback-actions {
    flex-direction: column;
    width: 100%;
    max-width: 280px;
  }

  .fallback-actions :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
