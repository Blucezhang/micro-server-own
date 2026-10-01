import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@vben/request', () => ({
  RequestClient: class {
    addRequestInterceptor() {}
    addResponseInterceptor() {}
  },
}));
vi.mock('ant-design-vue', () => ({ message: { error: vi.fn() } }));

describe('marketplace persisted session', () => {
  beforeEach(() => {
    vi.resetModules();
    sessionStorage.clear();
  });

  it('discards malformed JSON instead of preventing the app from loading', async () => {
    sessionStorage.setItem('micro-market-vben-session', '{invalid');
    const { marketplaceSession } = await import('./marketplace-request');
    expect(marketplaceSession.get()).toBeNull();
    expect(sessionStorage.getItem('micro-market-vben-session')).toBeNull();
  });

  it('discards a stored object without both tokens', async () => {
    sessionStorage.setItem('micro-market-vben-session', '{"accessToken":"only"}');
    const { marketplaceSession } = await import('./marketplace-request');
    expect(marketplaceSession.get()).toBeNull();
  });

  it('restores a valid token pair', async () => {
    sessionStorage.setItem('micro-market-vben-session', '{"accessToken":"access","refreshToken":"refresh"}');
    const { marketplaceSession } = await import('./marketplace-request');
    expect(marketplaceSession.get()).toEqual({ accessToken: 'access', refreshToken: 'refresh' });
  });
});
