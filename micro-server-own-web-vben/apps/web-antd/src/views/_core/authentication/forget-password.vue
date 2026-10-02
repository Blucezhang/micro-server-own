<script lang="ts" setup>
import AuthNotice from '#/components/auth-notice.vue';

defineOptions({ name: 'ForgetPassword' });
</script>

<template>
  <AuthNotice
    card-desc="为保障商城商户数据与系统资产安全，本系统未开放公开邮箱或短信重置通道，不会虚构发送重置邮件。如遇密码遗失，请联系微商城系统管理员进行身份核验与密码重置。"
    card-title="当前环境不提供线上自主重置"
    subtitle="线上自主重置服务暂未开放"
    tag="需管理员重置"
    :tips="[
      '正式环境：请联系微商城系统运维或管理员重置凭据',
      '演示走查：可直接通过主登录入口的演示角色进入',
    ]"
    title="重置与找回密码"
  />
</template>
