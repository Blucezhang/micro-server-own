<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { Button, Card, Popconfirm, Tag } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';

interface Address { id: number; recipientName: string; mobile: string; province: string; city: string; district: string; detail: string; defaultAddress?: boolean }
const addresses = ref<Address[]>([]);
const loading = ref(false);

async function load() { loading.value = true; try { addresses.value = await buyerApi.addresses() as Address[]; } finally { loading.value = false; } }
async function setDefault(id: number) { await buyerApi.defaultAddress(id); await load(); }
async function remove(id: number) { await buyerApi.deleteAddress(id); await load(); }
onMounted(load);
</script>

<template>
  <main class="addresses-page">
    <Card :bordered="false">
      <div class="heading"><div><Tag color="cyan">买家中心</Tag><h1>收货地址</h1><p>订单会保存当前收货信息快照。</p></div><Button @click="load">刷新</Button></div>
    </Card>
    <Card :bordered="false" :loading="loading">
      <div v-if="addresses.length" class="address-grid">
        <article v-for="address in addresses" :key="address.id" class="address-card">
          <div><strong>{{ address.recipientName }}</strong><span>{{ address.mobile }}</span></div>
          <p>{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</p>
          <Tag v-if="address.defaultAddress" color="green">默认地址</Tag>
          <footer><Button v-if="!address.defaultAddress" type="link" @click="setDefault(address.id)">设为默认</Button><Popconfirm title="确认删除该地址？" @confirm="remove(address.id)"><Button danger type="link">删除</Button></Popconfirm></footer>
        </article>
      </div>
      <p v-else class="empty">暂无收货地址。地址新增与编辑接口已保留，后续接入统一表单抽屉。</p>
    </Card>
  </main>
</template>

<style scoped>
.addresses-page { display:grid; gap:16px; padding:24px; }.heading{align-items:center;display:flex;justify-content:space-between}h1{font-size:24px;margin:10px 0 6px}p{color:#64748b;margin:0}.address-grid{display:grid;gap:16px;grid-template-columns:repeat(auto-fit,minmax(260px,1fr))}.address-card{border:1px solid #e2e8f0;border-radius:12px;padding:18px}.address-card span{color:#64748b;margin-left:12px}.address-card p{line-height:1.7;margin:14px 0}.address-card footer{margin-top:14px}.empty{padding:30px;text-align:center}@media(max-width:640px){.addresses-page{padding:16px}}
</style>
