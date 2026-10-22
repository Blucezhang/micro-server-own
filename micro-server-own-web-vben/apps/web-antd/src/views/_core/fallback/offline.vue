<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'FallbackOffline' });
</script>

<template>
  <FallbackView
    describe="无法连接到服务网络。请检查本地网络连接后重试。"
    show-refresh
    status="offline"
    title="网络连接中断"
  />
</template>
