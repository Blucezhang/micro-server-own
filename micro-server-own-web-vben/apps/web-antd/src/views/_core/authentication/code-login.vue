<script lang="ts" setup>
import AuthNotice from '#/components/auth-notice.vue';

defineOptions({ name: 'CodeLogin' });
</script>

<template>
  <AuthNotice
    card-desc="微商城运营台目前未连接第三方短信网关，暂不提供手机短信验证码登录。为避免误导，系统已关闭空验证码提交入口。请使用已分配的账号及密码在主登录入口完成身份认证。"
    card-title="当前版本仅支持账号密码登录与演示模式"
    subtitle="当前环境未开放短信验证通道"
    tag="功能暂未开启"
    :tips="[
      '真实业务：支持买家端、商家端、系统治理端已分配账号',
      '本地走查：开发调试环境可免密直接进入各端演示工作台',
    ]"
    title="手机验证码登录"
  />
</template>
