<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'Fallback404' });
</script>

<template>
  <FallbackView
    describe="您访问的页面不存在或已被移除。请检查访问路径，或返回工作台继续操作。"
    status="404"
    title="页面未找到"
  />
</template>
