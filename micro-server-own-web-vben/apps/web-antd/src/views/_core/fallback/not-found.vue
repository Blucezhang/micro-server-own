<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'Fallback404' });
</script>

<template>
  <FallbackView
    describe="抱歉，您访问的商城页面不存在、已被下线或网址输入有误。您可以返回工作台继续浏览或办理其他商城业务。"
    status="404"
    title="404 · 商城页面未找到"
  />
</template>
