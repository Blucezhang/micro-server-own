import { describe, expect, it, vi } from 'vitest';

const { requests } = vi.hoisted(() => ({ requests: [] as Record<string, any>[] }));

vi.mock('./marketplace-request', () => ({
  marketplaceRequest: vi.fn(async (config: Record<string, any>) => {
    requests.push(config);
    return [];
  }),
}));

import { buyerApi } from './marketplace';

describe('buyer coupon API', () => {
  it('reads the current buyer wallet', async () => {
    await buyerApi.coupons();
    expect(requests.at(-1)).toMatchObject({
      method: 'GET',
      url: '/sale/api/v1/coupons/me',
    });
  });

  it('claims a merchant template using the supplied retry key', async () => {
    await buyerApi.claimMerchantCoupon(12, 'claim-attempt-1');
    expect(requests.at(-1)).toMatchObject({
      data: { couponType: 'STORE', merchantTemplateId: 12 },
      headers: { 'Idempotency-Key': 'claim-attempt-1' },
      method: 'POST',
      url: '/sale/api/v1/coupons/claim',
    });
  });
});
