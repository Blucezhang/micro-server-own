import type { App } from 'vue';

import { createApp, h, nextTick, ref } from 'vue';

import { afterEach, describe, expect, it } from 'vitest';

import DemoOutput from '../../src/components/demo-output.vue';

let app: App | undefined;

async function mountOutput(
  text: string,
  attributes: Record<string, unknown> = {},
) {
  const value = ref(text);
  const host = document.createElement('div');
  document.body.append(host);
  app = createApp({
    setup: () => () => h(DemoOutput, { ...attributes, text: value.value }),
  });
  app.mount(host);
  await nextTick();
  const output = host.querySelector('pre');
  if (!output) throw new Error('DemoOutput must render a pre element');
  return { host, output, value };
}

afterEach(() => {
  app?.unmount();
  app = undefined;
  document.body.replaceChildren();
});

describe('DemoOutput', () => {
  it('renders a single pre root and preserves whitespace verbatim', async () => {
    const text = '\n  中文输出\n    "id": "9007199254740993"\n';
    const { host, output } = await mountOutput(text);
    expect(host.children).toHaveLength(1);
    expect(output.textContent).toBe(text);
  });

  it('renders HTML as inert text, not executable markup', async () => {
    const text = '<img src=x onerror="alert(1)"><script>alert(2)</script>';
    const { output } = await mountOutput(text);
    expect(output.textContent).toBe(text);
    expect(output.querySelector('img, script')).toBeNull();
  });

  it('preserves the empty state without inventing placeholder data', async () => {
    const { output } = await mountOutput('');
    expect(output.textContent).toBe('');
  });

  it('updates text reactively without reformatting it', async () => {
    const { output, value } = await mountOutput('{}');
    value.value = '{\n  "name": "修改后"\n}';
    await nextTick();
    expect(output.textContent).toBe(value.value);
  });

  it('keeps the shared semantic surface and scrolling utilities', async () => {
    const { output } = await mountOutput('示例');
    expect(output.classList.contains('bg-muted')).toBe(true);
    expect(output.classList.contains('overflow-auto')).toBe(true);
  });

  it('merges compact spacing and radius overrides deterministically', async () => {
    const { output } = await mountOutput('示例', {
      class: 'max-h-105 rounded-lg p-3 text-xs text-muted-foreground',
    });
    expect(output.classList.contains('p-3')).toBe(true);
    expect(output.classList.contains('p-4')).toBe(false);
    expect(output.classList.contains('rounded-lg')).toBe(true);
    expect(output.classList.contains('rounded-md')).toBe(false);
    expect(output.classList.contains('max-h-105')).toBe(true);
    expect(output.classList.contains('text-xs')).toBe(true);
    expect(output.classList.contains('text-muted-foreground')).toBe(true);
  });

  it('preserves caller-provided attributes on the output root', async () => {
    const { output } = await mountOutput('示例', {
      'aria-label': '提交结果',
      id: 'submission-output',
    });
    expect(output.id).toBe('submission-output');
    expect(output.getAttribute('aria-label')).toBe('提交结果');
  });
});
