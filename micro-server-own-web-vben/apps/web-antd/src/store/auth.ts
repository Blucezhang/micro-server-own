import type { Recordable, UserInfo } from '@vben/types';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';

import { message } from 'ant-design-vue';
import { defineStore } from 'pinia';

import { marketplaceAuthApi } from '#/api/marketplace';
import { marketplaceSession } from '#/api/marketplace-request';

const roleHome = {
  BUYER: '/buyer/workspace',
  MERCHANT: '/merchant/workspace',
  SYSTEM: '/system/workspace',
} as const;

type MarketplaceActor = keyof typeof roleHome;
const uiDemoEnabled = import.meta.env.DEV;

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();
  const loginLoading = ref(false);

  async function fetchUserInfo() {
    const authorization = await marketplaceAuthApi.authorization();
    const userInfo: UserInfo = {
      avatar: '',
      desc: `${authorization.actorType} 账户`,
      homePath: roleHome[authorization.actorType as keyof typeof roleHome],
      realName: `${authorization.actorType} 用户`,
      roles: [authorization.actorType, ...authorization.roles],
      token: marketplaceSession.get()?.accessToken || '',
      userId: String(authorization.userId),
      username: String(authorization.userId),
    };
    accessStore.setAccessCodes(authorization.permissions);
    userStore.setUserInfo(userInfo);
    return userInfo;
  }

  async function authLogin(params: Recordable<any>) {
    try {
      loginLoading.value = true;
      const tokenPair = await marketplaceAuthApi.login({
        actorType: params.actorType || 'BUYER',
        loginUserName: params.username,
        password: params.password,
      });
      marketplaceSession.set(tokenPair);
      accessStore.setAccessToken(tokenPair.accessToken);
      const userInfo = await fetchUserInfo();
      accessStore.setIsAccessChecked(false);
      await router.push(
        decodeURIComponent(
          (router.currentRoute.value.query.redirect as string) ||
            userInfo.homePath,
        ),
      );
      message.success('登录成功');
      return { userInfo };
    } finally {
      loginLoading.value = false;
    }
  }

  async function enterUiDemo(actorType: MarketplaceActor) {
    if (!uiDemoEnabled) return;
    loginLoading.value = true;
    try {
      marketplaceSession.clear();
      accessStore.setAccessToken(`ui-demo-${actorType.toLowerCase()}`);
      accessStore.setAccessCodes([]);
      accessStore.setIsAccessChecked(false);
      userStore.setUserInfo({
        avatar: '',
        desc: '本地 UI 演示身份',
        homePath: roleHome[actorType],
        realName: `${actorType} 演示用户`,
        roles: [actorType],
        token: `ui-demo-${actorType.toLowerCase()}`,
        userId: `demo-${actorType.toLowerCase()}`,
        username: `demo-${actorType.toLowerCase()}`,
      });
      await router.push(roleHome[actorType]);
      message.info('已进入本地 UI 演示模式，未连接业务网关');
    } finally {
      loginLoading.value = false;
    }
  }

  async function logout(redirect = true) {
    const refreshToken = marketplaceSession.get()?.refreshToken;
    if (refreshToken) {
      await marketplaceAuthApi.logout(refreshToken).catch(() => undefined);
    }
    marketplaceSession.clear();
    resetAllStores();
    accessStore.setLoginExpired(false);
    if (redirect) await router.replace(LOGIN_PATH);
  }

  function $reset() {
    loginLoading.value = false;
  }

  return { $reset, authLogin, enterUiDemo, fetchUserInfo, loginLoading, logout, uiDemoEnabled };
});
