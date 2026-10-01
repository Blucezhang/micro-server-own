<script lang="ts" setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

import { Fallback } from '@vben/common-ui';
import { LOGIN_PATH } from '@vben/constants';
import { useAccessStore, useUserStore } from '@vben/stores';

import { Button } from 'ant-design-vue';

defineOptions({ name: 'Fallback500Demo' });

const router = useRouter();
const accessStore = useAccessStore();
const userStore = useUserStore();

const homePath = computed(() => {
  if (accessStore.accessToken && userStore.userInfo?.homePath) {
    return userStore.userInfo.homePath;
  }
  return '/buyer/workspace';
});

function refresh() {
  window.location.reload();
}

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
    <Fallback status="500">
      <template #title>
        <h1 class="fallback-title">500 · 微商城服务异常</h1>
      </template>
      <template #describe>
        <p class="fallback-desc">
          微商城服务端处理请求时遇到了未预期的错误或服务超时。您可以尝试刷新页面重新发起请求，或返回工作台处理其他业务。
        </p>
      </template>
      <template #action>
        <div class="fallback-actions">
          <Button size="large" type="primary" @click="refresh">
            刷新重试
          </Button>
          <Button size="large" @click="backToHome">
            返回工作台
          </Button>
          <Button size="large" @click="backToLogin">
            返回登录页
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
