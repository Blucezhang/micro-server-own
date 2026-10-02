<script lang="ts" setup>
import { computed } from 'vue';

import { ThemeToggle } from '@vben/layouts';
import { preferences } from '@vben/preferences';

import logo from '#/assets/logo.svg';

defineOptions({ name: 'AuthLayout' });

const appName = computed(() => preferences.app.name || '微服务商城运营台');
const copyright = computed(() => preferences.copyright);

const domainFeatures = [
  {
    code: 'BUYER',
    desc: '商品选购、购物车结算、订单与物流跟踪、售后退换',
    title: '买家服务中心',
  },
  {
    code: 'MERCHANT',
    desc: '商品上下架、价格审计、库存管理、发货履约与结算提现',
    title: '商家运营工作台',
  },
  {
    code: 'SYSTEM',
    desc: '操作员账号分配、角色功能授权与评价内容风控治理',
    title: '系统治理控制台',
  },
] as const;
</script>

<template>
  <div class="auth-wrapper">
    <!-- Desktop Left Column: Brand & Service Scope -->
    <aside class="auth-brand-side">
      <div class="brand-content">
        <header class="brand-header">
          <img :src="logo" alt="Logo" class="brand-logo" height="38" width="38" />
          <div class="brand-title-wrap">
            <span class="brand-badge">MALL PLATFORM</span>
            <h1 class="brand-title">{{ appName }}</h1>
          </div>
        </header>

        <p class="brand-desc">
          集中办理买家下单履约、商户商品库存运营与平台治理任务。
        </p>

        <div class="features-list">
          <div v-for="feature in domainFeatures" :key="feature.code" class="feature-card">
            <div class="feature-head">
              <span class="feature-code">{{ feature.code }}</span>
              <strong class="feature-title">{{ feature.title }}</strong>
            </div>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </div>

        <footer class="brand-footer">
          <span>微商城统一运营管理平台</span>
        </footer>
      </div>
    </aside>

    <!-- Right Column: Scrollable Auth Form Panel -->
    <main class="auth-main-side">
      <!-- Top Toolbar -->
      <header class="auth-topbar">
        <div class="mobile-brand">
          <img :src="logo" alt="Logo" class="brand-logo-sm" height="28" width="28" />
          <span class="mobile-title">{{ appName }}</span>
        </div>
        <div class="topbar-actions">
          <ThemeToggle />
        </div>
      </header>

      <!-- Center Form Container with Natural Height & Scroll -->
      <div class="form-container">
        <RouterView v-slot="{ Component, route }">
          <Transition appear mode="out-in" name="fade-slide">
            <KeepAlive :include="['Login']">
              <component :is="Component" :key="route.fullPath" class="auth-form-card" />
            </KeepAlive>
          </Transition>
        </RouterView>
      </div>

      <!-- Copyright Footer -->
      <footer v-if="copyright.enable" class="auth-footer">
        <p>
          Copyright © {{ copyright.date }} {{ copyright.companyName }}
          <template v-if="copyright.icp"> · {{ copyright.icp }}</template>
        </p>
      </footer>
    </main>
  </div>
</template>

<style scoped>
.auth-wrapper {
  display: flex;
  width: 100vw;
  height: 100vh;
  height: 100dvh;
  min-height: 100vh;
  min-height: 100dvh;
  overflow: hidden;
  background-color: hsl(var(--background));
  color: hsl(var(--foreground));
}

/* Left Brand Panel (Desktop) */
.auth-brand-side {
  display: none;
  flex: 0 0 440px;
  max-width: 460px;
  height: 100%;
  border-right: 1px solid hsl(var(--border));
  background: hsl(var(--muted) / 0.28);
  box-sizing: border-box;
}

@media (min-width: 1024px) {
  .auth-brand-side {
    display: flex;
  }
}

.brand-content {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding: clamp(28px, 3.5vw, 44px);
  box-sizing: border-box;
  overflow-y: auto;
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  border-radius: 8px;
  flex-shrink: 0;
}

.brand-title-wrap {
  display: flex;
  flex-direction: column;
}

.brand-badge {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: hsl(var(--primary));
  text-transform: uppercase;
}

.brand-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: hsl(var(--foreground));
}

.brand-desc {
  margin: 16px 0 20px;
  font-size: 13px;
  line-height: 1.55;
  color: hsl(var(--muted-foreground));
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: auto;
}

.feature-card {
  padding: 12px 14px;
  border: 1px solid hsl(var(--border) / 0.8);
  border-radius: 8px;
  background: hsl(var(--card));
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.feature-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.feature-code {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 4px;
  background: hsl(var(--primary) / 0.1);
  color: hsl(var(--primary));
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.feature-title {
  font-size: 13px;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.feature-desc {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: hsl(var(--muted-foreground));
}

.brand-footer {
  margin-top: 20px;
  padding-top: 14px;
  border-top: 1px solid hsl(var(--border));
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}

/* Right Main Panel */
.auth-main-side {
  display: flex;
  flex-direction: column;
  flex: 1;
  height: 100%;
  min-width: 0;
  overflow-y: auto;
  position: relative;
  box-sizing: border-box;
}

.auth-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  flex-shrink: 0;
}

.mobile-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (min-width: 1024px) {
  .mobile-brand {
    visibility: hidden;
  }
}

.brand-logo-sm {
  border-radius: 6px;
}

.mobile-title {
  font-size: 15px;
  font-weight: 650;
  color: hsl(var(--foreground));
}

.topbar-actions {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.form-container {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1 0 auto;
  padding: clamp(16px, 3vh, 36px) 20px;
  box-sizing: border-box;
  min-height: min-content;
}

.auth-form-card {
  width: 100%;
  max-width: 420px;
  margin: auto;
}

.auth-footer {
  padding: 14px 20px;
  text-align: center;
  flex-shrink: 0;
  border-top: 1px solid hsl(var(--border) / 0.5);
}

.auth-footer p {
  margin: 0;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (prefers-reduced-motion: reduce) {
  .fade-slide-enter-active,
  .fade-slide-leave-active {
    transition: none !important;
  }
  .fade-slide-enter-from,
  .fade-slide-leave-to {
    opacity: 1 !important;
    transform: none !important;
  }
}
</style>
