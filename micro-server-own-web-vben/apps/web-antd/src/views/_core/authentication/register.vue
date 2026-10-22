<script lang="ts" setup>
import AuthNotice from '#/components/auth-notice.vue';

defineOptions({ name: 'Register' });
</script>

<template>
  <AuthNotice
    card-desc="微服务商城运营台实行统一账号授权管理，当前不开放公开在线注册。各端业务操作账号由平台管理员按需统一配置与分配。"
    card-title="公开在线注册暂未开放"
    subtitle="开放式在线注册通道暂未开启"
    tag="统一授权分配"
    :tips="[
      '业务账号请联系平台系统管理员统一开通与授权',
    ]"
    title="账号注册申请"
  />
</template>
