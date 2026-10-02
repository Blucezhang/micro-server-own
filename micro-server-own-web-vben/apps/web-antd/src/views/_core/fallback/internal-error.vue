<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'Fallback500' });
</script>

<template>
  <FallbackView
    describe="微商城服务端处理请求时遇到了未预期的错误或服务超时。您可以尝试刷新重试，或返回工作台处理其他业务。"
    show-refresh
    status="500"
    title="500 · 微商城服务异常"
  />
</template>
