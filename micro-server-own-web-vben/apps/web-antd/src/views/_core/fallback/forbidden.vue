<script lang="ts" setup>
import FallbackView from '#/components/fallback-view.vue';

defineOptions({ name: 'Fallback403' });
</script>

<template>
  <FallbackView
    describe="您当前的登录身份或角色未获得访问该微商城模块的授权。请核实您所在的业务端（买家端、商家端或系统治理端），或切换到具备权限的账号重试。"
    status="403"
    title="403 · 无权访问该商城资源"
  />
</template>
