import { describe, expect, it, vi } from 'vitest';

const { interceptors } = vi.hoisted(() => ({ interceptors: [] as ((config: any) => any)[] }));

vi.mock('@vben/request', () => ({
  RequestClient: class {
    addRequestInterceptor(interceptor: any) { interceptors.push(interceptor.fulfilled); }
    addResponseInterceptor() {}
  },
}));
vi.mock('ant-design-vue', () => ({ message: { error: vi.fn() } }));

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
