<script lang="ts" setup>
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';

import { Button } from 'ant-design-vue';

import { useAuthStore } from '#/store';

defineOptions({ name: 'CodeLogin' });

const router = useRouter();
const authStore = useAuthStore();

const demoActors = [
  { label: '买家端演示', value: 'BUYER' },
  { label: '商家端演示', value: 'MERCHANT' },
  { label: '系统治理演示', value: 'SYSTEM' },
] as const;

function goToLogin() {
  router.push(LOGIN_PATH);
}
</script>

<template>
  <div class="auth-alt-page">
    <div class="auth-alt-header">
      <h2 class="auth-alt-title">手机验证码登录 📲</h2>
      <p class="auth-alt-subtitle">当前环境未开放短信验证通道</p>
    </div>

    <div class="auth-alt-card">
      <div class="alt-tag">功能暂未开启</div>
      <h3 class="alt-card-title">当前版本仅支持账号密码登录与演示模式</h3>
      <p class="alt-card-desc">
        微商城运营台目前未连接第三方短信网关，暂不提供手机短信验证码登录。为避免误导，系统已关闭空验证码提交入口。请使用已分配的账号及密码在主登录入口完成身份认证。
      </p>
      <div class="alt-card-tips">
        <div>• 真实业务：支持买家端、商家端、系统治理端已分配账号</div>
        <div>• 本地走查：开发调试环境可免密直接进入各端演示工作台</div>
      </div>
    </div>

    <Button block size="large" type="primary" @click="goToLogin">
      返回账号密码登录
    </Button>

    <div v-if="authStore.uiDemoEnabled" class="demo-entry">
      <p class="demo-entry-tip">本地 UI 走查入口（未连接真实业务网关）：</p>
      <button
        v-for="actor in demoActors"
        :key="actor.value"
        class="demo-btn"
        type="button"
        @click="authStore.enterUiDemo(actor.value)"
      >
        {{ actor.label }}
      </button>
    </div>

    <div class="auth-alt-footer">
      <a class="auth-back-link" @click="goToLogin">← 返回登录主入口</a>
    </div>
  </div>
</template>

<style scoped>
.auth-alt-page {
  box-sizing: border-box;
  width: 100%;
  max-width: 420px;
  margin: 0 auto;
}

.auth-alt-header {
  margin-bottom: 24px;
}

.auth-alt-title {
  margin: 0 0 8px;
  color: hsl(var(--foreground));
  font-size: clamp(22px, 2.6vw, 28px);
  font-weight: 700;
  line-height: 1.25;
}

.auth-alt-subtitle {
  margin: 0;
  color: hsl(var(--muted-foreground));
  font-size: 14px;
  line-height: 1.5;
}

.auth-alt-card {
  margin-bottom: 24px;
  padding: 16px 18px;
  border: 1px solid hsl(var(--border));
  border-radius: 10px;
  background: hsl(var(--card));
  box-shadow: 0 1px 3px hsl(var(--foreground) / 0.04);
}

.alt-tag {
  display: inline-block;
  margin-bottom: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 500;
}

.alt-card-title {
  margin: 0 0 8px;
  color: hsl(var(--foreground));
  font-size: 14px;
  font-weight: 600;
  line-height: 1.45;
}

.alt-card-desc {
  margin: 0 0 12px;
  color: hsl(var(--muted-foreground));
  font-size: 13px;
  line-height: 1.6;
}

.alt-card-tips {
  display: grid;
  gap: 5px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.5;
}

.demo-entry {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  padding: 12px 14px;
  border: 1px dashed hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--card) / 0.5);
}

.demo-entry-tip {
  width: 100%;
  margin: 0 0 4px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 1.5;
}

.demo-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 5px 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.16s ease;
}

.demo-btn:hover {
  border-color: hsl(var(--primary));
  background: hsl(var(--primary) / 0.08);
  color: hsl(var(--primary));
}

.demo-btn:focus-visible {
  outline: 2px solid hsl(var(--primary));
  outline-offset: 1px;
}

.auth-alt-footer {
  margin-top: 18px;
  text-align: center;
}

.auth-back-link {
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.16s ease;
}

.auth-back-link:hover {
  color: hsl(var(--primary));
}
</style>
