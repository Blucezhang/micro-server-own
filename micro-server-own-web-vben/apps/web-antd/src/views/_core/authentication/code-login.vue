<script lang="ts" setup>
import AuthNotice from '#/components/auth-notice.vue';

defineOptions({ name: 'CodeLogin' });
</script>

<template>
  <AuthNotice
    card-desc="运营台当前未接入第三方短信验证服务。为确保账号凭据安全，请使用系统已授权的账号与密码在登录页完成身份认证。"
    card-title="短信验证码登录暂未开放"
    subtitle="当前环境未开放短信验证通道"
    tag="功能暂未开放"
    :tips="[
      '支持在登录页选择买家端、商家端或系统治理端进行密码登录',
    ]"
    title="手机验证码登录"
  />
</template>
