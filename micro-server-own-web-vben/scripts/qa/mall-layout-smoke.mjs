import assert from 'node:assert/strict';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import process from 'node:process';

import { chromium } from 'playwright';

import { installFixtures, product } from './mall-layout-fixtures.mjs';

const origin = process.env.MALL_UI_URL || 'http://127.0.0.1:5180';
assert.ok(['127.0.0.1', 'localhost'].includes(new URL(origin).hostname), 'Layout tests must use a local server');
const output = process.env.MALL_UI_OUTPUT || join(tmpdir(), 'mall-layout-verification');
await mkdir(output, { recursive: true });
const source = await readFile(new URL('../../apps/web-antd/src/router/routes/modules/marketplace.ts', import.meta.url), 'utf8');
const groups = ['buyer', 'merchant', 'system'].map((actor) => {
  const body = source.match(new RegExp(`const ${actor}Children[^=]*= \\[([\\s\\S]*?)\\n\\];`))?.[1];
  assert.ok(body, `Missing route group: ${actor}`);
  return { actor, routes: [...body.matchAll(/path: '([^']+)'/g)].map((match) => `/${actor}/${match[1]}`) };
});
const onlyActor = process.env.MALL_UI_ONLY_ACTOR;
const onlyWidth = Number(process.env.MALL_UI_ONLY_WIDTH || 0);
const authOnly = process.env.MALL_UI_AUTH_ONLY === '1';
const traced = process.env.MALL_UI_TRACE === '1';
const labels = { buyer: '买家端演示', merchant: '商家端演示', system: '系统治理演示' };
const workspaceTargets = {
  buyer: ['/buyer/catalog', '/buyer/cart', '/buyer/orders', '/buyer/after-sales', '/buyer/favorites', '/buyer/account'],
  merchant: ['/merchant/products', '/merchant/orders', '/merchant/after-sales', '/merchant/reviews', '/merchant/settlement', '/merchant/account'],
  system: ['/system/accounts', '/system/roles', '/system/reviews'],
};
const viewports = [
  { width: 1440, height: 1000 },
  { width: 1024, height: 768 },
  { width: 390, height: 844 },
  { width: 320, height: 844 },
];
const errors = [];
const unexpected = [];
const resizeWarnings = [];
const results = [];
const tableCueChecks = { empty: 0, populated: 0 };
const browser = await chromium.launch({ headless: true, ...(process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE ? { executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE } : {}) });

async function trackResizeWarnings(context) {
  await context.addInitScript((trace) => {
    window.__mallResizeWarnings = [];
    window.addEventListener('error', (event) => {
      if (/ResizeObserver/i.test(event.message)) {
        window.__mallResizeWarnings.push({ path: location.pathname, message: event.message });
        if (trace) console.warn(`QA_RESIZE_WARNING ${location.pathname}`);
      }
    }, true);
  }, traced);
}

async function settle(page) {
  await page.locator('.ant-spin-spinning').waitFor({ state: 'detached', timeout: 5000 }).catch(() => {});
  await page.waitForTimeout(400);
  assert.equal(await page.locator('vite-error-overlay').count(), 0);
}

async function checkBounds(page, label) {
  const warnings = await page.evaluate(() => window.__mallResizeWarnings?.splice(0) ?? []);
  resizeWarnings.push(...warnings.map((warning) => ({ ...warning, check: label })));
  if (traced && warnings.length) console.log(`ResizeObserver warning at ${label}: ${warnings.length}`);
  const geometry = await page.evaluate(() => {
    const width = document.documentElement.clientWidth;
    const selectors = '.market-page, .market-head, .market-panel, .ant-modal:has(.ant-modal-content), .ant-drawer-content-wrapper';
    return {
      width,
      overflow: document.documentElement.scrollWidth - width,
      outside: [...document.querySelectorAll(selectors)].filter((el) => {
        const box = el.getBoundingClientRect();
        return box.width && box.height && (box.right > width + 2 || box.left < -2);
      }).map((el) => el.className),
    };
  });
  assert.ok(geometry.overflow <= 2, `${label}: page overflow ${geometry.overflow}`);
  assert.deepEqual(geometry.outside, [], `${label}: containers outside viewport`);

  if (geometry.width <= 680) {
    const tables = await page.evaluate(() => {
      const selector = '.market-page .ant-table-wrapper, .ant-drawer-content .ant-table-wrapper';
      return [...document.querySelectorAll(selector)].map((table) => ({
        hasRows: Boolean(table.querySelector('.ant-table-tbody > tr.ant-table-row')),
        isCompact: table.classList.contains('compact-table'),
        hasCue: getComputedStyle(table, '::before').content.includes('左右滑动查看更多列'),
        hasVisibleHeader: [...table.querySelectorAll('.ant-table-thead')].some((head) => getComputedStyle(head).display !== 'none'),
        emptyImageHeight: table.querySelector('.ant-table-placeholder .ant-empty-image')?.getBoundingClientRect().height ?? 0,
        emptyRowHeight: table.querySelector('.ant-table-placeholder')?.getBoundingClientRect().height ?? 0,
      }));
    });
    for (const { hasRows, isCompact, hasCue, hasVisibleHeader, emptyImageHeight, emptyRowHeight } of tables) {
      tableCueChecks[hasRows ? 'populated' : 'empty'] += 1;
      assert.equal(hasCue, hasRows && !isCompact, `${label}: table scroll cue must only appear for horizontally scrollable data tables`);
      assert.equal(hasVisibleHeader, hasRows, `${label}: table headers must only appear when rows are available`);
      if (!hasRows && emptyImageHeight > 0) {
        assert.ok(emptyImageHeight <= 64, `${label}: table empty illustration should remain compact`);
        assert.ok(emptyRowHeight <= 220, `${label}: empty table row should not dominate the mobile viewport`);
      }
    }
  }
}

async function setTheme(page, dark) {
  const current = await page.locator('html').evaluate((el) => el.classList.contains('dark'));
  if (current === dark) return;
  await page.locator('button.theme-toggle').click();
  await page.waitForFunction((expected) => document.documentElement.classList.contains('dark') === expected, dark);
  await settle(page);
}

async function assertThemeMode(page, dark, label) {
  const channels = await page.evaluate(() => getComputedStyle(document.body).backgroundColor.match(/[\d.]+/g)?.slice(0, 3).map(Number) ?? []);
  assert.equal(channels.length, 3, `${label}: body background must resolve to an RGB color`);
  const brightness = channels.reduce((sum, channel) => sum + channel, 0);
  assert.ok(dark ? brightness < 240 : brightness > 500, `${label}: expected the ${dark ? 'dark' : 'light'} theme to be active`);
}

async function verifyDarkEmptyIllustration(page, label) {
  const fills = await page.locator('.ant-empty-image svg [fill]').evaluateAll((elements) =>
    elements
      .map((element) => getComputedStyle(element).fill)
      .filter((fill) => fill !== 'none' && fill !== 'transparent')
      .map((fill) => fill.match(/[\d.]+/g)?.slice(0, 3).map(Number) ?? [])
      .filter((rgb) => rgb.length === 3),
  );
  assert.ok(fills.length > 0, `${label}: expected the simple empty illustration`);
  assert.ok(fills.some((rgb) => rgb.reduce((sum, channel) => sum + channel, 0) > 180), `${label}: dark empty illustration must remain visible`);
}

async function verifyOverlays(page, viewport) {
  await page.goto(`${origin}/buyer/catalog`);
  await page.getByRole('button', { name: '详情与评价', exact: true }).click();
  await page.locator('.ant-drawer-content').waitFor();
  await settle(page);
  await checkBounds(page, `drawer ${viewport.width}`);
  await page.screenshot({ path: join(output, `drawer-${viewport.width}.png`) });
  await page.locator('.ant-drawer-close').click();
  await page.goto(`${origin}/buyer/addresses`);
  await page.locator('.market-heading').waitFor();
  await settle(page);
  await page.getByRole('button', { name: /新增地址/ }).click();
  await page.locator('.ant-drawer-content').waitFor();
  await settle(page);
  await checkBounds(page, `address drawer ${viewport.width}`);
  await page.getByRole('button', { name: '保存地址', exact: true }).click();
  await page.getByText('请填写收货人', { exact: true }).waitFor();
  await checkBounds(page, `address validation ${viewport.width}`);
  await page.screenshot({ path: join(output, `address-${viewport.width}.png`) });
  await page.locator('.ant-drawer-close').click();
  await page.getByRole('button', { name: '用户', exact: true }).click();
  await page.getByText('关于平台', { exact: true }).click();
  await page.locator('.ant-modal-content').waitFor();
  await settle(page);
  await checkBounds(page, `about ${viewport.width}`);
  await page.screenshot({ path: join(output, `about-${viewport.width}.png`) });
  await page.locator('.ant-modal-close').click();
}

async function verifyNavigation(page, viewport) {
  if (viewport.width < 768) {
    await page.getByRole('button', { name: /浏览商品/ }).click();
    await page.waitForURL('**/buyer/catalog');
  } else {
    const toggle = page.locator('[data-layout-action="toggle-sidebar"]');
    const before = await page.locator('.market-page').boundingBox();
    await toggle.click();
    await settle(page);
    const after = await page.locator('.market-page').boundingBox();
    assert.ok(after.x < before.x, 'Sidebar collapse must reclaim content width');
    await toggle.click();
  }
  await settle(page);
  await checkBounds(page, `navigation ${viewport.width}`);
}

async function assertActionFitsViewport(page, name, label) {
  const action = page.getByRole('button', { name, exact: true }).first();
  await action.waitFor({ state: 'visible' });
  const box = await action.boundingBox();
  const width = await page.evaluate(() => document.documentElement.clientWidth);
  assert.ok(box && box.x >= 0 && box.x + box.width <= width + 1, `${label}: primary action must be visible without horizontal table scrolling`);
}

async function assertFixedColumnsDoNotOverlap(page, label) {
  const overlaps = await page.evaluate(() => [...document.querySelectorAll('.market-page .ant-table-wrapper')].flatMap((wrapper) => {
    const fixed = [...wrapper.querySelectorAll('.ant-table-thead .ant-table-cell-fix-right')];
    const cells = [...wrapper.querySelectorAll('.ant-table-thead tr > th:not(.ant-table-cell-fix-right)')];
    return fixed.flatMap((pinned) => {
      const a = pinned.getBoundingClientRect();
      return cells.filter((cell) => {
        const b = cell.getBoundingClientRect();
        return b.right > a.left + 2 && b.left < a.right - 2 && b.bottom > a.top + 2 && b.top < a.bottom - 2;
      }).map((cell) => ({ pinned: pinned.textContent?.trim(), covered: cell.textContent?.trim() }));
    });
  }));
  assert.deepEqual(overlaps, [], `${label}: fixed action column overlaps another visible header`);
}

async function assertCompactWorkflowTable(page, expectedHeadings, label) {
  const state = await page.locator('.market-page .ant-table-wrapper').first().evaluate((wrapper) => ({
    compact: wrapper.classList.contains('compact-table'),
    headings: [...wrapper.querySelectorAll('.ant-table-thead th')].map((cell) => cell.textContent.trim()).filter(Boolean),
  }));
  assert.equal(state.compact, true, `${label}: mobile workflow table should use the compact layout`);
  assert.deepEqual(state.headings, expectedHeadings, `${label}: compact table should show only the summary and action columns`);
}

async function verifyMobileWorkflowActions(page, actor) {
  if (actor === 'buyer') {
    await page.goto(`${origin}/buyer/cart`);
    await settle(page);
    await assertActionFitsViewport(page, '移除', 'cart');
    await assertCompactWorkflowTable(page, ['商品信息', '操作'], 'cart');

    await page.goto(`${origin}/buyer/orders`);
    await settle(page);
    await assertActionFitsViewport(page, '详情', 'orders');
    await assertFixedColumnsDoNotOverlap(page, 'orders');
    await page.getByRole('button', { name: '详情', exact: true }).click();
    await page.locator('#sub-orders').scrollIntoViewIfNeeded();
    await page.locator('#sub-orders').waitFor();
    await assertActionFitsViewport(page, '物流轨迹', 'order detail');
  } else if (actor === 'merchant') {
    await page.goto(`${origin}/merchant/orders`);
    await settle(page);
    await assertActionFitsViewport(page, '录入物流', 'fulfillment');
    await assertFixedColumnsDoNotOverlap(page, 'fulfillment');
  } else {
    await page.goto(`${origin}/system/reviews`);
    await settle(page);
    await assertActionFitsViewport(page, '处置', 'review moderation');
    await assertCompactWorkflowTable(page, ['举报信息', '操作'], 'review moderation');
  }
}

async function verifyCouponTemplateHandoff(page) {
  await page.goto(`${origin}/merchant/coupons`);
  await settle(page);
  assert.equal(await page.locator('.coupon-id').innerText(), '模板 ID：128', 'Merchant coupon template ID must be visible');
  await page.getByRole('button', { name: '复制模板 ID 128' }).click();
  await page.getByText('模板 ID 128 已复制，可发给买家领取', { exact: true }).waitFor();
  assert.equal(await page.evaluate(() => navigator.clipboard.readText()), '128', 'Copy action must place the template ID on the clipboard');
}

async function verifyWorkspaceActions(page, actor) {
  await page.goto(`${origin}/${actor}/workspace`);
  await settle(page);
  const actions = page.locator('.workspace-tasks .action');
  await actions.first().waitFor({ state: 'visible' });
  assert.equal(await actions.count(), workspaceTargets[actor].length, `${actor}: workspace should present one entry per supported task`);
  assert.equal(await page.locator('.workspace-flow').count(), 0, `${actor}: duplicated workflow panel should not return`);
  assert.equal(await page.getByText('当前业务空间', { exact: true }).count(), 0, `${actor}: workspace role context should not repeat the overline`);
  const titles = await actions.locator('strong').allTextContents();
  assert.equal(new Set(titles).size, titles.length, `${actor}: workspace action titles should be unique`);
  for (const [index, target] of workspaceTargets[actor].entries()) {
    await actions.nth(index).click();
    await page.waitForURL(`**${target}`);
    await page.goto(`${origin}/${actor}/workspace`);
    await settle(page);
  }
}

async function verifySystemAdministration(page) {
  await page.goto(`${origin}/system/accounts`);
  await settle(page);
  await page.getByLabel('登录账号 ID').fill('1001');
  await page.getByRole('menuitem', { name: '角色权限', exact: true }).click();
  await page.waitForURL('**/system/roles');
  await settle(page);
  assert.equal(await page.getByLabel('角色 ID').inputValue(), '', 'Switching to role permissions must clear the hidden account ID');
  await page.getByRole('menuitem', { name: '账号与商家', exact: true }).click();
  await page.waitForURL('**/system/accounts');
  await settle(page);
  assert.equal(await page.getByLabel('登录账号 ID').inputValue(), '', 'Switching back must not restore a stale account ID');

  await page.route('**/api/user/api/v1/system/accounts/1001/merchant-role', async (route) => {
    const request = route.request();
    assert.equal(request.method(), 'PUT');
    assert.ok(request.headers()['idempotency-key'], 'Merchant role grant must carry the generated idempotency key');
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ status: 200, data: { role: 'ROLE_MERCHANT', refreshSessionsRevoked: true }, message: 'merchant_role_granted' }) });
  });
  await page.getByLabel('登录账号 ID').fill('1001');
  await page.getByRole('button', { name: '授予商家角色' }).click();
  await page.getByText('商家角色授权成功，该账号刷新会话已撤销，请重新登录', { exact: true }).waitFor();

  await page.route('**/api/user/api/v1/system/roles/2001/functions', async (route) => {
    const request = route.request();
    assert.equal(request.method(), 'PUT');
    assert.ok(request.headers()['idempotency-key'], 'Role function grant must carry the generated idempotency key');
    assert.deepEqual(request.postDataJSON(), { functionIds: [12, 18] }, 'Duplicate function IDs must be removed before sending');
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ status: 200, data: { roleId: 2001, functionIds: [12, 18] }, message: 'role_functions_granted' }) });
  });
  await page.getByRole('menuitem', { name: '角色权限', exact: true }).click();
  await page.waitForURL('**/system/roles');
  await settle(page);
  await page.getByLabel('角色 ID').fill('2001');
  await page.getByLabel('功能 ID 列表').fill('12, 18, 12');
  await page.getByRole('button', { name: '追加角色功能' }).click();
  await page.getByText('角色功能追加成功', { exact: true }).waitFor();
}

