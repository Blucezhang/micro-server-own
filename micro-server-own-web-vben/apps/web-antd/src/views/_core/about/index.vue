<script lang="ts" setup>
defineOptions({ name: 'About' });

const businessDomains = [
  {
    code: 'BUYER',
    description: '涵盖商品浏览、加购结算、订单履约追踪、收货地址与售后退换。',
    name: '买家中心',
  },
  {
    code: 'MERCHANT',
    description: '涵盖商品上下架、价格审计、库存配置、物流履约与资金结算。',
    name: '商家运营',
  },
  {
    code: 'SYSTEM',
    description: '涵盖操作员账号管理、RBAC 角色权限管控与评价内容风控治理。',
    name: '系统治理',
  },
] as const;

const architectureSpecs = [
  { label: '核心架构', value: '领域驱动微服务 (DDD) + 统一网关分发' },
  { label: '前端底座', value: 'Vue 3 + TypeScript + Vite + Ant Design Vue' },
  { label: '视觉规范', value: '克制视觉、柔和边界、清晰留白、主题 Token 自适应' },
  { label: '数据契约', value: '遵循后端真实 REST 接口契约，不伪造生产数据' },
] as const;
</script>

<template>
  <main class="about-page">
    <header class="about-head">
      <div>
        <span class="about-overline">SYSTEM · 关于平台</span>
        <h1 class="about-heading">微服务商城运营与治理平台</h1>
        <p class="about-subtitle">
          面向买家服务、商家履约经营与系统全局治理的一体化管理工作台。坚持安静运营工具设计原则，以真实业务契约为准，提供清晰一致的操作体验。
        </p>
      </div>
    </header>

    <div class="about-grid">
      <section aria-labelledby="domain-title" class="about-panel">
        <h2 id="domain-title" class="about-panel-title">业务服务矩阵</h2>
        <div class="domain-list">
          <div
            v-for="domain in businessDomains"
            :key="domain.code"
            class="domain-item"
          >
            <div class="domain-meta">
              <span class="domain-code">{{ domain.code }}</span>
              <strong class="domain-name">{{ domain.name }}</strong>
            </div>
            <p class="domain-desc">{{ domain.description }}</p>
          </div>
        </div>
      </section>

      <section aria-labelledby="specs-title" class="about-panel">
        <h2 id="specs-title" class="about-panel-title">技术与交互规范</h2>
        <dl class="spec-list">
          <div
            v-for="spec in architectureSpecs"
            :key="spec.label"
            class="spec-item"
          >
            <dt class="spec-label">{{ spec.label }}</dt>
            <dd class="spec-value">{{ spec.value }}</dd>
          </div>
        </dl>
      </section>
    </div>
  </main>
</template>

<style scoped>
.about-page {
  --about-line: hsl(var(--border));
  --about-text: hsl(var(--foreground));
  --about-muted: hsl(var(--muted-foreground));
  --about-surface: hsl(var(--card));
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 20px;
  min-width: 0;
  padding: clamp(16px, 2.5vw, 28px);
  color: var(--about-text);
}

.about-head {
  padding: 4px 0 18px;
  border-bottom: 1px solid var(--about-line);
}

.about-overline {
  color: var(--about-muted);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.about-heading {
  margin: 6px 0 6px;
  color: var(--about-text);
  font-size: clamp(22px, 2.2vw, 28px);
  font-weight: 650;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.about-subtitle {
  max-width: 680px;
  margin: 0;
  color: var(--about-muted);
  font-size: 13px;
  line-height: 1.6;
}

.about-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  align-items: start;
}

.about-panel {
  min-width: 0;
  padding: clamp(16px, 1.8vw, 22px);
  border: 1px solid var(--about-line);
  border-radius: 12px;
  background: var(--about-surface);
}

.about-panel-title {
  margin: 0 0 14px;
  color: var(--about-text);
  font-size: 15px;
  font-weight: 620;
  letter-spacing: -0.01em;
}

.domain-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.domain-item {
  padding: 10px 12px;
  border: 1px solid var(--about-line);
  border-radius: 8px;
  background: hsl(var(--background) / 0.5);
}

.domain-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.domain-code {
  display: inline-block;
  padding: 1px 6px;
  border-radius: 4px;
  background: hsl(var(--primary) / 0.1);
  color: hsl(var(--primary));
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.domain-name {
  color: var(--about-text);
  font-size: 13px;
  font-weight: 600;
}

.domain-desc {
  margin: 0;
  color: var(--about-muted);
  font-size: 12px;
  line-height: 1.5;
}

.spec-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
  padding: 0;
}

.spec-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--about-line);
}

.spec-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.spec-label {
  color: var(--about-muted);
  font-size: 12px;
  font-weight: 500;
}

.spec-value {
  margin: 0;
  color: var(--about-text);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
}

@media (max-width: 760px) {
  .about-grid {
    grid-template-columns: 1fr;
  }
}
</style>
