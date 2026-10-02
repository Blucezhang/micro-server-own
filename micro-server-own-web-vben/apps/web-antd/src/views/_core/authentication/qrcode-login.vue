<script lang="ts" setup>
import AuthNotice from '#/components/auth-notice.vue';

defineOptions({ name: 'QrCodeLogin' });
</script>

<template>
  <AuthNotice
    card-desc="微商城移动端 App 与微信小程序授权扫码服务正在规划中，当前运营环境暂未接入扫码鉴权服务端。请使用已分配的各端账号与密码在主登录入口完成身份认证。"
    card-title="当前版本仅支持桌面端账号密码登录"
    subtitle="移动端扫码授权服务暂未开放"
    tag="功能暂未开启"
    :tips="[
      '桌面端登录：支持买家端、商家端、系统治理端账号',
      '演示模式：可在登录页直接选择角色进入演示工作台',
    ]"
    title="扫码安全登录"
  />
</template>
