<script lang="ts" setup>
import type { BuyerCoupon } from '#/api/marketplace';
import type { BuyerAddress } from '#/api/marketplace-models';

import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Alert, Button, Descriptions, Empty, Radio, Space, Spin } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';
import { preferredAddressId } from '#/api/marketplace-models';


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
const quoteItems = computed(() => Object.entries(quote.value || {}).map(([label, value]) => ({ label, value: value == null ? '—' : typeof value === 'object' ? JSON.stringify(value) : String(value) })));
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
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">BUYER · CHECKOUT</span>
        <h1 class="market-heading">确认结算</h1>
        <p class="market-subtitle">订单金额、优惠和库存将以服务端试算结果为准。</p>
      </div>
    </header>
    <Spin :spinning="loading || quoteLoading">
      <div class="checkout-panels">
        <section class="market-panel" aria-labelledby="checkout-address-title">
          <h2 id="checkout-address-title" class="market-panel-title">收货地址</h2>
          <Radio.Group v-model:value="addressId" :disabled="loading || quoteLoading" class="addresses">
            <Radio v-for="address in addresses" :key="address.id" :value="address.id">
              {{ address.recipientName }} · {{ address.mobile }} · {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}
            </Radio>
          </Radio.Group>
          <Empty v-if="!addresses.length" description="暂无收货地址，请先新增地址" />
          <Button class="address-action" @click="router.push({ path: '/buyer/addresses', query: { redirect: route.fullPath } })">{{ addresses.length ? '管理收货地址' : '新增收货地址' }}</Button>
        </section>
        <section class="market-panel" aria-labelledby="checkout-coupon-title">
          <h2 id="checkout-coupon-title" class="market-panel-title">选择优惠券</h2>
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
        </section>
        <section class="market-panel" aria-labelledby="checkout-quote-title">
          <h2 id="checkout-quote-title" class="market-panel-title">结算试算</h2>
          <Alert v-if="quoteError" :message="quoteError" show-icon type="error" />
          <Descriptions v-if="quoteItems.length" :column="1" :items="quoteItems" class="quote" bordered size="small" />
          <p v-else-if="!quoteError" class="market-note">请选择购物车商品和收货地址后重新进入结算。</p>
        </section>
        <section class="market-panel">
          <Alert v-if="submitError" :message="submitError" description="可在当前页面重试；相同结算内容会复用幂等键。" show-icon type="error" />
          <Space class="checkout-actions"><Button @click="router.back()">返回购物车</Button><Button :disabled="!addressId || !itemIds.length || !quote || quoteLoading" :loading="loading" type="primary" @click="submit">提交订单</Button></Space>
        </section>
      </div>
    </Spin>
  </main>
</template>

<style scoped>
.checkout-panels { display: grid; gap: 16px; }
.addresses { display: grid; gap: 14px; }
.quote { margin-top: 16px; overflow-wrap: anywhere; }
.address-action { margin-top: 16px; }
.checkout-actions { margin-top: 16px; }
</style>
