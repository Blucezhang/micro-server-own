<script lang="ts" setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

import { Fallback } from '@vben/common-ui';
import { LOGIN_PATH } from '@vben/constants';
import { useAccessStore, useUserStore } from '@vben/stores';

import { Button } from 'ant-design-vue';

import { useAuthStore } from '#/store';

interface Props {
  describe: string;
  showHome?: boolean;
  showLogin?: boolean;
  showRefresh?: boolean;
  status: '403' | '404' | '500' | 'coming-soon' | 'offline';
  title: string;
}

const props = withDefaults(defineProps<Props>(), {
  showHome: true,
  showLogin: true,
  showRefresh: false,
});

const router = useRouter();
const accessStore = useAccessStore();
const userStore = useUserStore();
const authStore = useAuthStore();

const isLoggedIn = computed(() => !!accessStore.accessToken);

const homePath = computed(() => {
  if (isLoggedIn.value && userStore.userInfo?.homePath) {
    return userStore.userInfo.homePath;
  }
  return '/buyer/workspace';
});

function backToHome() {
  if (isLoggedIn.value) {
    router.push(homePath.value);
  } else {
    router.push(LOGIN_PATH);
  }
}

function handleRefresh() {
  window.location.reload();
}

async function handleLogin() {
  if (isLoggedIn.value) {
    // 退出当前登录凭证后前往登录页，避免路由守卫重定向回工作台
    await authStore.logout(false);
  } else {
    router.push(LOGIN_PATH);
  }
}
</script>

<template>
  <div class="fallback-container">
    <Fallback :status="status">
      <template #title>
        <h1 class="fallback-title">{{ title }}</h1>
      </template>

      <template #describe>
        <p class="fallback-desc">{{ describe }}</p>
      </template>

      <template #action>
        <div class="fallback-actions">
          <Button v-if="showRefresh" type="primary" @click="handleRefresh">
            刷新重试
          </Button>

          <Button
            v-if="showHome"
            :type="showRefresh ? 'default' : 'primary'"
            @click="backToHome"
          >
            返回工作台
          </Button>

          <Button v-if="showLogin" @click="handleLogin">
            {{ isLoggedIn ? '退出并重新登录' : '返回登录页' }}
          </Button>
        </div>
      </template>
    </Fallback>
  </div>
</template>

<style scoped>
.fallback-container {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  width: 100%;
  min-height: clamp(440px, 75vh, 720px);
  padding: 24px 16px;
}

.fallback-title {
  margin: 20px 0 0;
  color: hsl(var(--foreground));
  font-size: clamp(18px, 2.2vw, 24px);
  font-weight: 650;
  letter-spacing: -0.02em;
  line-height: 1.35;
  text-align: center;
}

.fallback-desc {
  max-width: 520px;
  margin: 10px auto 0;
  color: hsl(var(--muted-foreground));
  font-size: 13px;
  line-height: 1.6;
  text-align: center;
}

.fallback-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 22px;
}

@media (max-width: 640px) {
  .fallback-container {
    padding: 16px 12px;
  }

  .fallback-actions {
    flex-direction: column;
    width: 100%;
    max-width: 260px;
  }

  .fallback-actions :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
