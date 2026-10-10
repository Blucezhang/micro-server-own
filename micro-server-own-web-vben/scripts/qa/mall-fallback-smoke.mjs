import assert from 'node:assert/strict';
import { mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

import { chromium } from 'playwright';

import { installFixtures } from './mall-layout-fixtures.mjs';

const origin = process.env.MALL_UI_URL || 'http://127.0.0.1:5180';
assert.ok(['127.0.0.1', 'localhost'].includes(new URL(origin).hostname));
const output = process.env.MALL_FALLBACK_OUTPUT || join(tmpdir(), 'mall-fallback-verification');
const chrome = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE;
const browser = await chromium.launch({ headless: true, ...(chrome ? { executablePath: chrome } : {}) });
const statuses = {
  403: { module: 'forbidden', title: '403 · 无权访问该商城资源' },
  500: { module: 'internal-error', title: '500 · 微商城服务异常' },
  offline: { module: 'offline', title: '网络连接中断 · 离线状态' },
  'coming-soon': { module: 'coming-soon', title: '功能建设中 · 敬请期待' },
};
const viewports = [
  { width: 1440, height: 1000 },
  { width: 1024, height: 768 },
  { width: 390, height: 844 },
  { width: 320, height: 844 },
];
const results = [];
const errors = [];
const unexpected = [];
await mkdir(output, { recursive: true });

try {
  for (const viewport of viewports) {
    const context = await browser.newContext({ viewport, reducedMotion: 'reduce' });
    await installFixtures(context, origin, unexpected);
    const page = await context.newPage();
    page.on('pageerror', (error) => errors.push(`${viewport.width}: ${error.message}`));
    await page.goto(`${origin}/auth/login`);
    await page.getByRole('button', { name: '买家端演示', exact: true }).click();
    await page.waitForURL('**/buyer/workspace');

    for (const dark of [false, true]) {
      await page.goto(`${origin}/buyer/workspace`);
      await page.locator('button.theme-toggle').waitFor();
      const isDark = await page.locator('html').evaluate((element) => element.classList.contains('dark'));
      if (isDark !== dark) await page.locator('button.theme-toggle').click();
      await page.waitForFunction((expected) => document.documentElement.classList.contains('dark') === expected, dark);

      for (const [status, spec] of Object.entries(statuses)) {
        const path = `/__qa/fallback/${status}`;
        await page.evaluate(async ({ path, module }) => {
          const app = document.querySelector('#app').__vue_app__;
          const router = app.config.globalProperties.$router;
          const component = (await import(`/src/views/_core/fallback/${module}.vue`)).default;
          router.addRoute({ path, name: `QaFallback${module}`, component, meta: { ignoreAccess: true, hideInMenu: true } });
          await router.push(path);
        }, { path, module: spec.module });
        await page.waitForURL(`**${path}`);
        await page.getByRole('heading', { name: spec.title }).waitFor();
        await page.locator('#nprogress').waitFor({ state: 'detached', timeout: 2000 }).catch(() => {});

        const geometry = await page.evaluate(() => {
          const viewportWidth = document.documentElement.clientWidth;
          const panel = document.querySelector('.fallback-container').getBoundingClientRect();
          const background = getComputedStyle(document.body).backgroundColor.match(/[\d.]+/g)?.slice(0, 3).map(Number) ?? [];
          return { overflow: document.documentElement.scrollWidth - viewportWidth,
            outside: panel.left < -2 || panel.right > viewportWidth + 2,
            brightness: background.reduce((sum, value) => sum + value, 0) };
        });
        assert.ok(geometry.overflow <= 2, `${status} ${viewport.width}: horizontal overflow`);
        assert.equal(geometry.outside, false, `${status} ${viewport.width}: fallback outside viewport`);
        assert.ok(dark ? geometry.brightness < 240 : geometry.brightness > 500, `${status} ${viewport.width}: wrong theme`);
        assert.equal(await page.getByRole('button', { name: '返回工作台' }).count(), 1);
        assert.equal(await page.getByRole('button', { name: '退出并重新登录' }).count(), 1);
        if (['500', 'offline'].includes(status)) {
          assert.equal(await page.getByRole('button', { name: '刷新重试' }).count(), 1);
        }
        await page.screenshot({ path: join(output, `${status}-${viewport.width}-${dark ? 'dark' : 'light'}.png`) });
        results.push({ status, width: viewport.width, theme: dark ? 'dark' : 'light', result: 'passed' });
        await page.getByRole('button', { name: '返回工作台' }).click();
        await page.waitForURL('**/buyer/workspace');
      }
    }
    await context.close();
  }
  assert.deepEqual(errors, [], 'Browser page errors');
  assert.deepEqual(unexpected, [], 'Unexpected business requests');
  await writeFile(join(output, 'results.json'), JSON.stringify({ results, errors, unexpected }, null, 2));
  console.log(`PASS ${results.length} fallback/viewport/theme checks; screenshots: ${output}`);
} finally {
  await browser.close();
}
