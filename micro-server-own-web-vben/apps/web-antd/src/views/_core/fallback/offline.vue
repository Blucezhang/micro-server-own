<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'FallbackOffline' });
</script>

<template>
  <FallbackView
    describe="无法连接到微商城服务网络，请检查您的网络连接与本地网关，确认网络恢复后点击重试。"
    show-refresh
    status="offline"
    title="网络连接中断 · 离线状态"
  />
</template>
