<script lang="ts" setup>
import { computed } from 'vue';

import { ThemeToggle } from '@vben/layouts';
import { preferences } from '@vben/preferences';

import logo from '#/assets/logo.svg';

defineOptions({ name: 'AuthLayout' });

const appName = computed(() => preferences.app.name || '微服务商城运营台');
const copyright = computed(() => preferences.copyright);

const serviceItems = [
  {
    desc: '商品浏览、订单履约跟踪与售后退换',
    title: '买家服务中心',
  },
  {
    desc: '商品发布、库存盘点、履约发货与结算对账',
    title: '商家运营工作台',
  },
  {
    desc: '账号角色授权、评价举报处置',
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
          <img :src="logo" alt="Logo" class="brand-logo" height="34" width="34" />
          <div class="brand-title-wrap">
            <h1 class="brand-title">{{ appName }}</h1>
            <span class="brand-subtitle">统一运营管理平台</span>
          </div>
        </header>

        <p class="brand-desc">
          面向买家服务、商户运营与平台治理的统一身份凭据与业务入口。
        </p>

        <div class="service-directory">
          <div
            v-for="item in serviceItems"
            :key="item.title"
            class="service-entry"
          >
            <div class="service-entry-title">{{ item.title }}</div>
            <div class="service-entry-desc">{{ item.desc }}</div>
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

<style scoped src="./auth.css"></style>
