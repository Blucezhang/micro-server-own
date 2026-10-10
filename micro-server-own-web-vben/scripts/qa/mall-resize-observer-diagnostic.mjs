import { mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

import { chromium } from 'playwright';

import { installFixtures } from './mall-layout-fixtures.mjs';

const origin = process.env.MALL_UI_URL || 'http://127.0.0.1:5177';
const output = process.env.MALL_UI_OUTPUT || join(tmpdir(), 'mall-resize-observer-diagnostic');
const widths = [1440, 1024, 390, 320];
await mkdir(output, { recursive: true });
const browser = await chromium.launch({
  headless: true,
  ...(process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE ? { executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE } : {}),
});
const results = [];

try {
  for (const width of widths) {
    const context = await browser.newContext({ viewport: { width, height: 900 } });
    await installFixtures(context, origin, []);
    await context.addInitScript(() => {
      const NativeResizeObserver = window.ResizeObserver;
      const observers = [];
      const warnings = [];
      let currentPhase = 'initial';
      let nextObserverId = 1;

      window.__mallSetDiagnosticPhase = (phaseName) => {
        currentPhase = phaseName;
      };

      const getSelectorPath = (el) => {
        if (!el || !(el instanceof Element)) return '';
        const parts = [];
        let curr = el;
        while (curr && curr.nodeType === Node.ELEMENT_NODE && parts.length < 5) {
          let descriptor = curr.tagName.toLowerCase();
          if (curr.id) {
            descriptor += `#${curr.id}`;
            parts.unshift(descriptor);
            break;
          }
          if (typeof curr.className === 'string' && curr.className.trim()) {
            const firstClass = curr.className.trim().split(/\s+/)[0];
            if (firstClass) descriptor += `.${firstClass}`;
          }
          parts.unshift(descriptor);
          curr = curr.parentElement;
        }
        return parts.join(' > ');
      };

      const describeTarget = (target) => {
        if (!target || !(target instanceof Element)) {
          return { tag: 'unknown', id: '', className: '', selector: '', rect: null, text: '' };
        }
        const rect = target.getBoundingClientRect ? target.getBoundingClientRect() : null;
        return {
          tag: target.tagName,
          id: target.id || '',
          className: typeof target.className === 'string' ? target.className : '',
          selector: getSelectorPath(target),
          rect: rect ? { width: Math.round(rect.width), height: Math.round(rect.height), top: Math.round(rect.top), left: Math.round(rect.left) } : null,
          text: target.textContent?.trim().slice(0, 60) || '',
        };
      };

      const recordWarning = (message, source = 'window.error') => {
        if (!/ResizeObserver/i.test(message)) return;
        const now = performance.now();
        const temporallyCorrelatedObservers = observers
          .filter((obs) => obs.calls.some((call) => now - call.at < 200))
          .map(({ id, created, targets, calls }) => ({
            id,
            created,
            targets,
            recentCalls: calls.filter((call) => now - call.at < 200),
          }));

        warnings.push({
          phase: currentPhase,
          source,
          message,
          at: now,
          correlationNote: '时间上近 200ms 内触发的回调与元素，仅作为关联证据，不推断必然因果关系',
          associatedObserversCount: temporallyCorrelatedObservers.length,
          temporallyCorrelatedObservers,
        });
      };

      window.__mallResizeTrace = {
        observers,
        warnings,
        getCurrentPhase: () => currentPhase,
      };

      window.ResizeObserver = class extends NativeResizeObserver {
        constructor(callback) {
          const id = nextObserverId++;
          const record = {
            id,
            created: new Error().stack || '',
            targets: [],
            calls: [],
          };
          super((entries, observer) => {
            const call = {
              at: performance.now(),
              phase: currentPhase,
              entriesCount: entries.length,
              entries: entries.map((entry) => describeTarget(entry.target)),
            };
            record.calls.push(call);
            if (record.calls.length > 10) {
              record.calls = record.calls.slice(-10);
            }
            return callback(entries, observer);
          });
          record.instance = this;
          observers.push(record);
        }

        observe(target, options) {
          const record = observers.find((item) => item.instance === this);
          if (record) {
            record.targets.push(describeTarget(target));
          }
          return super.observe(target, options);
        }

        unobserve(target) {
          return super.unobserve(target);
        }

        disconnect() {
          return super.disconnect();
        }
      };

      window.addEventListener('error', (event) => {
        recordWarning(event.message || String(event.error), 'window.error');
      }, true);

      const origConsoleError = console.error;
      console.error = (...args) => {
        const msg = args.map((arg) => (typeof arg === 'string' ? arg : (arg?.message || String(arg)))).join(' ');
        if (/ResizeObserver/i.test(msg)) {
          recordWarning(msg, 'console.error');
        }
        return origConsoleError.apply(console, args);
      };

      const origConsoleWarn = console.warn;
      console.warn = (...args) => {
        const msg = args.map((arg) => (typeof arg === 'string' ? arg : (arg?.message || String(arg)))).join(' ');
        if (/ResizeObserver/i.test(msg)) {
          recordWarning(msg, 'console.warn');
        }
        return origConsoleWarn.apply(console, args);
      };
    });

    const page = await context.newPage();

    // 阶段 1：login_and_workspace
    await page.evaluate(() => window.__mallSetDiagnosticPhase('login_and_workspace'));
    await page.goto(`${origin}/auth/login`);
    await page.getByRole('button', { name: '买家端演示', exact: true }).click();
    await page.waitForURL('**/buyer/workspace');
    await page.waitForTimeout(300);

    // 阶段 2：catalog_initial
    await page.evaluate(() => window.__mallSetDiagnosticPhase('catalog_initial'));
    await page.goto(`${origin}/buyer/catalog`);
    await page.locator('.market-heading').waitFor();
    await page.waitForTimeout(500);

    // 阶段 3：open_drawer
    await page.evaluate(() => window.__mallSetDiagnosticPhase('open_drawer'));
    await page.getByRole('button', { name: '详情与评价', exact: true }).click();
    await page.locator('.ant-drawer-content').waitFor();
    await page.waitForTimeout(800);

    const traceData = await page.evaluate(() => {
      const trace = window.__mallResizeTrace;
      const allWarnings = trace.warnings || [];
      const phaseNames = ['login_and_workspace', 'catalog_initial', 'open_drawer'];

      const phases = phaseNames.map((phase) => {
        const phaseWarnings = allWarnings.filter((w) => w.phase === phase);
        return {
          phase,
          reproduced: phaseWarnings.length > 0,
          warningCount: phaseWarnings.length,
          statusMessage: phaseWarnings.length > 0
            ? `阶段 [${phase}] 捕获到 ${phaseWarnings.length} 条警告`
            : `阶段 [${phase}] 未复现`,
          warnings: phaseWarnings,
        };
      });

      const observerSummaries = (trace.observers || []).map(({ id, created, targets, calls }) => ({
        id,
        created,
        targets,
        callCount: calls.length,
        recentCalls: calls.slice(-3),
      }));

      return {
        totalWarnings: allWarnings.length,
        reproduced: allWarnings.length > 0,
        phases,
        allWarnings,
        observers: observerSummaries,
      };
    });

    results.push({
      width,
      reproduced: traceData.reproduced,
      totalWarnings: traceData.totalWarnings,
      phases: traceData.phases,
      allWarnings: traceData.allWarnings,
      observers: traceData.observers,
    });

    await context.close();
  }

  const anyReproduced = results.some((r) => r.reproduced);
  const totalWarnings = results.reduce((sum, r) => sum + r.totalWarnings, 0);

  const finalReport = {
    summary: {
      totalWidthsTested: widths.length,
      widths,
      reproduced: anyReproduced,
      totalWarnings,
      verdict: anyReproduced
        ? `在测试视口下复现并记录了 ${totalWarnings} 条 ResizeObserver 警告。`
        : '所有视口测试均未复现 ResizeObserver 警告。',
    },
    results,
  };

  await writeFile(join(output, 'diagnostic.json'), JSON.stringify(finalReport, null, 2));
  console.log(`ResizeObserver diagnostic saved: ${join(output, 'diagnostic.json')}`);
  console.log(finalReport.summary.verdict);
} finally {
  await browser.close();
}
