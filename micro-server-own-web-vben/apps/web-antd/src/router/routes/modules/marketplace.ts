import type { RouteRecordRaw } from 'vue-router';

const Workspace = () => import('#/views/marketplace/workspace.vue');
const BuyerCart = () => import('#/views/marketplace/buyer-cart.vue');
const BuyerAddresses = () => import('#/views/marketplace/buyer-addresses.vue');
const BuyerAfterSales = () => import('#/views/marketplace/buyer-after-sales.vue');
const BuyerCheckout = () => import('#/views/marketplace/buyer-checkout.vue');
const BuyerOrders = () => import('#/views/marketplace/buyer-orders.vue');
const BuyerCatalog = () => import('#/views/marketplace/buyer-catalog.vue');
const MerchantConsole = () => import('#/views/marketplace/merchant-console.vue');
const MerchantFulfillment = () => import('#/views/marketplace/merchant-fulfillment.vue');
const MerchantInventory = () => import('#/views/marketplace/merchant-inventory.vue');
const MerchantPriceAudits = () => import('#/views/marketplace/merchant-price-audits.vue');
const SystemAdministration = () => import('#/views/marketplace/system-administration.vue');
const SystemReviews = () => import('#/views/marketplace/system-reviews.vue');

const buyerChildren: RouteRecordRaw[] = [
  { component: Workspace, name: 'BuyerWorkspace', path: 'workspace', meta: { affixTab: true, icon: 'lucide:layout-dashboard', title: '买家工作台' } },
  { component: BuyerCatalog, name: 'BuyerCatalog', path: 'catalog', meta: { icon: 'lucide:store', title: '商品浏览' } },
  { component: BuyerCart, name: 'BuyerCart', path: 'cart', meta: { icon: 'lucide:shopping-cart', title: '购物车' } },
  { component: BuyerOrders, name: 'BuyerOrders', path: 'orders', meta: { icon: 'lucide:receipt-text', title: '订单中心' } },
  { component: BuyerAfterSales, name: 'BuyerAfterSales', path: 'after-sales', meta: { icon: 'lucide:rotate-ccw', title: '售后服务' } },
  { component: BuyerAddresses, name: 'BuyerAddresses', path: 'addresses', meta: { icon: 'lucide:map-pin', title: '收货地址' } },
  { component: BuyerCheckout, name: 'BuyerCheckout', path: 'checkout', meta: { hideInMenu: true, title: '确认结算' } },
];

const merchantChildren: RouteRecordRaw[] = [
  { component: Workspace, name: 'MerchantWorkspace', path: 'workspace', meta: { affixTab: true, icon: 'lucide:layout-dashboard', title: '商家工作台' } },
  { component: MerchantConsole, name: 'MerchantProducts', path: 'products', meta: { icon: 'lucide:package-search', title: '商品管理' } },
  { component: MerchantPriceAudits, name: 'MerchantPriceAudits', path: 'price-audits', meta: { icon: 'lucide:badge-dollar-sign', title: '价格审计' } },
  { component: MerchantInventory, name: 'MerchantInventory', path: 'inventory', meta: { icon: 'lucide:boxes', title: '库存管理' } },
  { component: MerchantFulfillment, name: 'MerchantOrders', path: 'orders', meta: { icon: 'lucide:truck', title: '履约工作台' } },
  { component: MerchantConsole, name: 'MerchantCoupons', path: 'coupons', meta: { icon: 'lucide:ticket-percent', title: '营销优惠' } },
  { component: MerchantConsole, name: 'MerchantFreight', path: 'freight', meta: { icon: 'lucide:package-open', title: '运费规则' } },
  { component: MerchantConsole, name: 'MerchantSettlement', path: 'settlement', meta: { icon: 'lucide:wallet-cards', title: '结算中心' } },
];

const systemChildren: RouteRecordRaw[] = [
  { component: Workspace, name: 'SystemWorkspace', path: 'workspace', meta: { affixTab: true, icon: 'lucide:layout-dashboard', title: '系统工作台' } },
  { component: SystemAdministration, name: 'SystemAccounts', path: 'accounts', meta: { icon: 'lucide:users-round', title: '账号与商家' } },
  { component: SystemAdministration, name: 'SystemRoles', path: 'roles', meta: { icon: 'lucide:shield-check', title: '角色权限' } },
  { component: SystemReviews, name: 'SystemReviews', path: 'reviews', meta: { icon: 'lucide:message-square-warning', title: '评价治理' } },
];

const routes: RouteRecordRaw[] = [
  { children: buyerChildren, meta: { authority: ['BUYER'], icon: 'lucide:shopping-bag', order: -3, title: '买家中心' }, name: 'Buyer', path: '/buyer' },
  { children: merchantChildren, meta: { authority: ['MERCHANT'], icon: 'lucide:store', order: -2, title: '商家运营' }, name: 'Merchant', path: '/merchant' },
  { children: systemChildren, meta: { authority: ['SYSTEM'], icon: 'lucide:settings-2', order: -1, title: '系统治理' }, name: 'System', path: '/system' },
];

export default routes;
