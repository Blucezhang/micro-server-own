import { describe, expect, it, vi } from 'vitest';

const { errorToast, interceptors, responseRejectors } = vi.hoisted(() => ({
  errorToast: vi.fn(),
  interceptors: [] as ((config: any) => any)[],
  responseRejectors: [] as ((error: any) => Promise<unknown>)[],
}));

vi.mock('@vben/request', () => ({
  RequestClient: class {
    addRequestInterceptor(interceptor: any) { interceptors.push(interceptor.fulfilled); }
    addResponseInterceptor(interceptor: any) { responseRejectors.push(interceptor.rejected); }
  },
}));
vi.mock('ant-design-vue', () => ({ message: { error: errorToast } }));

import './marketplace-request';

describe('business request idempotency', () => {
  it('preserves a command key when the same request is retried', () => {
    const config = { headers: { 'Idempotency-Key': 'checkout-attempt-1' }, method: 'POST', url: '/order/api/v1/orders' };
    expect(interceptors[0]!(config).headers['Idempotency-Key']).toBe('checkout-attempt-1');
    expect(interceptors[0]!(config).headers['Idempotency-Key']).toBe('checkout-attempt-1');
  });

  it('generates a key only when a business command does not supply one', () => {
    const config = { headers: {}, method: 'POST', url: '/order/api/v1/orders' };
    const key = interceptors[0]!(config).headers['Idempotency-Key'];
    expect(key).toBeTruthy();
    expect(interceptors[0]!(config).headers['Idempotency-Key']).toBe(key);
  });
});

describe('request error presentation', () => {
  it('leaves failed GET feedback to the view without a duplicate toast', async () => {
    errorToast.mockClear();
    const error = { config: { method: 'GET', url: '/product/api/v1/products' }, response: { status: 502 } };
    await expect(responseRejectors[0]!(error)).rejects.toBe(error);
    expect(errorToast).not.toHaveBeenCalled();
  });

  it('leaves checkout quote feedback to the view without a duplicate toast', async () => {
    errorToast.mockClear();
    const error = { config: { method: 'POST', url: '/order/api/v1/checkouts/quote' }, response: { status: 502 } };
    await expect(responseRejectors[0]!(error)).rejects.toBe(error);
    expect(errorToast).not.toHaveBeenCalled();
  });

  it('keeps the global toast for a failed business write', async () => {
    errorToast.mockClear();
    const error = { config: { method: 'POST', url: '/order/api/v1/orders' }, response: { status: 502 } };
    await expect(responseRejectors[0]!(error)).rejects.toBe(error);
    expect(errorToast).toHaveBeenCalledWith('服务暂时不可用，请稍后重试');
  });

  it('shows a Chinese service error instead of an upstream English status', async () => {
    errorToast.mockClear();
    const error = {
      config: { method: 'POST', url: '/product/api/v1/merchant/reviews/7011/reply' },
      response: { data: { message: 'Service Unavailable' }, status: 503 },
    };
    await expect(responseRejectors[0]!(error)).rejects.toBe(error);
    expect(errorToast).toHaveBeenCalledWith('服务暂时不可用（503），请稍后重试');
  });
});
