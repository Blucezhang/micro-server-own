import assert from 'node:assert/strict';
import { mkdir } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import { chromium } from 'playwright';

const origin = process.env.MALL_UI_URL || 'http://127.0.0.1:5180';
assert.ok(['127.0.0.1', 'localhost'].includes(new URL(origin).hostname), 'Showcase capture only supports localhost');

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../../../docs/frontend/screenshots');
await mkdir(root, { recursive: true });

const products = [
  { id: 701, partyId: 1024, categoryId: 8, name: '晨间手冲咖啡豆 · 花香日晒', skuCode: 'COFFEE-ETH-250G', originalPrice: '98.00', promotionPrice: '78.00', saleStatus: 'AVAILABLE', content: '来自耶加雪菲产区的浅烘焙单品豆，带有柑橘与白花香气。', specification: '250g / 袋' },
  { id: 702, partyId: 1024, categoryId: 8, name: '不锈钢细口手冲壶', skuCode: 'BREW-KETTLE-600', originalPrice: '268.00', promotionPrice: '229.00', saleStatus: 'AVAILABLE', content: '细口控水，适合日常手冲。', specification: '600ml / 雾面银' },
  { id: 703, partyId: 1024, categoryId: 9, name: '锥形滤杯与滤纸组合', skuCode: 'DRIP-SET-02', originalPrice: '129.00', promotionPrice: '99.00', saleStatus: 'OFF_SHELF', content: '锥形滤杯搭配原木底座。', specification: '02 号 / 套装' },
];
const orderNo = 'MSO-20261001-10001';
const subOrderNo = 'SUB-20261001-10001-01';
const order = { orderNo, status: 'SHIPPED', payableAmount: '307.00', createdAt: '2026-10-01T10:24:00+08:00', shippingAddress: '上海市浦东新区示例路 88 号' };
const subOrder = { subOrderNo, status: 'SHIPPED', merchantId: 1024, logisticsCompany: '顺丰速运', trackingNo: 'SF-DEMO-2026100101' };
const orderDetail = {
  order,
  subOrders: [subOrder],
  items: [
    { id: 9101, subOrderNo, productName: products[0].name, quantity: 2, lineTotal: '156.00' },
    { id: 9102, subOrderNo, productName: products[1].name, quantity: 1, lineTotal: '151.00' },
  ],
};
const reviews = [
  { id: 8011, rating: 5, content: '香气明亮，手冲口感干净，回甘很好。', merchantReply: '感谢喜欢，建议使用 91℃ 左右水温。' },
  { id: 8012, rating: 4, content: '包装完整，风味稳定，日常喝很合适。', merchantReply: '谢谢反馈，我们会继续做好烘焙批次管理。' },
];
const reports = [
  { id: 5101, orderNo: 'MSO-20260928-00842', productName: '晨间手冲咖啡豆 · 花香日晒', reviewContent: '商品描述与收到的规格不符', reason: '疑似不实描述', status: 'PENDING', createdAt: '2026-10-01 09:18' },
  { id: 5102, orderNo: 'MSO-20260927-00763', productName: '不锈钢细口手冲壶', reviewContent: '联系我领取站外优惠', reason: '疑似引导站外交易', status: 'PENDING', createdAt: '2026-09-30 16:42' },
];

function page(items) {
  return { items, total: items.length, page: 0, size: 20 };
}

function response(path) {
  if (path.endsWith('/account/profile')) return { loginUserId: 2026, loginName: 'showcase-buyer', name: '演示买家' };
  if (path.endsWith('/sessions')) return [];
  if (path.endsWith('/categories')) return [{ id: 8, name: '咖啡器具' }, { id: 9, name: '手冲周边' }];
  if (path.endsWith('/favorites')) return [{ id: 3001, productId: products[0].id, createdAt: '2026-10-01T08:00:00+08:00' }];
  if (path.endsWith('/products/701/reviews')) return page(reviews);
  if (/\/products\/\d+$/.test(path)) return products.find((item) => path.endsWith(`/${item.id}`)) || products[0];
  if (path.endsWith('/products')) return page(products);
  if (path.endsWith('/orders/page')) return page([order]);
  if (path.endsWith(`/orders/${orderNo}/detail`)) return orderDetail;
  if (path.endsWith(`/sub-orders/${subOrderNo}/logistics-traces`)) {
    return [
      { createdAt: '2026-10-02T08:30:00+08:00', traceStatus: '派送中', detail: '快件正在派送，请留意查收' },
      { createdAt: '2026-10-01T18:10:00+08:00', traceStatus: '已发出', detail: '快件已从上海转运中心发出' },
    ];
  }
  if (path.endsWith('/merchant/products')) return page(products);
  if (path.endsWith('/merchant/settlement/balance')) return { availableAmount: '12860.50' };
  if (path.endsWith('/system/review-reports')) return page(reports);
  if (path.endsWith('/coupons/me') || path.endsWith('/merchant/templates') || path.endsWith('/cart/items')) return [];
  throw new Error(`Missing showcase fixture for ${path}`);
}

