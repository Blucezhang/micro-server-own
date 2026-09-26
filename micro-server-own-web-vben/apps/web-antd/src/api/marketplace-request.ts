import type { RequestClientConfig } from '@vben/request';

import { RequestClient } from '@vben/request';

import { message } from 'ant-design-vue';

export interface ApiResponse<T> {
  data: T;
  message: string;
  status: number;
}

export interface TokenPair {
  accessToken: string;
  expiresIn?: number;
  refreshToken: string;
}

const sessionKey = 'micro-market-vben-session';
let tokenPair: null | TokenPair = JSON.parse(
  sessionStorage.getItem(sessionKey) || 'null',
);
let refreshRequest: null | Promise<TokenPair> = null;

export const marketplaceSession = {
  clear() {
    tokenPair = null;
    sessionStorage.removeItem(sessionKey);
  },
  get() {
    return tokenPair;
  },
  set(value: TokenPair) {
    tokenPair = value;
    sessionStorage.setItem(sessionKey, JSON.stringify(value));
  },
};

const client = new RequestClient({ baseURL: '/api', timeout: 15_000 });
const baseClient = new RequestClient({ baseURL: '/api', timeout: 15_000 });

function isBusinessWrite(config: RequestClientConfig) {
  const method = config.method?.toLowerCase();
  return (
    ['delete', 'patch', 'post', 'put'].includes(method || '') &&
    !config.url?.includes('/login/')
  );
}

function createCorrelationId() {
  return crypto.randomUUID();
}

function notifyError(error: any) {
  if (!error?.response) {
    message.error('无法连接本地网关（http://localhost:9632）。请先启动后端服务。');
    return;
  }
  if (error.response.status === 403) {
    message.error('无此权限');
    return;
  }
  message.error(error.response.data?.message || '服务暂时不可用，请稍后重试');
}

async function refreshToken() {
  const refreshToken = marketplaceSession.get()?.refreshToken;
  if (!refreshToken) throw new Error('Missing refresh token');
  const response = await baseClient.post<ApiResponse<TokenPair>>(
    '/user/login/refresh',
    { refreshToken },
  );
  const tokenPair = (response as any).data.data as TokenPair;
  marketplaceSession.set(tokenPair);
  return tokenPair;
}

client.addRequestInterceptor({
  fulfilled: (config) => {
    const accessToken = marketplaceSession.get()?.accessToken;
    if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
    config.headers['X-Correlation-Id'] = createCorrelationId();
    if (isBusinessWrite(config)) config.headers['Idempotency-Key'] = crypto.randomUUID();
    return config;
  },
});

client.addResponseInterceptor({
  rejected: async (error: any) => {
    const config = error.config as (RequestClientConfig & { retried?: boolean }) | undefined;
    if (error.response?.status === 401 && config && !config.retried && marketplaceSession.get()) {
      config.retried = true;
      try {
        refreshRequest ||= refreshToken().finally(() => {
          refreshRequest = null;
        });
        await refreshRequest;
        return client.request(config.url || '', config);
      } catch {
        marketplaceSession.clear();
        location.assign('/auth/login');
      }
    }
    notifyError(error);
    return Promise.reject(error);
  },
});

export async function marketplaceRequest<T>(config: RequestClientConfig) {
  const response = await client.request<ApiResponse<T>>(config.url || '', config);
  return (response as any).data.data as T;
}
