<script lang="ts" setup>
import AuthNotice from '#/components/auth-notice.vue';

defineOptions({ name: 'QrCodeLogin' });
</script>

<template>
  <AuthNotice
    card-desc="运营台当前未接入移动端扫码鉴权服务。无论桌面端或移动端浏览器，均可在主登录入口使用已分配的账号与密码完成身份认证。"
    card-title="扫码鉴权服务暂未开放"
    subtitle="移动端扫码授权服务暂未开放"
    tag="功能暂未开放"
    :tips="[
      '支持桌面端与移动端浏览器直接使用账号密码登录',
    ]"
    title="扫码安全登录"
  />
</template>