async function verifyCatalogFiltersAndPagination(page) {
  const requests = [];
  await page.route('**/api/product/api/v1/products**', async (route) => {
    const url = new URL(route.request().url());
    requests.push(url);
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ status: 200, data: { items: [product], total: 41 }, message: 'layout fixture' }),
    });
  });
  await page.goto(`${origin}/buyer/catalog`);
  await settle(page);
  await page.getByLabel('关键词').fill('旅行');
  const searchResponse = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.pathname.endsWith('/product/api/v1/products') && url.searchParams.get('keyword') === '旅行';
  });
  await page.locator('.catalog-filters button[type="submit"]').click();
  await searchResponse;
  assert.equal(requests.at(-1)?.searchParams.get('keyword'), '旅行', 'Catalog search must submit the selected keyword');
  assert.equal(requests.at(-1)?.searchParams.get('page'), '0', 'Catalog search must return to the first page');

  const nextPageResponse = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.pathname.endsWith('/product/api/v1/products') && url.searchParams.get('page') === '1';
  });
  await page.locator('.catalog-pagination .ant-pagination-item-2').click();
  await nextPageResponse;
  await page.waitForFunction(() => document.querySelector('.catalog-pagination .ant-pagination-item-active')?.textContent?.trim() === '2');
  assert.equal(requests.at(-1)?.searchParams.get('page'), '1', 'Catalog pagination must load the selected page');

  const resetResponse = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.pathname.endsWith('/product/api/v1/products') && url.searchParams.get('keyword') !== '旅行';
  });
  await page.locator('.catalog-filters button').nth(1).click();
  await resetResponse;
  assert.equal(requests.at(-1)?.searchParams.get('page'), '0', 'Resetting catalog filters must return to page one');
  assert.notEqual(requests.at(-1)?.searchParams.get('keyword'), '旅行', 'Resetting catalog filters must clear the keyword');
}

