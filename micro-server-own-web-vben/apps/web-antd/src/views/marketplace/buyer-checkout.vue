<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Alert, Button, Card, Empty, Radio, Space, Spin, Tag } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';
import type { BuyerCoupon } from '#/api/marketplace';
import { preferredAddressId } from '#/api/marketplace-models';
import type { BuyerAddress } from '#/api/marketplace-models';


const route = useRoute();
const router = useRouter();
const addresses = ref<BuyerAddress[]>([]);
const addressId = ref<number>();
const coupons = ref<BuyerCoupon[]>([]);
const couponNo = ref('');
const couponError = ref('');
const quote = ref<Record<string, any>>();
const loading = ref(false);
const quoteLoading = ref(false);
const quoteError = ref('');
const submitError = ref('');
const itemIds = computed(() => [...new Set(String(route.query.items || '').split(',').map(Number).filter((id) => Number.isSafeInteger(id) && id > 0))]);
const availableCoupons = computed(() => coupons.value.filter((coupon) =>
  coupon.status === 'AVAILABLE' && new Date(coupon.expiresAt).getTime() >= Date.now()));
const selectedCoupon = computed(() => availableCoupons.value.find((coupon) => coupon.couponNo === couponNo.value));
const command = () => ({
  addressId: addressId.value,
  cartItemIds: itemIds.value,
  ...(selectedCoupon.value ? { coupons: [{
    couponNo: selectedCoupon.value.couponNo,
    ...(selectedCoupon.value.merchantId ? { merchantId: selectedCoupon.value.merchantId } : {}),
  }] } : {}),
});
// Keep the key after a failed response; only changing the checkout command starts a new attempt.
const orderAttempt = computed(() => ({ data: command(), key: crypto.randomUUID() }));
let quoteRevision = 0;

watch([addressId, itemIds, couponNo], ([selectedAddressId, selectedItems]) => {
  const revision = ++quoteRevision;
  quote.value = undefined;
  quoteError.value = '';
  submitError.value = '';
  if (!selectedAddressId || !selectedItems.length) {
    quoteLoading.value = false;
    return;
  }
  quoteLoading.value = true;
  buyerApi.quote(command())
    .then((result) => { if (revision === quoteRevision) quote.value = result as Record<string, any>; })
    .catch((error: any) => {
      if (revision === quoteRevision) quoteError.value = error?.response?.data?.message || error?.message || '结算试算失败，请重试';
    })
    .finally(() => { if (revision === quoteRevision) quoteLoading.value = false; });
});

async function load() {
  loading.value = true;
  try {
    addresses.value = await buyerApi.addresses() as BuyerAddress[];
    addressId.value = preferredAddressId(addresses.value);
  } catch (error: any) {
    quoteError.value = error?.response?.data?.message || error?.message || '收货地址加载失败，请重试';
  } finally { loading.value = false; }
}

async function loadCoupons() {
  couponError.value = '';
  try {
    coupons.value = await buyerApi.coupons();
  } catch (error: any) {
    couponError.value = error?.response?.data?.message || error?.message || '券包加载失败，仍可不使用优惠券结算';
  }
}

async function submit() {
  if (loading.value || quoteLoading.value || !quote.value || !addressId.value || !itemIds.value.length) return;
  loading.value = true;
  submitError.value = '';
  try {
    await buyerApi.createOrder(orderAttempt.value.data, orderAttempt.value.key);
    router.replace('/buyer/orders');
  } catch (error: any) {
    submitError.value = error?.response?.data?.message || error?.message || '提交结果未确认，请在当前页面重试';
  } finally { loading.value = false; }
}

onMounted(() => { void load(); void loadCoupons(); });
</script>

<template>
  <main class="checkout-page">
    <Card :bordered="false"><Tag color="cyan">买家中心</Tag><h1>确认结算</h1><p>订单金额、优惠和库存将以服务端试算结果为准。</p></Card>
    <Spin :spinning="loading || quoteLoading">
      <Card :bordered="false" title="收货地址">
        <Radio.Group v-model:value="addressId" :disabled="loading || quoteLoading" class="addresses">
          <Radio v-for="address in addresses" :key="address.id" :value="address.id">
            {{ address.recipientName }} · {{ address.mobile }} · {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}
          </Radio>
        </Radio.Group>
        <Empty v-if="!addresses.length" description="暂无收货地址，请先新增地址" />
        <Button class="address-action" @click="router.push({ path: '/buyer/addresses', query: { redirect: route.fullPath } })">{{ addresses.length ? '管理收货地址' : '新增收货地址' }}</Button>
      </Card>
      <Card :bordered="false" title="选择优惠券">
        <Alert v-if="couponError" :message="couponError" show-icon type="warning" />
        <Radio.Group v-model:value="couponNo" :disabled="loading || quoteLoading" class="addresses">
          <Radio value="">不使用优惠券</Radio>
          <Radio v-for="coupon in availableCoupons" :key="coupon.couponNo" :value="coupon.couponNo">
            {{ coupon.couponType === 'MALL' ? '平台券' : '店铺券' }} · 减 {{ coupon.discountAmount }} ·
            满 {{ coupon.minimumAmount }} 可用 · {{ coupon.couponNo }}
          </Radio>
        </Radio.Group>
        <Button class="address-action" @click="router.push({ path: '/buyer/coupons', query: { redirect: route.fullPath } })">
          查看券包或领券
        </Button>
      </Card>
      <Card :bordered="false" title="结算试算">
        <Alert v-if="quoteError" :message="quoteError" show-icon type="error" />
        <pre v-if="quote" class="quote">{{ JSON.stringify(quote, null, 2) }}</pre>
        <p v-else-if="!quoteError">请选择购物车商品和收货地址后重新进入结算。</p>
      </Card>
      <Card :bordered="false"><Alert v-if="submitError" :message="submitError" description="可在当前页面重试；相同结算内容会复用幂等键。" show-icon type="error" /><Space><Button @click="router.back()">返回购物车</Button><Button :disabled="!addressId || !itemIds.length || !quote || quoteLoading" :loading="loading" type="primary" @click="submit">提交订单</Button></Space></Card>
    </Spin>
  </main>
</template>

<style scoped>
.checkout-page { display: grid; grid-template-columns: minmax(0, 1fr); gap: 16px; padding: 24px; } h1 { font-size: 24px; margin: 10px 0 6px; } p { color: #64748b; margin: 0; }
.addresses { display: grid; gap: 14px; } .quote { background: hsl(var(--muted)); color: hsl(var(--foreground)); border-radius: 8px; margin: 0; max-height: 320px; overflow: auto; overflow-wrap: anywhere; padding: 16px; white-space: pre-wrap; }
.address-action { margin-top: 16px; }
@media (max-width: 640px) { .checkout-page { padding: 16px; } }
</style>
