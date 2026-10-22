<script lang="ts" setup>
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';

import { Button } from 'ant-design-vue';

interface Props {
  cardDesc: string;
  cardTitle: string;
  subtitle: string;
  tag?: string;
  tips?: string[];
  title: string;
}

withDefaults(defineProps<Props>(), {
  tag: '暂未开放',
  tips: () => [],
});

const router = useRouter();

function goToLogin() {
  router.push(LOGIN_PATH);
}
</script>

<template>
  <div class="auth-notice-card">
    <header class="notice-header">
      <h2 class="notice-title">{{ title }}</h2>
      <p class="notice-subtitle">{{ subtitle }}</p>
    </header>

    <div class="notice-body">
      <div v-if="tag" class="notice-tag-wrap">
        <span class="notice-tag">{{ tag }}</span>
      </div>
      <h3 class="notice-body-title">{{ cardTitle }}</h3>
      <p class="notice-body-desc">{{ cardDesc }}</p>

      <ul v-if="tips && tips.length" class="notice-tips">
        <li v-for="(tip, idx) in tips" :key="idx">{{ tip }}</li>
      </ul>
    </div>

    <div class="notice-actions">
      <Button block type="primary" @click="goToLogin">
        返回账号密码登录
      </Button>
    </div>
  </div>
</template>

<style scoped src="./auth-notice.css"></style>
