<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';

import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { AuthenticationLoginExpiredModal } from '@vben/common-ui';
import { useWatermark } from '@vben/hooks';
import {
  BasicLayout,
  LockScreen,
  Notification,
  UserDropdown,
} from '@vben/layouts';
import { preferences, usePreferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';

import { Modal } from 'ant-design-vue';

import { useAuthStore } from '#/store';
import AboutView from '#/views/_core/about/index.vue';
import LoginForm from '#/views/_core/authentication/login.vue';

const notifications = ref<NotificationItem[]>([]);
const aboutModalOpen = ref(false);

const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { destroyWatermark, updateWatermark } = useWatermark();
const { isDark } = usePreferences();

const showDot = computed(() =>
  notifications.value.some((item) => !item.isRead),
);

const userDisplayName = computed(() => {
  return (
    userStore.userInfo?.realName ||
    userStore.userInfo?.username ||
    '商城运营用户'
  );
});

const userRoleDesc = computed(() => {
  return (
    userStore.userInfo?.desc ||
    (userStore.userInfo?.username
      ? `用户ID: ${userStore.userInfo.username}`
      : '')
  );
});

const userRoleTag = computed(() => {
  const role = userStore.userInfo?.roles?.[0];
  if (role === 'BUYER') return '买家端';
  if (role === 'MERCHANT') return '商家端';
  if (role === 'SYSTEM') return '系统治理';
  return role || '';
});

const menus = computed(() => [
  {
    handler: () => {
      const homePath =
        userStore.userInfo?.homePath ||
        preferences.app.defaultHomePath ||
        '/buyer/workspace';
      router.push(homePath);
    },
    icon: 'lucide:layout-dashboard',
    text: '工作台首页',
  },
  {
    handler: () => {
      const roles = userStore.userInfo?.roles || [];
      if (roles.includes('MERCHANT')) {
        router.push('/merchant/account');
      } else if (roles.includes('BUYER')) {
        router.push('/buyer/account');
      } else {
        router.push(userStore.userInfo?.homePath || '/buyer/workspace');
      }
    },
    icon: 'lucide:user-round-cog',
    text: '账号设置',
  },
  {
    handler: () => {
      aboutModalOpen.value = true;
    },
    icon: 'lucide:info',
    text: '关于平台',
  },
]);

const avatar = computed(() => {
  return userStore.userInfo?.avatar ?? preferences.app.defaultAvatar;
});

async function handleLogout() {
  await authStore.logout(false);
}

function handleNoticeClear() {
  notifications.value = [];
}

function markRead(id: number | string) {
  const item = notifications.value.find((item) => item.id === id);
  if (item) {
    item.isRead = true;
  }
}

function remove(id: number | string) {
  notifications.value = notifications.value.filter((item) => item.id !== id);
}

function handleMakeAll() {
  notifications.value.forEach((item) => (item.isRead = true));
}

const viewAll = () => {};

const handleClick = (item: NotificationItem) => {
  if (item.link) {
    navigateTo(item.link, item.query, item.state);
  }
};

function navigateTo(
  link: string,
  query?: Record<string, any>,
  state?: Record<string, any>,
) {
  if (link.startsWith('http://') || link.startsWith('https://')) {
    window.open(link, '_blank');
  } else {
    router.push({
      path: link,
      query: query || {},
      state,
    });
  }
}

watch(
  () => ({
    content: preferences.app.watermarkContent,
    enable: preferences.app.watermark,
    isDark: isDark.value,
  }),
  async ({ content, enable, isDark: isDarkValue }) => {
    if (enable) {
      const watermarkColor = isDarkValue
        ? 'rgba(255, 255, 255, 0.12)'
        : 'rgba(0, 0, 0, 0.12)';

      await updateWatermark({
        advancedStyle: {
          colorStops: [
            {
              color: watermarkColor,
              offset: 0,
            },
            {
              color: watermarkColor,
              offset: 1,
            },
          ],
          type: 'linear',
        },
        content:
          content ||
          `${userStore.userInfo?.username || ''} - ${userStore.userInfo?.realName || ''}`,
      });
    } else {
      destroyWatermark();
    }
  },
  {
    immediate: true,
  },
);
</script>

<template>
  <BasicLayout
    :avatar
    :text="userDisplayName"
    @clear-preferences-and-logout="handleLogout"
    @logout="handleLogout"
  >
    <template #user-dropdown>
      <UserDropdown
        :avatar
        :description="userRoleDesc"
        :menus
        :tag-text="userRoleTag"
        :text="userDisplayName"
        @clear-preferences-and-logout="handleLogout"
        @logout="handleLogout"
      />
    </template>
    <template #notification>
      <Notification
        :dot="showDot"
        :notifications="notifications"
        @clear="handleNoticeClear"
        @make-all="handleMakeAll"
        @on-click="handleClick"
        @read="(item) => item.id && markRead(item.id)"
        @remove="(item) => item.id && remove(item.id)"
        @view-all="viewAll"
      />
    </template>
    <template #extra>
      <AuthenticationLoginExpiredModal
        v-model:open="accessStore.loginExpired"
        :avatar
      >
        <LoginForm />
      </AuthenticationLoginExpiredModal>

      <Modal
        v-model:open="aboutModalOpen"
        :footer="null"
        :width="760"
        centered
        destroy-on-close
      >
        <AboutView />
      </Modal>
    </template>
    <template #lock-screen>
      <LockScreen :avatar @to-login="handleLogout" />
    </template>
  </BasicLayout>
</template>
