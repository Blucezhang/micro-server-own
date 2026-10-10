<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const sections = {
  buyer: {
    label: 'BUYER · 买家中心',
    title: '从选购到售后，都在这里。',
    description: '浏览商品、管理订单或跟进售后，直接进入对应任务。',
    actions: [
      { title: '浏览商品', detail: '搜索、收藏与评价', path: '/buyer/catalog' },
      { title: '购物车', detail: '核对商品并开始结算', path: '/buyer/cart' },
      { title: '订单与物流', detail: '查看订单、签收与物流轨迹', path: '/buyer/orders' },
      { title: '售后服务', detail: '申请、退货与进度查询', path: '/buyer/after-sales' },
      { title: '我的收藏', detail: '继续查看关注的商品', path: '/buyer/favorites' },
      { title: '账号设置', detail: '资料与登录会话', path: '/buyer/account' },
    ],
  },
  merchant: {
    label: 'MERCHANT · 商家运营',
    title: '处理今天的经营事项。',
    description: '商品、履约、售后与资金操作集中在一个入口。',
    actions: [
      { title: '商品管理', detail: '发布、编辑与上下架', path: '/merchant/products' },
      { title: '发货履约', detail: '待发货订单与物流录入', path: '/merchant/orders' },
      { title: '售后处理', detail: '审核、收退货与换货发货', path: '/merchant/after-sales' },
      { title: '商品评价', detail: '查看并回复评价', path: '/merchant/reviews' },
      { title: '结算中心', detail: '余额与提现申请', path: '/merchant/settlement' },
      { title: '账号设置', detail: '资料与登录会话', path: '/merchant/account' },
    ],
  },
  system: {
    label: 'SYSTEM · 系统治理',
    title: '保持权限与内容清晰可控。',
    description: '账号授权、角色配置与评价治理集中展示，操作范围由服务端权限规则校验。',
    actions: [
      { title: '账号与商家', detail: '按已核实的账号 ID 授权', path: '/system/accounts' },
      { title: '角色权限', detail: '管理角色功能授权', path: '/system/roles' },
      { title: '评价治理', detail: '处理买家评价举报', path: '/system/reviews' },
    ],
  },
} as const;
const content = computed(() => {
  const section = route.path.split('/')[1] as keyof typeof sections;
  return sections[section] || sections.buyer;
});
</script>

<template>
  <main class="market-page workspace-page" :class="`workspace-${route.path.split('/')[1]}`">
    <header class="market-head workspace-head">
      <div>
        <span class="market-overline">{{ content.label }}</span>
        <h1 class="market-heading">{{ content.title }}</h1>
        <p class="market-subtitle">{{ content.description }}</p>
      </div>
    </header>
    <section class="workspace-tasks" aria-labelledby="workspace-actions">
      <div class="workspace-section-heading">
        <div>
          <span class="market-overline">WORKSPACE</span>
          <h2 id="workspace-actions">快捷入口</h2>
          <p class="workspace-section-note">按日常处理顺序排列</p>
        </div>
      </div>
      <div class="action-list">
        <button v-for="(action, index) in content.actions" :key="action.path" class="action" type="button" @click="router.push(action.path)">
          <span class="action-index">{{ String(index + 1).padStart(2, '0') }}</span>
          <span class="action-copy"><strong>{{ action.title }}</strong><small>{{ action.detail }}</small></span>
          <span class="action-arrow" aria-hidden="true">→</span>
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.workspace-page { --workspace-accent: hsl(var(--primary)); }
.workspace-merchant { --workspace-accent: hsl(161 56% 34%); }
.workspace-system { --workspace-accent: hsl(248 48% 55%); }
.workspace-tasks { min-width: 0; padding: 24px; border: 1px solid var(--market-line); border-radius: var(--mall-radius-xl); background: var(--market-surface); box-shadow: 0 1px 2px rgb(28 36 48 / 0.035), 0 10px 28px rgb(28 36 48 / 0.025); }
.workspace-section-heading { display: flex; align-items: end; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.workspace-section-heading h2 { margin: 5px 0 0; color: var(--market-text); font-size: 17px; font-weight: 650; letter-spacing: -0.02em; }
.workspace-section-note { margin: 4px 0 0; color: var(--market-muted); font-size: 12px; }
.action-list { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.action { display: flex; align-items: center; gap: 13px; width: 100%; min-height: 92px; padding: 16px; border: 1px solid var(--market-line); border-radius: 11px; background: hsl(var(--background) / 0.72); color: var(--market-text); cursor: pointer; text-align: left; transition: border-color 160ms ease, box-shadow 160ms ease, transform 160ms ease; }
.action:hover { transform: translateY(-2px); border-color: color-mix(in srgb, var(--workspace-accent) 36%, var(--market-line)); box-shadow: 0 8px 20px rgb(28 36 48 / 0.07); }
.action:focus-visible { outline: 2px solid var(--workspace-accent); outline-offset: 2px; }
.action-index { display: grid; width: 34px; height: 34px; flex: 0 0 auto; place-items: center; border-radius: 10px; background: color-mix(in srgb, var(--workspace-accent) 10%, white); color: var(--workspace-accent); font-size: 11px; font-weight: 700; font-variant-numeric: tabular-nums; }
.action-copy { display: grid; min-width: 0; flex: 1; gap: 5px; }
.action-copy strong { overflow: hidden; font-size: 14px; font-weight: 650; text-overflow: ellipsis; white-space: nowrap; }
.action-copy small { overflow: hidden; color: var(--market-muted); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.action-arrow { color: var(--market-muted); font-size: 17px; transition: color 160ms ease, transform 160ms ease; }
.action:hover .action-arrow { transform: translateX(3px); color: var(--workspace-accent); }
@media (max-width: 1120px) { .action-list { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 760px) { .workspace-tasks { padding: 18px; } .action-list { grid-template-columns: 1fr; } }
</style>