async function verifyMerchantAfterSalesFilterAndPagination(page) {
  const requests = [];
  await page.route('**/api/order/api/v1/after-sales/merchant/page**', async (route) => {
    const url = new URL(route.request().url());
    requests.push(url);
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ status: 200, data: { items: [], total: 41 }, message: 'layout fixture' }),
    });
  });
  await page.goto(`${origin}/merchant/after-sales`);
  await settle(page);
  await page.locator('.status-filter .ant-select-selector').click();
  await page.getByText('已同意，待退货', { exact: true }).click();
  await page.waitForFunction(() => document.querySelector('.ant-pagination'));
  assert.equal(requests.at(-1)?.searchParams.get('status'), 'APPROVED', 'After-sales status selection must reach the list query');
  assert.equal(requests.at(-1)?.searchParams.get('page'), '0', 'Changing status must reset pagination');

  await page.locator('.ant-pagination-item-2').click();
  await page.waitForFunction(() => document.querySelector('.ant-pagination-item-active')?.textContent?.trim() === '2');
  assert.equal(requests.at(-1)?.searchParams.get('page'), '1', 'After-sales pagination must load the selected page');
}

try {
  for (const viewport of (authOnly ? [] : viewports.filter((item) => !onlyWidth || item.width === onlyWidth))) {
    for (const { actor, routes } of groups.filter((item) => !onlyActor || item.actor === onlyActor)) {
      const context = await browser.newContext({ viewport, reducedMotion: 'reduce', permissions: ['clipboard-read', 'clipboard-write'] });
      await installFixtures(context, origin, unexpected);
      await trackResizeWarnings(context);
      const page = await context.newPage();
      page.on('pageerror', (error) => errors.push(error.message));
      if (traced) page.on('console', (message) => { if (message.text().startsWith('QA_RESIZE_WARNING')) console.log(message.text()); });
      await page.goto(`${origin}/auth/login`);
      const actorEntry = page.getByRole('button', { name: labels[actor], exact: true });
      await actorEntry.waitFor({ state: 'visible' });
      await actorEntry.click();
      await page.waitForURL(`**/${actor}/workspace`);
      for (const dark of [false, true]) {
        await setTheme(page, dark);
        for (const path of routes) {
          if (traced) console.log(`VISIT ${actor} ${viewport.width} ${dark ? 'dark' : 'light'} ${path}`);
          await page.goto(`${origin}${path}${path.endsWith('/checkout') ? '?items=1' : ''}`);
          await page.locator('.market-heading').waitFor();
          await settle(page);
          await setTheme(page, dark);
          await assertThemeMode(page, dark, `${path} ${viewport.width}`);
          if (path === '/buyer/checkout') {
            const quoteLines = page.locator('.quote-lines .quote-line strong');
            await quoteLines.nth(3).waitFor({ state: 'visible' });
            assert.deepEqual(await quoteLines.allTextContents(), ['¥338.00', '−¥0.00', '¥0.00', '¥338.00'], 'Checkout quote must render complete service amounts');
          }
          if (path === '/buyer/catalog') {
            const productCategory = page.locator('.product-card .product-art-tag').first();
            await productCategory.waitFor({ state: 'visible' });
            await page.waitForFunction(() => document.querySelector('.product-card .product-art-tag')?.textContent?.trim() === '测试分类');
          }
          if (path === '/buyer/coupons') {
            await page.getByText(/联系商家获取模板 ID 后/).waitFor();
            await page.getByText(/商家可在优惠券模板中复制 ID 发给买家/).waitFor();
          }
          assert.equal(new URL(page.url()).pathname, path);
          await checkBounds(page, `${path} ${viewport.width} ${dark ? 'dark' : 'light'}`);
          if (path === '/buyer/after-sales' && dark) {
            await verifyDarkEmptyIllustration(page, `${path} ${viewport.width}`);
          }
          assert.equal(await page.locator('.market-page .ant-alert-error').count(), 0, `${path}: unexpected API error`);
          results.push({ path, width: viewport.width, theme: dark ? 'dark' : 'light' });
          if ([1440, 390].includes(viewport.width)) {
            await page.screenshot({ path: join(output, `${actor}-${path.split('/').at(-1)}-${viewport.width}-${dark ? 'dark' : 'light'}.png`) });
          }
        }
      }
      if (actor === 'buyer') {
        await page.goto(`${origin}/buyer/workspace`);
        await settle(page);
        await verifyNavigation(page, viewport);
        await verifyOverlays(page, viewport);
      }
      if (viewport.width <= 680) {
        await verifyMobileWorkflowActions(page, actor);
      }
      if (viewport.width === 390) {
        await verifyWorkspaceActions(page, actor);
      }
      if (actor === 'system' && viewport.width === 1440) {
        await verifySystemAdministration(page);
      }
      if (actor === 'buyer' && viewport.width === 1440) {
        await verifyCatalogFiltersAndPagination(page);
      }
      if (actor === 'merchant' && viewport.width === 1440) {
        await verifyMerchantAfterSalesFilterAndPagination(page);
      }
      if (actor === 'merchant' && viewport.width === 390) {
        await verifyCouponTemplateHandoff(page);
      }
      await page.goto(`${origin}/unknown-layout-route`);
      await page.locator('.fallback-title').waitFor();
      assert.match(await page.locator('.fallback-title').innerText(), /404/, `404 ${actor} ${viewport.width}: fallback title should identify the missing route`);
      await settle(page);
      await checkBounds(page, `404 ${actor} ${viewport.width}`);
      await page.getByRole('button', { name: '返回工作台', exact: true }).click();
      await page.waitForURL(`**/${actor}/workspace`);
      await context.close();
      console.log(`PASS ${actor} ${viewport.width}px: ${routes.length} routes × light/dark`);
    }
  }
  const authPaths = ['login', 'code-login', 'qrcode-login', 'forget-password', 'register'];
  for (const viewport of [viewports[0], viewports[2], viewports[3], { width: 844, height: 390 }]) {
    const context = await browser.newContext({ viewport, reducedMotion: 'reduce' });
    await installFixtures(context, origin, unexpected);
    await trackResizeWarnings(context);
    const page = await context.newPage();
    page.on('pageerror', (error) => errors.push(error.message));
    if (traced) page.on('console', (message) => { if (message.text().startsWith('QA_RESIZE_WARNING')) console.log(message.text()); });
    for (const path of authPaths) {
      if (traced) console.log(`AUTH ${viewport.width} ${path}`);
      await page.goto(`${origin}/auth/${path}`);
      await page.locator('button').first().waitFor();
      await settle(page);
      await checkBounds(page, `auth ${path} ${viewport.width}`);
      if (path !== 'login') {
        await page.getByRole('button', { name: '返回账号密码登录', exact: true }).click();
        await page.waitForURL('**/auth/login');
      }
      await page.screenshot({ path: join(output, `login-${viewport.width}.png`) });
    }
    await context.close();
  }
  assert.deepEqual(errors, [], 'Uncaught browser errors');
  assert.deepEqual(unexpected, [], 'Requests without fixture');
  if (!authOnly && !onlyActor && !onlyWidth) {
    assert.ok(tableCueChecks.empty > 0, 'Expected mobile empty-table cue coverage');
    assert.ok(tableCueChecks.populated > 0, 'Expected mobile populated-table cue coverage');
  }
  await writeFile(join(output, 'results.json'), JSON.stringify({ results, authChecks: 20, errors, unexpected, resizeWarnings }, null, 2));
  console.log(`PASS ${results.length} route/viewport/theme combinations; 20 auth checks; overlays; 404 return. Screenshots: ${output}`);
} finally {
  await writeFile(join(output, 'results.partial.json'), JSON.stringify({ results, errors, unexpected, resizeWarnings }, null, 2));
  await browser.close();
}
