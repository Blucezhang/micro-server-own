import type { Recordable, UserInfo } from '@vben/types';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { resetAllStores, useAccessStore, useTabbarStore, useUserStore } from '@vben/stores';
import { resetStaticRoutes } from '@vben/utils';

import { message } from 'ant-design-vue';
import { defineStore } from 'pinia';

import { marketplaceAuthApi } from '#/api/marketplace';
import { marketplaceSession } from '#/api/marketplace-request';
import { routes } from '#/router/routes';

const roleHome = {
  BUYER: '/buyer/workspace',
  MERCHANT: '/merchant/workspace',
  SYSTEM: '/system/workspace',
} as const;

type MarketplaceActor = keyof typeof roleHome;
const uiDemoEnabled = import.meta.env.DEV;

function demoUserInfo(actorType: MarketplaceActor): UserInfo {
  const token = `ui-demo-${actorType.toLowerCase()}`;
  return {
    avatar: '', desc: '本地 UI 演示身份', homePath: roleHome[actorType],
    realName: `${actorType} 演示用户`, roles: [actorType], token,
    userId: `demo-${actorType.toLowerCase()}`,
    username: `demo-${actorType.toLowerCase()}`,
  };
}

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();
  const loginLoading = ref(false);
  const actorId = ref<number>();

  function resetNavigationState() {
    resetStaticRoutes(router, routes);
    if (router.hasRoute('Root')) router.removeRoute('Root');
    const root = routes.find((route) => route.name === 'Root');
    if (root) router.addRoute({ ...root, children: [] });
    useTabbarStore().$reset();
  }

  async function fetchUserInfo() {
    const demoActor = uiDemoEnabled && /^ui-demo-(buyer|merchant|system)$/.exec(String(accessStore.accessToken || ''))?.[1]?.toUpperCase() as MarketplaceActor | undefined;
    if (demoActor) {
      actorId.value = undefined;
      const userInfo = demoUserInfo(demoActor);
      accessStore.setAccessCodes([]);
      userStore.setUserInfo(userInfo);
      return userInfo;
    }
    actorId.value = undefined;
    const authorization = await marketplaceAuthApi.authorization();
    actorId.value = authorization.actorId;
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
      resetNavigationState();
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
      actorId.value = undefined;
      resetNavigationState();
      accessStore.setAccessToken(`ui-demo-${actorType.toLowerCase()}`);
      accessStore.setAccessCodes([]);
      accessStore.setIsAccessChecked(false);
      userStore.setUserInfo(demoUserInfo(actorType));
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
    resetNavigationState();
    resetAllStores();
    accessStore.setLoginExpired(false);
    if (redirect) await router.replace(LOGIN_PATH);
  }

  function $reset() {
    loginLoading.value = false;
    actorId.value = undefined;
  }

  return { $reset, actorId, authLogin, enterUiDemo, fetchUserInfo, loginLoading, logout, uiDemoEnabled };
});
