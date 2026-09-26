import { marketplaceRequest } from './marketplace-request';

export interface Authorization {
  actorId: number;
  actorType: 'BUYER' | 'MERCHANT' | 'SYSTEM';
  permissions: string[];
  roles: string[];
  userId: number;
}

export const marketplaceAuthApi = {
  authorization: () =>
    marketplaceRequest<Authorization>({
      method: 'GET',
      url: '/user/api/v1/account/authorization',
    }),
  login: (payload: {
    actorType: Authorization['actorType'];
    loginUserName: string;
    password: string;
  }) =>
    marketplaceRequest<import('./marketplace-request').TokenPair>({
      data: payload,
      method: 'POST',
      url: '/user/login/token',
    }),
  logout: (refreshToken: string) =>
    marketplaceRequest({
      data: { refreshToken },
      method: 'POST',
      url: '/user/login/logout',
    }),
};

export const catalogApi = {
  products: (params: Record<string, unknown>) =>
    marketplaceRequest({
      method: 'GET',
      params,
      url: '/product/api/v1/products',
    }),
  categories: () => marketplaceRequest({ method: 'GET', url: '/product/api/v1/categories' }),
  product: (id: number) => marketplaceRequest({ method: 'GET', url: `/product/api/v1/products/${id}` }),
};

export const buyerApi = {
  quote: (data: Record<string, unknown>) => marketplaceRequest({ data, method: 'POST', url: '/order/api/v1/checkouts/quote' }),
  createOrder: (data: Record<string, unknown>) => marketplaceRequest({ data, method: 'POST', url: '/order/api/v1/orders' }),
  afterSales: (params: Record<string, unknown>) => marketplaceRequest({ method: 'GET', params, url: '/order/api/v1/after-sales/page' }),
  createAfterSale: (data: Record<string, unknown>) => marketplaceRequest({ data, method: 'POST', url: '/order/api/v1/after-sales' }),
  closeAfterSale: (no: string) => marketplaceRequest({ method: 'POST', url: `/order/api/v1/after-sales/${no}/close` }),
  returnShipment: (no: string, data: Record<string, unknown>) => marketplaceRequest({ data, method: 'POST', url: `/order/api/v1/after-sales/${no}/return-shipment` }),
  addresses: () =>
    marketplaceRequest({ method: 'GET', url: '/user/api/v1/addresses' }),
  defaultAddress: (id: number) =>
    marketplaceRequest({ method: 'POST', url: `/user/api/v1/addresses/${id}/default` }),
  deleteAddress: (id: number) =>
    marketplaceRequest({ method: 'DELETE', url: `/user/api/v1/addresses/${id}` }),
  saveAddress: (data: Record<string, unknown>, id?: number) =>
    marketplaceRequest({
      data,
      method: id ? 'PUT' : 'POST',
      url: id ? `/user/api/v1/addresses/${id}` : '/user/api/v1/addresses',
    }),
  orders: (params: Record<string, unknown>) =>
    marketplaceRequest({
      method: 'GET',
      params,
      url: '/order/api/v1/orders/page',
    }),
  orderDetail: (no: string) => marketplaceRequest({ method: 'GET', url: `/order/api/v1/orders/${no}/detail` }),
  cancelOrder: (no: string) => marketplaceRequest({ method: 'POST', url: `/order/api/v1/orders/${no}/cancel` }),
  receiveSubOrder: (no: string) => marketplaceRequest({ method: 'POST', url: `/order/api/v1/sub-orders/${no}/receive` }),
  logisticsTraces: (no: string) => marketplaceRequest({ method: 'GET', url: `/order/api/v1/sub-orders/${no}/logistics-traces` }),
};

export const cartApi = {
  list: () => marketplaceRequest({ method: 'GET', url: '/order/api/v1/cart/items' }),
  add: (productId: number, quantity: number) => marketplaceRequest({ data: { productId, quantity }, method: 'POST', url: '/order/api/v1/cart/items' }),
  remove: (id: number) => marketplaceRequest({ method: 'DELETE', url: `/order/api/v1/cart/items/${id}` }),
  update: (id: number, quantity: number) => marketplaceRequest({ data: { quantity }, method: 'PUT', url: `/order/api/v1/cart/items/${id}` }),
};

