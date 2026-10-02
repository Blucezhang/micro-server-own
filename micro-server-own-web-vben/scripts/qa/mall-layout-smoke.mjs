import assert from 'node:assert/strict';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import process from 'node:process';

import { chromium } from 'playwright';

import { installFixtures } from './mall-layout-fixtures.mjs';

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
const labels = { buyer: '买家端演示', merchant: '商家端演示', system: '系统治理演示' };
const viewports = [{ width: 1440, height: 1000 }, { width: 1024, height: 768 }, { width: 390, height: 844 }];
const errors = [];
const unexpected = [];
const results = [];
const browser = await chromium.launch({ headless: true, ...(process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE ? { executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE } : {}) });

async function settle(page) {
  await page.locator('.ant-spin-spinning').waitFor({ state: 'detached', timeout: 5000 }).catch(() => {});
  await page.waitForTimeout(400);
  assert.equal(await page.locator('vite-error-overlay').count(), 0);
}

async function checkBounds(page, label) {
  const geometry = await page.evaluate(() => {
    const width = document.documentElement.clientWidth;
    const selectors = '.market-page, .market-head, .market-panel, .ant-modal:has(.ant-modal-content), .ant-drawer-content-wrapper';
    return {
      overflow: document.documentElement.scrollWidth - width,
      outside: [...document.querySelectorAll(selectors)].filter((el) => {
        const box = el.getBoundingClientRect();
        return box.width && box.height && (box.right > width + 2 || box.left < -2);
      }).map((el) => el.className),
    };
  });
  assert.ok(geometry.overflow <= 2, `${label}: page overflow ${geometry.overflow}`);
  assert.deepEqual(geometry.outside, [], `${label}: containers outside viewport`);
}

async function setTheme(page, dark) {
  const current = await page.locator('html').evaluate((el) => el.classList.contains('dark'));
  if (current === dark) return;
  await page.locator('button.theme-toggle').click();
  await page.waitForFunction((expected) => document.documentElement.classList.contains('dark') === expected, dark);
  await settle(page);
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
  await page.getByRole('button', { name: '新增地址', exact: true }).click();
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
    const toggle = page.getByRole('button', { name: '切换侧边栏', exact: true });
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

try {
  for (const viewport of viewports) {
    for (const { actor, routes } of groups) {
      const context = await browser.newContext({ viewport, reducedMotion: 'reduce' });
      await installFixtures(context, origin, unexpected);
      const page = await context.newPage();
      page.on('pageerror', (error) => errors.push(error.message));
      await page.goto(`${origin}/auth/login`);
      await page.getByRole('button', { name: labels[actor], exact: true }).click();
      await page.waitForURL(`**/${actor}/workspace`);
      for (const dark of [false, true]) {
        await setTheme(page, dark);
        for (const path of routes) {
          await page.goto(`${origin}${path}${path.endsWith('/checkout') ? '?items=1' : ''}`);
          await page.locator('.market-heading').waitFor();
          await settle(page);
          assert.equal(new URL(page.url()).pathname, path);
          await checkBounds(page, `${path} ${viewport.width} ${dark ? 'dark' : 'light'}`);
          assert.equal(await page.locator('.market-page .ant-alert-error').count(), 0, `${path}: unexpected API error`);
          results.push({ path, width: viewport.width, theme: dark ? 'dark' : 'light' });
          if (path.endsWith('/workspace') || path === '/merchant/products') {
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
      await page.goto(`${origin}/unknown-layout-route`);
      await page.getByText('404', { exact: false }).first().waitFor();
      await settle(page);
      await checkBounds(page, `404 ${actor} ${viewport.width}`);
      await page.getByRole('button', { name: '返回工作台', exact: true }).click();
      await page.waitForURL(`**/${actor}/workspace`);
      await context.close();
      console.log(`PASS ${actor} ${viewport.width}px: ${routes.length} routes × light/dark`);
    }
  }
  const authPaths = ['login', 'code-login', 'qrcode-login', 'forget-password', 'register'];
  for (const viewport of [viewports[0], viewports[2], { width: 844, height: 390 }]) {
    const context = await browser.newContext({ viewport, reducedMotion: 'reduce' });
    await installFixtures(context, origin, unexpected);
    const page = await context.newPage();
    page.on('pageerror', (error) => errors.push(error.message));
    for (const path of authPaths) {
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
  await writeFile(join(output, 'results.json'), JSON.stringify({ results, authChecks: 15, errors, unexpected }, null, 2));
  console.log(`PASS ${results.length} route/viewport/theme combinations; 15 auth checks; overlays; 404 return. Screenshots: ${output}`);
} finally {
  await browser.close();
}
