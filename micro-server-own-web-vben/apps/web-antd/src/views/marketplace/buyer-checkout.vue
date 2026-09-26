<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Button, Card, Empty, Radio, Space, Spin, Tag } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';
import { preferredAddressId } from '#/api/marketplace-models';
import type { BuyerAddress } from '#/api/marketplace-models';


const route = useRoute();
const router = useRouter();
const addresses = ref<BuyerAddress[]>([]);
const addressId = ref<number>();
const quote = ref<Record<string, any>>();
const loading = ref(false);
const itemIds = computed(() => String(route.query.items || '').split(',').filter(Boolean).map(Number));
const command = () => ({ addressId: addressId.value, cartItemIds: itemIds.value });

async function load() {
  loading.value = true;
  try {
    addresses.value = await buyerApi.addresses() as BuyerAddress[];
    addressId.value = preferredAddressId(addresses.value);
    if (addressId.value && itemIds.value.length) quote.value = await buyerApi.quote(command()) as Record<string, any>;
  } finally { loading.value = false; }
}

async function submit() {
  if (!addressId.value) return;
  loading.value = true;
  try {
    await buyerApi.createOrder(command());
    router.replace('/buyer/orders');
  } finally { loading.value = false; }
}

onMounted(load);
</script>

<template>
  <main class="checkout-page">
    <Card :bordered="false"><Tag color="cyan">买家中心</Tag><h1>确认结算</h1><p>订单金额、优惠和库存将以服务端试算结果为准。</p></Card>
    <Spin :spinning="loading">
      <Card :bordered="false" title="收货地址">
        <Radio.Group v-model:value="addressId" class="addresses">
          <Radio v-for="address in addresses" :key="address.id" :value="address.id">
            {{ address.recipientName }} · {{ address.mobile }} · {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}
          </Radio>
        </Radio.Group>
        <Empty v-if="!addresses.length" description="暂无收货地址，请先新增地址" />
        <Button class="address-action" @click="router.push({ path: '/buyer/addresses', query: { redirect: route.fullPath } })">{{ addresses.length ? '管理收货地址' : '新增收货地址' }}</Button>
      </Card>
      <Card :bordered="false" title="结算试算">
        <pre v-if="quote" class="quote">{{ JSON.stringify(quote, null, 2) }}</pre>
        <p v-else>请选择购物车商品和收货地址后重新进入结算。</p>
      </Card>
      <Card :bordered="false"><Space><Button @click="router.back()">返回购物车</Button><Button :disabled="!addressId || !itemIds.length" type="primary" @click="submit">提交订单</Button></Space></Card>
    </Spin>
  </main>
</template>

<style scoped>
.checkout-page { display: grid; gap: 16px; padding: 24px; } h1 { font-size: 24px; margin: 10px 0 6px; } p { color: #64748b; margin: 0; }
.addresses { display: grid; gap: 14px; } .quote { background: #f8fafc; border-radius: 8px; margin: 0; max-height: 320px; overflow: auto; padding: 16px; white-space: pre-wrap; }
.address-action { margin-top: 16px; }
@media (max-width: 640px) { .checkout-page { padding: 16px; } }
</style>
