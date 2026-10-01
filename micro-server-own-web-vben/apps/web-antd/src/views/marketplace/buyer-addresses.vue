<script lang="ts" setup>
import type { BuyerAddress } from '#/api/marketplace-models';

import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Button, Drawer, Form, Input, message, Popconfirm, Space, Switch, Tag } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';

const route = useRoute();
const router = useRouter();
const addresses = ref<BuyerAddress[]>([]);
const loading = ref(false);
const drawerOpen = ref(false);
const editingId = ref<number>();
const form = reactive({ recipientName: '', mobile: '', province: '', city: '', district: '', detail: '', defaultAddress: false });

async function load() {
  loading.value = true;
  try { addresses.value = await buyerApi.addresses() as BuyerAddress[]; }
  finally { loading.value = false; }
}

function openForm(address?: BuyerAddress) {
  editingId.value = address?.id;
  Object.assign(form, {
    recipientName: address?.recipientName || '', mobile: address?.mobile || '',
    province: address?.province || '', city: address?.city || '',
    district: address?.district || '', detail: address?.detail || '',
    defaultAddress: address?.defaultAddress || false,
  });
  drawerOpen.value = true;
}

async function save() {
  if (Object.entries(form).some(([key, value]) => key !== 'defaultAddress' && !String(value).trim())) {
    message.error('请填写完整的收货地址');
    return;
  }
  if (!/^1\d{10}$/.test(form.mobile.trim())) {
    message.error('请输入 11 位手机号');
    return;
  }
  loading.value = true;
  try {
    await buyerApi.saveAddress({ ...form }, editingId.value);
    drawerOpen.value = false;
    await load();
  } finally { loading.value = false; }
}

async function setDefault(id: number) { await buyerApi.defaultAddress(id); await load(); }
async function remove(id: number) { await buyerApi.deleteAddress(id); await load(); }
function returnToCheckout() {
  const redirect = route.query.redirect;
  if (typeof redirect === 'string' && redirect.startsWith('/buyer/checkout')) router.push(redirect);
}
onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">BUYER · ADDRESS</span>
        <h1 class="market-heading">收货地址</h1>
        <p class="market-subtitle">订单会保存当前收货信息快照；默认地址可用于预选结算。</p>
      </div>
      <Space wrap>
        <Button v-if="route.query.redirect" @click="returnToCheckout">返回结算</Button>
        <Button type="primary" @click="openForm()">新增地址</Button>
        <Button :loading="loading" @click="load">刷新</Button>
      </Space>
    </header>
    <section class="market-panel" :aria-busy="loading" aria-labelledby="address-list-title">
      <h2 id="address-list-title" class="market-panel-title">地址簿</h2>
      <div v-if="addresses.length" class="address-grid">
        <article v-for="address in addresses" :key="address.id" class="address-card">
          <div><strong>{{ address.recipientName }}</strong><span>{{ address.mobile }}</span></div>
          <p>{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</p>
          <Tag v-if="address.defaultAddress" color="green">默认地址</Tag>
          <footer>
            <Button type="link" @click="openForm(address)">编辑</Button>
            <Button v-if="!address.defaultAddress" type="link" @click="setDefault(address.id)">设为默认</Button>
            <Popconfirm title="确认删除该地址？" @confirm="remove(address.id)"><Button danger type="link">删除</Button></Popconfirm>
          </footer>
        </article>
      </div>
      <p v-else class="empty">暂无收货地址，请先新增地址。</p>
    </section>
    <Drawer :open="drawerOpen" :title="editingId ? '编辑地址' : '新增地址'" width="480" @close="drawerOpen = false">
      <Form :model="form" layout="vertical" @finish="save">
        <Form.Item label="收货人" name="recipientName" :rules="[{ required: true, message: '请填写收货人' }]"><Input v-model:value="form.recipientName" /></Form.Item>
        <Form.Item label="手机号" name="mobile" :rules="[{ required: true, message: '请填写手机号' }]"><Input v-model:value="form.mobile" /></Form.Item>
        <Form.Item label="省" name="province" :rules="[{ required: true, message: '请填写省' }]"><Input v-model:value="form.province" /></Form.Item>
        <Form.Item label="市" name="city" :rules="[{ required: true, message: '请填写市' }]"><Input v-model:value="form.city" /></Form.Item>
        <Form.Item label="区/县" name="district" :rules="[{ required: true, message: '请填写区/县' }]"><Input v-model:value="form.district" /></Form.Item>
        <Form.Item label="详细地址" name="detail" :rules="[{ required: true, message: '请填写详细地址' }]"><Input v-model:value="form.detail" /></Form.Item>
        <Form.Item label="设为默认地址"><Switch v-model:checked="form.defaultAddress" /></Form.Item>
        <Button :loading="loading" html-type="submit" type="primary">保存地址</Button>
      </Form>
    </Drawer>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.address-grid { display: grid; gap: 16px; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); }
.address-card { border: 1px solid var(--market-line); border-radius: 12px; padding: 18px; }
.address-card span { color: var(--market-muted); margin-left: 12px; }
.address-card p { line-height: 1.7; margin: 14px 0; }
.address-card footer { margin-top: 16px; }
.empty { color: var(--market-muted); padding: 30px 0; }
</style>
