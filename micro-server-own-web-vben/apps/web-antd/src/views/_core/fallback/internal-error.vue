<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'Fallback500' });
</script>

<template>
  <FallbackView
    describe="系统处理当前请求时遇到异常。请尝试刷新重试，或返回工作台继续其他操作。"
    show-refresh
    status="500"
    title="服务异常"
  />
</template>
