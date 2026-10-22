<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'Fallback403' });
</script>

<template>
  <FallbackView
    describe="当前登录账号未获得该页面的访问权限。请核实账号角色，或切换至具备权限的账号重试。"
    status="403"
    title="无访问权限"
  />
</template>
