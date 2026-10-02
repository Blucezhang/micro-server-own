<script lang="ts" setup>
import AuthNotice from '#/components/auth-notice.vue';

defineOptions({ name: 'Register' });
</script>

<template>
  <AuthNotice
    card-desc="微商城系统整合了买家服务、商家履约经营与系统治理多端业务，目前实行准入制，暂不开放公开无审核的在线注册表单。新商户与买家账户由平台管理员统一审核与开通。"
    card-title="微商城运营台采用统一授权与开通机制"
    subtitle="开放式在线注册暂未开启"
    tag="准入审核制"
    :tips="[
      '已签约客户：请使用管理员下发的正式账号密码登录',
      '体验与评估：可在登录页免密进入演示工作台',
    ]"
    title="账号注册申请"
  />
</template>
