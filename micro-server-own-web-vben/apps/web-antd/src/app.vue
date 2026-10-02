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
        '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif',
      fontSize: 13,
      controlHeight: 36,
      borderRadius: 6,
      borderRadiusLG: 10,
      borderRadiusSM: 4,
      wireframe: false,
    },
    components: {
      Button: {
        controlHeight: 36,
      },
      Card: {
        borderRadiusLG: 10,
      },
      DatePicker: {
        controlHeight: 36,
      },
      Input: {
        controlHeight: 36,
      },
      InputNumber: {
        controlHeight: 36,
      },
      Modal: {
        borderRadiusLG: 12,
      },
      Select: {
        controlHeight: 36,
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