const unexpected = [];
const browser = await chromium.launch({ headless: true, ...(process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE ? { executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE } : {}) });

async function installFixtures(context) {
  await context.route('**/*', async (route) => {
    const url = new URL(route.request().url());
    if (url.origin !== origin) return route.abort();
    if (!url.pathname.startsWith('/api/')) return route.continue();
    try {
      const data = response(url.pathname.slice(4));
      return route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ status: 200, data, message: 'README showcase fixture' }) });
    } catch (error) {
      unexpected.push(error.message);
      return route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ status: 500, message: error.message }) });
    }
  });
}

async function enterRole(role, label) {
  const context = await browser.newContext({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1, reducedMotion: 'reduce' });
  await installFixtures(context);
  const page = await context.newPage();
  page.on('pageerror', (error) => unexpected.push(error.message));
  await page.goto(`${origin}/auth/login`);
  await page.getByRole('button', { name: label, exact: true }).click();
  await page.waitForURL(`**/${role}/workspace`);
  return { context, page };
}

async function capture(page, path, fileName) {
  await page.goto(`${origin}${path}`);
  await page.locator('.market-heading').waitFor();
  await page.waitForFunction(() => !document.querySelector('.ant-spin-spinning'));
  await page.waitForTimeout(450);
  await page.screenshot({ path: resolve(root, fileName), fullPage: true });
  console.log(`Captured ${fileName}`);
}

try {
  const buyer = await enterRole('buyer', '买家端演示');
  await capture(buyer.page, '/buyer/workspace', 'buyer-workspace.png');
  await capture(buyer.page, '/buyer/catalog', 'buyer-catalog.png');
  await buyer.page.goto(`${origin}/buyer/orders`);
  await buyer.page.locator('.market-heading').waitFor();
  await buyer.page.waitForFunction(() => !document.querySelector('.ant-spin-spinning'));
  await buyer.page.getByRole('button', { name: '详情', exact: true }).click();
  await buyer.page.getByRole('button', { name: '物流轨迹', exact: true }).waitFor();
  await buyer.page.getByRole('button', { name: '物流轨迹', exact: true }).click();
  await buyer.page.getByText('快件正在派送，请留意查收', { exact: true }).waitFor();
  await buyer.page.waitForFunction(() => !document.querySelector('.ant-spin-spinning'));
  await buyer.page.setViewportSize({ width: 1440, height: 1500 });
  await buyer.page.waitForTimeout(450);
  await buyer.page.screenshot({ path: resolve(root, 'buyer-order-detail.png'), fullPage: true });
  await buyer.context.close();

  const merchant = await enterRole('merchant', '商家端演示');
  await merchant.page.goto(`${origin}/merchant/products`);
  await merchant.page.locator('.market-heading').waitFor();
  await merchant.page.waitForFunction(() => !document.querySelector('.ant-spin-spinning'));
  const productInputs = merchant.page.locator('[aria-labelledby="product-form-title"] input');
  for (const [index, value] of ['1024', '8', '晨间手冲咖啡豆 · 花香日晒', 'COFFEE-ETH-250G', '98', '78'].entries()) {
    await productInputs.nth(index).fill(value);
  }
  await merchant.page.waitForTimeout(450);
  await merchant.page.screenshot({ path: resolve(root, 'merchant-products.png'), fullPage: true });
  await merchant.page.goto(`${origin}/merchant/settlement`);
  await merchant.page.locator('.market-heading').waitFor();
  await merchant.page.waitForFunction(() => !document.querySelector('.ant-spin-spinning'));
  await merchant.page.locator('.market-grid input').fill('3480');
  await merchant.page.waitForTimeout(450);
  await merchant.page.screenshot({ path: resolve(root, 'merchant-settlement.png'), fullPage: true });
  await merchant.context.close();

  const system = await enterRole('system', '系统治理演示');
  await system.page.goto(`${origin}/system/reviews`);
  await system.page.locator('.market-heading').waitFor();
  await system.page.waitForFunction(() => !document.querySelector('.ant-spin-spinning'));
  const moderationInputs = system.page.locator('.resolve-form input');
  await moderationInputs.nth(0).fill('5101');
  await system.page.locator('.resolve-form .ant-input').fill('已核查商品详情与评价内容，按平台规则跟进。');
  await system.page.waitForTimeout(450);
  await system.page.screenshot({ path: resolve(root, 'system-moderation.png'), fullPage: true });
  await system.context.close();

  assert.deepEqual(unexpected, [], 'Showcase must not produce browser or fixture errors');
  console.log(`Showcase screenshots saved to ${root}`);
} finally {
  await browser.close();
}