export const paymentApi = {
  create: (data: { channel: string; orderNo: string }) => marketplaceRequest({ data, method: 'POST', url: '/settlement/api/v1/payments' }),
  detail: (no: string) => marketplaceRequest({ method: 'GET', url: `/settlement/api/v1/payments/${no}` }),
};

export const merchantApi = {
  saveProduct: (data: Record<string, unknown>, id?: number) => marketplaceRequest({ data, method: id ? 'PUT' : 'POST', url: id ? `/product/api/v1/merchant/products/${id}` : '/product/api/v1/merchant/products' }),
  changeProductStatus: (id: number, value: 'AVAILABLE' | 'OFF_SHELF') => marketplaceRequest({ method: 'POST', params: { value }, url: `/product/api/v1/merchant/products/${id}/sale-status` }),
  priceAudits: (id: number, page = 0) => marketplaceRequest({ method: 'GET', params: { page, size: 20 }, url: `/product/api/v1/merchant/products/${id}/price-audits` }),
  ship: (no: string, data: Record<string, unknown>) => marketplaceRequest({ data, method: 'POST', url: `/order/api/v1/sub-orders/${no}/ship` }),
  coupons: () => marketplaceRequest({ method: 'GET', url: '/sale/api/v1/coupons/merchant/templates' }),
  saveCoupon: (data: Record<string, unknown>, id?: number) => marketplaceRequest({ data, method: id ? 'PUT' : 'POST', url: id ? `/sale/api/v1/coupons/merchant/templates/${id}` : '/sale/api/v1/coupons/merchant/templates' }),
  changeCouponStatus: (id: number, value: string) => marketplaceRequest({ method: 'POST', params: { value }, url: `/sale/api/v1/coupons/merchant/templates/${id}/status` }),
  freight: () => marketplaceRequest({ method: 'GET', url: '/order/api/v1/merchant/freight-rule' }),
  saveFreight: (data: Record<string, unknown>) => marketplaceRequest({ data, method: 'PUT', url: '/order/api/v1/merchant/freight-rule' }),
  balance: () => marketplaceRequest({ method: 'GET', url: '/settlement/api/v1/merchant/settlement/balance' }),
  withdraw: (amount: string) => marketplaceRequest({ data: { amount }, method: 'POST', url: '/settlement/api/v1/merchant/settlement/withdrawals' }),
  lowStockAlerts: () =>
    marketplaceRequest({
      method: 'GET',
      url: '/inventory/api/v1/merchant/low-stock-alerts',
    }),
  stock: (productId: number, merchantId: number) => marketplaceRequest({ method: 'GET', params: { merchantId }, url: `/inventory/api/v1/stocks/${productId}` }),
  adjustStock: (productId: number, data: Record<string, unknown>) => marketplaceRequest({ data, method: 'POST', url: `/inventory/api/v1/stocks/${productId}/adjustments` }),
  saveLowStockRule: (productId: number, data: Record<string, unknown>) => marketplaceRequest({ data, method: 'PUT', url: `/inventory/api/v1/stocks/${productId}/low-stock-alert-rule` }),
  subOrders: (params: Record<string, unknown>) =>
    marketplaceRequest({
      method: 'GET',
      params,
      url: '/order/api/v1/merchant/sub-orders',
    }),
  products: (params: Record<string, unknown>) =>
    marketplaceRequest({
      method: 'GET',
      params,
      url: '/product/api/v1/merchant/products',
    }),
};

export const systemApi = {
  reviewReports: (params: Record<string, unknown>) =>
    marketplaceRequest({
      method: 'GET',
      params,
      url: '/product/api/v1/system/review-reports',
    }),
  resolveReviewReport: (id: number, data: { note: string; status: string }) => marketplaceRequest({ data, method: 'POST', url: `/product/api/v1/system/review-reports/${id}/resolve` }),
  grantMerchantRole: (loginUserId: number) => marketplaceRequest({ method: 'PUT', url: `/user/api/v1/system/accounts/${loginUserId}/merchant-role` }),
  grantRoleFunctions: (roleId: number, functionIds: number[]) => marketplaceRequest({ data: { functionIds }, method: 'PUT', url: `/user/api/v1/system/roles/${roleId}/functions` }),
};
