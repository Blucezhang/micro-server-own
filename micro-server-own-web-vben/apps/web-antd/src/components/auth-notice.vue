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
      <span v-if="tag" class="notice-tag">{{ tag }}</span>
      <h3 class="notice-body-title">{{ cardTitle }}</h3>
      <p class="notice-body-desc">{{ cardDesc }}</p>

      <ul v-if="tips.length" class="notice-tips">
        <li v-for="(tip, idx) in tips" :key="idx">{{ tip }}</li>
      </ul>
    </div>

    <div class="notice-actions">
      <Button block type="primary" @click="goToLogin">
        返回账号密码登录
      </Button>
    </div>

    <footer class="notice-footer">
      <button class="notice-back-btn" type="button" @click="goToLogin">
        ← 返回登录主入口
      </button>
    </footer>
  </div>
</template>

<style scoped>
.auth-notice-card {
  width: 100%;
  max-width: 420px;
  margin: 0 auto;
  box-sizing: border-box;
}

.notice-header {
  margin-bottom: 20px;
}

.notice-title {
  margin: 0 0 6px;
  color: hsl(var(--foreground));
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.notice-subtitle {
  margin: 0;
  color: hsl(var(--muted-foreground));
  font-size: 13px;
  line-height: 1.5;
}

.notice-body {
  margin-bottom: 20px;
  padding: 16px 18px;
  border: 1px solid hsl(var(--border));
  border-radius: 10px;
  background: hsl(var(--card));
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.notice-tag {
  display: inline-block;
  margin-bottom: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.notice-body-title {
  margin: 0 0 8px;
  color: hsl(var(--foreground));
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
}

.notice-body-desc {
  margin: 0 0 12px;
  color: hsl(var(--muted-foreground));
  font-size: 13px;
  line-height: 1.6;
}

.notice-tips {
  display: grid;
  gap: 4px;
  margin: 0;
  padding: 0;
  list-style: none;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.5;
}

.notice-tips li {
  position: relative;
  padding-left: 12px;
}

.notice-tips li::before {
  content: "•";
  position: absolute;
  left: 0;
  color: hsl(var(--primary));
}

.notice-actions {
  margin-top: 4px;
}

.notice-footer {
  margin-top: 16px;
  text-align: center;
}

.notice-back-btn {
  background: transparent;
  border: none;
  color: hsl(var(--muted-foreground));
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: color 0.15s ease;
}

.notice-back-btn:hover {
  color: hsl(var(--primary));
}

.notice-back-btn:focus-visible {
  outline: 2px solid hsl(var(--primary));
  outline-offset: 2px;
}
</style>
