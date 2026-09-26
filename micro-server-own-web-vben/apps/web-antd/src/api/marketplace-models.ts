export interface BuyerAddress {
  id: number;
  recipientName: string;
  mobile: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  defaultAddress: boolean;
}

export interface MerchantCouponRow {
  id: number;
  name: string;
  status: string;
  minimumAmount: number | string;
  discountAmount: number | string;
  totalQuantity: number;
  claimStartsAt?: number | string;
  claimEndsAt?: number | string;
  expiresAt: number | string;
  availableQuantity: number;
}

export function preferredAddressId(addresses: BuyerAddress[]): number | undefined {
  return addresses.find((address) => address.defaultAddress)?.id ?? addresses[0]?.id;
}

export function merchantCouponRows(
  data: { availableQuantity: number; template: Omit<MerchantCouponRow, 'availableQuantity'> & Record<string, unknown> }[],
): MerchantCouponRow[] {
  return data.map(({ availableQuantity, template }) => ({ ...template, availableQuantity }));
}
