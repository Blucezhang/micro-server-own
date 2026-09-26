import { describe, expect, it } from 'vitest';

import { merchantCouponRows, preferredAddressId } from './marketplace-models';

describe('marketplace response contracts', () => {
  it('selects the backend defaultAddress rather than the first address', () => {
    const base = { city: '北京', detail: '测试路', district: '朝阳', mobile: '13800138000', province: '北京', recipientName: '测试' };
    expect(preferredAddressId([
      { ...base, defaultAddress: false, id: 1 },
      { ...base, defaultAddress: true, id: 2 },
    ])).toBe(2);
    expect(preferredAddressId([])).toBeUndefined();
  });

  it('flattens the backend coupon template envelope for row actions', () => {
    expect(merchantCouponRows([{
      availableQuantity: 3,
      template: { discountAmount: '5', expiresAt: 1_800_000_000_000, id: 7, minimumAmount: '50', name: '满减券', status: 'ACTIVE', totalQuantity: 3 },
    }])).toEqual([{ availableQuantity: 3, discountAmount: '5', expiresAt: 1_800_000_000_000, id: 7, minimumAmount: '50', name: '满减券', status: 'ACTIVE', totalQuantity: 3 }]);
  });
});
