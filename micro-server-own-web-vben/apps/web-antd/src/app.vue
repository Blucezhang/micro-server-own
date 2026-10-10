<script lang="ts" setup>
import { computed } from 'vue';

import { useAntdDesignTokens } from '@vben/hooks';
import { preferences, usePreferences } from '@vben/preferences';

import { App, ConfigProvider, theme } from 'ant-design-vue';

import { antdLocale } from '#/locales';

defineOptions({ name: 'App' });

const { isDark } = usePreferences();
const { tokens } = useAntdDesignTokens();

const tokenTheme = computed(() => {
  const algorithm = isDark.value
    ? [theme.darkAlgorithm]
    : [theme.defaultAlgorithm];

  // antd 紧凑模式算法
  if (preferences.app.compact) {
    algorithm.push(theme.compactAlgorithm);
  }

  return {
    algorithm,
    token: {
      ...tokens,
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
