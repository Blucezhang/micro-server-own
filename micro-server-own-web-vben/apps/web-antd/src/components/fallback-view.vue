<script lang="ts" setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { useAccessStore, useUserStore } from '@vben/stores';

import { Button } from 'ant-design-vue';

import { useAuthStore } from '#/store';

import './fallback-view.css';

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

const statusTag = computed(() => {
  switch (props.status) {
    case '403': {
      return '403';
    }
    case '404': {
      return '404';
    }
    case '500': {
      return '500';
    }
    case 'coming-soon': {
      return '建设中';
    }
    case 'offline': {
      return '离线';
    }
    default: {
      return '';
    }
  }
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
    await authStore.logout(false);
  } else {
    router.push(LOGIN_PATH);
  }
}
</script>

<template>
  <div class="fallback-wrapper">
    <div class="fallback-card">
      <div class="fallback-header">
        <div class="fallback-icon-wrap" :class="`fallback-icon-${status}`">
          <!-- 403 -->
          <svg
            v-if="status === '403'"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.75"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="fallback-icon-svg"
            aria-hidden="true"
          >
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>

          <!-- 404 -->
          <svg
            v-else-if="status === '404'"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.75"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="fallback-icon-svg"
            aria-hidden="true"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
            <line x1="8" y1="11" x2="14" y2="11" />
          </svg>

          <!-- 500 -->
          <svg
            v-else-if="status === '500'"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.75"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="fallback-icon-svg"
            aria-hidden="true"
          >
            <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
            <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
            <line x1="6" y1="6" x2="6.01" y2="6" />
            <line x1="6" y1="18" x2="6.01" y2="18" />
            <path d="M14 6h4" />
            <path d="M14 18h4" />
          </svg>

          <!-- offline -->
          <svg
            v-else-if="status === 'offline'"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.75"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="fallback-icon-svg"
            aria-hidden="true"
          >
            <line x1="1" y1="1" x2="23" y2="23" />
            <path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55" />
            <path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39" />
            <path d="M10.71 16.08a4.95 4.95 0 0 1 2.58 0" />
            <line x1="12" y1="20" x2="12.01" y2="20" />
          </svg>

          <!-- coming-soon -->
          <svg
            v-else-if="status === 'coming-soon'"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.75"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="fallback-icon-svg"
            aria-hidden="true"
          >
            <polygon points="12 2 2 7 12 12 22 7 12 2" />
            <polyline points="2 17 12 22 22 17" />
            <polyline points="2 12 12 17 22 12" />
          </svg>
        </div>

        <span class="fallback-status-tag">{{ statusTag }}</span>
      </div>

      <slot name="title">
        <h1 class="fallback-title">{{ title }}</h1>
      </slot>

      <slot name="describe">
        <p class="fallback-desc">{{ describe }}</p>
      </slot>

      <slot name="action">
        <div class="fallback-actions">
          <Button
            v-if="showRefresh"
            type="primary"
            @click="handleRefresh"
          >
            刷新重试
          </Button>

          <Button
            v-if="showHome"
            :type="showRefresh ? 'default' : 'primary'"
            @click="backToHome"
          >
            返回工作台
          </Button>

          <Button
            v-if="showLogin"
            :type="(!showHome && !showRefresh) ? 'primary' : 'default'"
            @click="handleLogin"
          >
            {{ isLoggedIn ? '退出并重新登录' : '返回登录页' }}
          </Button>
        </div>
      </slot>
    </div>
  </div>
</template>
