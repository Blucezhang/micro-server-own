<script lang="ts" setup>
import AuthNotice from '#/components/auth-notice.vue';

defineOptions({ name: 'ForgetPassword' });
</script>

<template>
  <AuthNotice
    card-desc="为保障商城多端业务与账户资产安全，运营台未开放线上公开重置通道。若遗失密码，请联系平台系统管理员核验身份后协助重置凭据。"
    card-title="线上自主重置暂未开放"
    subtitle="线上凭据自主重置通道暂未开放"
    tag="需管理员协助"
    :tips="[
      '请联系平台系统管理员或运维人员办理凭据重置',
    ]"
    title="找回与重置密码"
  />
</template>
