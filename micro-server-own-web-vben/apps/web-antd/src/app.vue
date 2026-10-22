<script lang="ts" setup>
import { computed } from 'vue';

import { useAntdDesignTokens } from '@vben/hooks';
import { preferences, usePreferences } from '@vben/preferences';

import { App, ConfigProvider, theme } from 'ant-design-vue';

import { antdLocale } from '#/locales';

defineOptions({ name: 'App' });

const { isDark } = usePreferences();
const { tokens } = useAntdDesignTokens();

const effectivePrimary = computed(() => {
  const base = preferences.theme.colorPrimary || tokens?.colorPrimary || '#0E665D';
  if (isDark.value && (base === '#0E665D' || base.includes('174') || base.includes('168'))) {
    return '#57C8B6';
  }
  return tokens?.colorPrimary || base;
});

const isTealDark = computed(() => isDark.value && effectivePrimary.value === '#57C8B6');

const tokenTheme = computed(() => {
  const algorithm = isDark.value
    ? [theme.darkAlgorithm]
    : [theme.defaultAlgorithm];

  // antd 紧凑模式算法
  if (preferences.app.compact) {
    algorithm.push(theme.compactAlgorithm);
  }

  const primary = effectivePrimary.value;

  return {
    algorithm,
    token: {
      ...tokens,
      colorPrimary: primary,
      colorPrimaryHover: isTealDark.value ? '#6EE0CE' : undefined,
      colorLink: primary,
      colorTextLightSolid: isTealDark.value ? '#101A1B' : '#FFFFFF',
      colorBgBase: isDark.value ? '#101A1B' : '#F6F7F6',
      colorBgContainer: isDark.value ? '#172223' : '#FFFFFF',
      colorBgElevated: isDark.value ? '#1E2C2D' : '#FFFFFF',
      colorTextBase: isDark.value ? '#E8F0ED' : '#1D2928',
      colorTextSecondary: isDark.value ? '#A7B7B1' : '#66736F',
      colorBorder: isDark.value ? '#30403E' : '#DDE4E1',
      colorBorderSecondary: isDark.value ? '#253331' : '#E8ECEB',
      fontFamily:
        '-apple-system, BlinkMacSystemFont, "PingFang SC", "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif',
      fontSize: 14,
      controlHeight: 38,
      borderRadius: 8,
      borderRadiusLG: 12,
      borderRadiusSM: 6,
      wireframe: false,
    },
    components: {
      Button: {
        controlHeight: 38,
        primaryColor: isTealDark.value ? '#101A1B' : '#FFFFFF',
      },
      Card: {
        borderRadiusLG: 12,
      },
      DatePicker: {
        controlHeight: 38,
      },
      Input: {
        controlHeight: 38,
      },
      InputNumber: {
        controlHeight: 38,
      },
      Modal: {
        borderRadiusLG: 14,
      },
      Select: {
        controlHeight: 38,
      },
    },
  };
});
</script>

<template>
  <ConfigProvider :locale="antdLocale" :theme="tokenTheme">
    <App>
      <RouterView />
    </App>
  </ConfigProvider>
</template>

<style>
html {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background: hsl(var(--muted-foreground) / 0.2);
  border-radius: 9999px;
}

::-webkit-scrollbar-thumb:hover {
  background: hsl(var(--muted-foreground) / 0.35);
}

::-webkit-scrollbar-track {
  background: transparent;
}
</style>
