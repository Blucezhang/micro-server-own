<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'FallbackComingSoon' });
</script>

<template>
  <FallbackView
    describe="该微商城业务功能正在规划与持续迭代中，当前环境暂未对外开放。您可以返回工作台继续办理其他经营事项。"
    status="coming-soon"
    title="功能建设中 · 敬请期待"
  />
</template>
