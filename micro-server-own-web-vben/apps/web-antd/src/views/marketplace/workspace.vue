<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Card, Col, Row, Statistic, Tag } from 'ant-design-vue';

const route = useRoute();
const router = useRouter();

const content = computed(() => {
  const section = String(route.path.split('/')[1] || 'buyer');
  const maps = {
    buyer: {
      actions: [
        ['继续选购', '/buyer/catalog'],
        ['查看购物车', '/buyer/cart'],
        ['订单中心', '/buyer/orders'],
      ],
      metrics: [['待支付', 0], ['待收货', 0], ['售后中', 0]],
      title: '买家工作台',
    },
    merchant: {
      actions: [
        ['管理商品', '/merchant/products'],
        ['处理订单', '/merchant/orders'],
        ['检查库存', '/merchant/inventory'],
      ],
      metrics: [['待发货', 0], ['库存预警', 0], ['待处理咨询', 0]],
      title: '商家经营工作台',
    },
    system: {
      actions: [
        ['账号与商家', '/system/accounts'],
        ['角色权限', '/system/roles'],
        ['事件排查', '/system/outbox'],
      ],
      metrics: [['待审核', 0], ['治理任务', 0], ['异常事件', 0]],
      title: '系统治理工作台',
    },
  } as const;
  return maps[section as keyof typeof maps] || maps.buyer;
});
</script>

<template>
  <main class="workspace-page">
    <section class="workspace-hero">
      <div>
        <Tag color="cyan">Vben 5 · 运营协同</Tag>
        <h1>{{ content.title }}</h1>
        <p>以业务任务为中心组织信息、操作和权限，让今天需要处理的事情一眼可见。</p>
      </div>
      <div class="hero-orb" aria-hidden="true" />
    </section>

    <Row :gutter="16" class="metric-row">
      <Col v-for="([label, value]) in content.metrics" :key="label" :lg="8" :md="8" :span="24">
        <Card :bordered="false" class="metric-card">
          <Statistic :title="label" :value="value" />
          <span>连接网关后将显示实时数据</span>
        </Card>
      </Col>
    </Row>

    <Card :bordered="false" class="action-card" title="快速开始">
      <button v-for="([label, path]) in content.actions" :key="path" type="button" @click="router.push(path)">
        <span>{{ label }}</span>
        <span>→</span>
      </button>
    </Card>
  </main>
</template>

<style scoped>
.workspace-page { display: grid; gap: 16px; padding: 24px; }
.workspace-hero { align-items: center; background: linear-gradient(120deg, #082f49, #0f766e 55%, #14b8a6); border-radius: 18px; color: white; display: flex; justify-content: space-between; min-height: 180px; overflow: hidden; padding: 30px 34px; position: relative; }
.workspace-hero h1 { font-size: 28px; font-weight: 700; margin: 14px 0 8px; }
.workspace-hero p { color: rgba(255, 255, 255, .78); margin: 0; max-width: 560px; }
.hero-orb { background: rgba(255,255,255,.16); border: 28px solid rgba(255,255,255,.1); border-radius: 999px; height: 180px; margin-right: 30px; width: 180px; }
.metric-row { margin: 0 !important; }
.metric-card span { color: #64748b; display: block; font-size: 12px; margin-top: 12px; }
.action-card :deep(.ant-card-body) { display: grid; gap: 12px; grid-template-columns: repeat(3, minmax(0, 1fr)); }
.action-card button { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; cursor: pointer; display: flex; font-size: 15px; justify-content: space-between; padding: 18px; text-align: left; transition: .2s ease; }
.action-card button:hover { border-color: #14b8a6; color: #0f766e; transform: translateY(-2px); }
@media (max-width: 768px) { .workspace-page { padding: 16px; } .hero-orb { display: none; } .action-card :deep(.ant-card-body) { grid-template-columns: 1fr; } }
</style>
