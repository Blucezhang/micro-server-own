<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const sections = {
  buyer: {
    title: '买家工作台',
    description: '从选购、下单到售后，按需要进入对应事项。',
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
    title: '商家工作台',
    description: '商品、履约、售后与结算，从这里进入。',
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
    title: '系统工作台',
    description: '管理账号权限与评价治理；具体操作范围由服务端校验。',
    actions: [
      { title: '账号与商家', detail: '按已核实的账号 ID 授权', path: '/system/accounts' },
      { title: '角色权限', detail: '管理角色功能授权', path: '/system/roles' },
      { title: '评价治理', detail: '处理买家评价举报', path: '/system/reviews' },
    ],
  },
} as const;

const roleKey = computed(() => {
  const section = route.path.split('/')[1] as keyof typeof sections;
  return section in sections ? section : 'buyer';
});

const content = computed(() => sections[roleKey.value]);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">{{ content.title }}</h1>
        <p class="market-subtitle">{{ content.description }}</p>
      </div>
    </header>

    <section class="market-panel workspace-tasks" aria-labelledby="workspace-actions">
      <div class="workspace-section-heading">
        <h2 id="workspace-actions">常用操作</h2>
        <p>选择一项业务继续处理</p>
      </div>
      <div class="action-list">
        <button
          v-for="action in content.actions"
          :key="action.path"
          class="action-item"
          type="button"
          @click="router.push(action.path)"
        >
          <div class="action-copy">
            <strong class="action-title">{{ action.title }}</strong>
            <span class="action-detail">{{ action.detail }}</span>
          </div>
          <span class="action-arrow" aria-hidden="true">→</span>
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped src="./workspace.css"></style>
