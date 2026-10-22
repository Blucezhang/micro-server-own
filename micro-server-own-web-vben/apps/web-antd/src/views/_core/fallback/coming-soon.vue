<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'FallbackComingSoon' });
</script>

<template>
  <FallbackView
    describe="该业务模块暂未开放，当前正在建设中。请返回工作台办理其他经营业务。"
    status="coming-soon"
    title="功能建设中"
  />
</template>
