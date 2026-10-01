<script lang="ts" setup>
import type { ToolbarType } from '@vben/layouts';

import { computed } from 'vue';

import { AuthPageLayout } from '@vben/layouts';
import { preferences } from '@vben/preferences';

const appName = computed(() => preferences.app.name || '微服务商城运营台');
const logo = computed(() => preferences.logo.source);
const logoDark = computed(() => preferences.logo.sourceDark);
const toolbarList: ToolbarType[] = ['theme'];
</script>

<template>
  <div class="auth-layout-container">
    <AuthPageLayout
      :app-name="appName"
      :logo="logo"
      :logo-dark="logoDark"
      :toolbar-list="toolbarList"
      page-description="统一连接买家服务、商家履约经营与系统全局治理的一体化运营工作台"
      page-title="微服务商城运营台"
    />
  </div>
</template>

<style scoped>
.auth-layout-container {
  height: 100%;
  width: 100%;
}
</style>
