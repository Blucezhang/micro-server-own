import type { AfterSale } from './marketplace';

export const afterSaleStatusNames: Record<AfterSale['status'], string> = {
  APPLYING: '待审核', APPROVED: '已同意，待退货', CLOSED: '已关闭', EXCHANGED: '换货完成',
  EXCHANGE_PENDING_SHIPMENT: '待寄换货', EXCHANGE_SHIPPED: '换货运输中', RECEIVED: '已收退货',
  REFUNDED: '已退款', REFUND_PENDING: '退款处理中', REJECTED: '已驳回', RETURNING: '买家已退货',
};

export const afterSaleTypeNames: Record<AfterSale['type'], string> = {
  EXCHANGE: '换货', REFUND_ONLY: '仅退款', RETURN_AND_REFUND: '退货退款',
};

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
