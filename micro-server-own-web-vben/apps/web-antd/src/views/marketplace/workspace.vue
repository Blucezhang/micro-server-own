<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const sections = {
  buyer: {
    label: 'BUYER · 买家中心',
    title: '从选购到售后，都在这里。',
    description: '按购物流程进入常用页面。订单金额、库存与状态始终以服务端为准。',
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
    description: '商品、履约和售后按任务集中呈现；没有实时数据时不显示虚构统计。',
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
    description: '进入已有的治理操作。列表与撤权能力仍需后端契约，不展示不可用入口。',
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
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">{{ content.label }}</span>
        <h1 class="market-heading">{{ content.title }}</h1>
        <p class="market-subtitle">{{ content.description }}</p>
      </div>
    </header>
    <section class="market-panel" aria-labelledby="workspace-actions">
      <h2 id="workspace-actions" class="market-panel-title">常用任务</h2>
      <div class="action-list">
        <button v-for="(action, index) in content.actions" :key="action.path" class="action" type="button" @click="router.push(action.path)">
          <span class="action-index">{{ String(index + 1).padStart(2, '0') }}</span>
          <span class="action-copy"><strong>{{ action.title }}</strong><small>{{ action.detail }}</small></span>
          <span class="action-arrow" aria-hidden="true">↗</span>
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.action-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1px 24px; }
.action { display: flex; align-items: center; gap: 14px; width: 100%; min-height: 84px; padding: 14px 0; border: 0; border-bottom: 1px solid var(--market-line); background: transparent; color: var(--market-text); cursor: pointer; text-align: left; }
.action:hover .action-copy strong, .action:focus-visible .action-copy strong { color: hsl(var(--primary)); }
.action:focus-visible { outline: 2px solid hsl(var(--primary)); outline-offset: 2px; }
.action-index { align-self: flex-start; min-width: 25px; color: var(--market-muted); font-size: 11px; font-variant-numeric: tabular-nums; }
.action-copy { display: grid; gap: 4px; flex: 1; }
.action-copy strong { font-size: 15px; font-weight: 620; transition: color .16s ease; }
.action-copy small { color: var(--market-muted); font-size: 12px; }
.action-arrow { align-self: flex-start; color: var(--market-muted); font-size: 18px; }
@media (max-width: 760px) { .action-list { grid-template-columns: 1fr; } }
</style>
