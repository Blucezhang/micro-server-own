// Browser-only fixtures: no requests from the layout checks reach a business service.
export const product = {
  id: 7, partyId: 8, categoryId: 1, name: '布局验收商品 · 长标题与多规格展示',
  skuCode: 'LAYOUT-TEST-LONG-SKU-00000000000000000000000007',
  originalPrice: '199.00', promotionPrice: '169.00', saleStatus: 'AVAILABLE',
  content: '仅用于浏览器布局验收的隔离数据。', specification: '标准款',
};
const address = {
  id: 1, recipientName: '测试收货人', mobile: '13800000000', province: '测试省',
  city: '测试市', district: '测试区', detail: '用于检查长地址自动换行的示例地址一号楼一单元', defaultAddress: true,
};
const page = (items = []) => ({ items, total: items.length, page: 0, size: 20 });

export function fixture(path) {
  if (path.endsWith('/account/profile')) return { loginUserId: 1, loginName: 'layout-test', name: '布局测试', email: 'layout@example.test', phone: '13800000000' };
  if (path.endsWith('/sessions')) return [];
  if (path.endsWith('/addresses')) return [address];
  if (path.endsWith('/categories')) return [{ id: 1, name: '测试分类' }];
  if (path.endsWith('/favorites')) return [{ id: 1, productId: 7, createdAt: '2026-10-01T08:00:00Z' }];
  if (/\/products\/7$/.test(path)) return product;
  if (path.endsWith('/products')) return page([product]);
  if (path.endsWith('/products/7/reviews')) return page();
  if (path.endsWith('/cart/items')) return [];
  if (path.endsWith('/coupons/me')) return [];
  if (path.endsWith('/merchant/templates')) return [];
  if (path.endsWith('/freight-rule')) return { fixedAmount: '8.00', freeThreshold: '199.00' };
  if (path.endsWith('/settlement/balance')) return { availableAmount: '120.00' };
  if (path.endsWith('/low-stock-alerts')) return [];
  if (path.endsWith('/review-reports')) return page();
  if (path.endsWith('/checkouts/quote')) return { payableAmount: '169.00', freightAmount: '0.00' };
  if (/\/page$|\/sub-orders$|\/price-audits$/.test(path)) return page();
  throw new Error(`Missing layout fixture: ${path}`);
}

export async function installFixtures(context, origin, unexpected) {
  await context.route('**/*', async (route) => {
    const url = new URL(route.request().url());
    if (url.origin !== origin) return route.abort();
    if (!url.pathname.startsWith('/api/')) return route.continue();
    try {
      const data = fixture(url.pathname.slice(4));
      return route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ status: 200, data, message: 'layout fixture' }) });
    } catch (error) {
      unexpected.push(error.message);
      return route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ status: 500, message: error.message }) });
    }
  });
}
