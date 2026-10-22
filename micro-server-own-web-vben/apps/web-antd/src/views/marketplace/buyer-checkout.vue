<script lang="ts" setup>
import type { BuyerCoupon, CheckoutQuote } from '#/api/marketplace';
import type { BuyerAddress } from '#/api/marketplace-models';

import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Alert, Button, Empty, Radio, Spin } from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';
import { preferredAddressId } from '#/api/marketplace-models';

const route = useRoute();
const router = useRouter();
const addresses = ref<BuyerAddress[]>([]);
const addressId = ref<number>();
const addressLoading = ref(false);
const addressError = ref('');
const coupons = ref<BuyerCoupon[]>([]);
const couponNo = ref('');
const couponLoading = ref(false);
const couponError = ref('');
const quote = ref<CheckoutQuote>();
const loading = ref(false);
const quoteLoading = ref(false);
const quoteError = ref('');
const submitError = ref('');

const merchantQuotes = computed(() => {
  const current = quote.value;
  return Object.entries(current?.merchantTotals || {}).map(([merchantId, amount]) => ({
    amount,
    discount: current?.merchantDiscounts?.[merchantId] ?? 0,
    freight: current?.merchantFreights?.[merchantId] ?? 0,
    merchantId,
  }));
});

const freightTotal = computed(() =>
  merchantQuotes.value.reduce((sum, item) => sum + Number(item.freight || 0), 0),
);

const itemIds = computed(() => [
  ...new Set(
    String(route.query.items || '')
      .split(',')
      .map(Number)
      .filter((id) => Number.isSafeInteger(id) && id > 0),
  ),
]);

const availableCoupons = computed(() =>
  coupons.value.filter(
    (coupon) =>
      coupon.status === 'AVAILABLE' &&
      new Date(coupon.expiresAt).getTime() >= Date.now(),
  ),
);

const selectedCoupon = computed(() =>
  availableCoupons.value.find((coupon) => coupon.couponNo === couponNo.value),
);

const command = () => ({
  addressId: addressId.value,
  cartItemIds: itemIds.value,
  ...(selectedCoupon.value
    ? {
        coupons: [
          {
            couponNo: selectedCoupon.value.couponNo,
            ...(selectedCoupon.value.merchantId
              ? { merchantId: selectedCoupon.value.merchantId }
              : {}),
          },
        ],
      }
    : {}),
});

function money(value: number | string | undefined) {
  const amount = Number(value);
  return Number.isFinite(amount) ? `¥${amount.toFixed(2)}` : '—';
}

// Keep the key after a failed response; only changing the checkout command starts a new attempt.
const orderAttemptKey = ref(crypto.randomUUID());
const orderAttempt = computed(() => ({ data: command(), key: orderAttemptKey.value }));
let quoteRevision = 0;

function isRawError(msg: string): boolean {
  if (!msg) return true;
  if (/^request failed with status code/i.test(msg)) return true;
  if (/network\s*error/i.test(msg)) return true;
  if (/timeout of \d+ms exceeded/i.test(msg)) return true;
  if (/axioserror/i.test(msg)) return true;
  if (/^(bad gateway|gateway timeout|internal server error|service unavailable)$/i.test(msg)) return true;
  if (/^(econnrefused|etimedout|enotfound|err_)/i.test(msg)) return true;
  return false;
}

function cleanFallback(fallback: string): string {
  return fallback.replace(/，?请(稍后)?重试$/, '');
}

function formatError(reason: unknown, fallback: string): string {
  if (!reason) return fallback;
  const err = reason as any;
  const data = err?.response?.data;
  const serverMsg = typeof data === 'string' ? '' : (data?.message || data?.msg || data?.error);
  if (typeof serverMsg === 'string' && serverMsg.trim() && !isRawError(serverMsg.trim())) {
    return serverMsg.trim();
  }
  const status = err?.response?.status;
  const rawMsg = String(err?.message || (reason instanceof Error ? reason.message : '')).trim();
  const base = cleanFallback(fallback);
  if (status === 502 || status === 504 || /status code 50[24]/i.test(rawMsg)) {
    return `${base}，网关响应超时或异常，请稍后重试`;
  }
  if ((typeof status === 'number' && status >= 500) || /status code 5\d\d/i.test(rawMsg)) {
    return `${base}，服务暂时不可用，请稍后重试`;
  }
  if (status === 404 || /status code 404/i.test(rawMsg)) {
    return `${base}，请求的资源不存在`;
  }
  if (status === 403 || /status code 403/i.test(rawMsg)) {
    return `${base}，暂无操作权限`;
  }
  if (status === 401 || /status code 401/i.test(rawMsg)) {
    return '登录状态已失效，请重新登录';
  }
  if (/network\s*error/i.test(rawMsg)) {
    return `${base}，网络连接异常，请检查网络后重试`;
  }
  if (/timeout/i.test(rawMsg)) {
    return `${base}，请求超时，请稍后重试`;
  }
  if (rawMsg && !isRawError(rawMsg)) {
    return rawMsg;
  }
  return fallback;
}

function calculateQuote() {
  const revision = ++quoteRevision;
  quote.value = undefined;
  quoteError.value = '';
  submitError.value = '';
  if (!addressId.value || !itemIds.value.length || !!addressError.value) {
    quoteLoading.value = false;
    return;
  }
  quoteLoading.value = true;
  buyerApi
    .quote(command())
    .then((result) => {
      if (revision === quoteRevision) quote.value = result;
    })
    .catch((error: any) => {
      if (revision === quoteRevision) {
        quoteError.value = formatError(error, '结算试算失败');
      }
    })
    .finally(() => {
      if (revision === quoteRevision) quoteLoading.value = false;
    });
}

function retryQuote() {
  calculateQuote();
}

watch([addressId, itemIds, couponNo], () => {
  orderAttemptKey.value = crypto.randomUUID();
  if (itemIds.value.length > 0) {
    if (!addresses.value.length && !addressLoading.value) {
      void loadAddresses();
    }
    if (!coupons.value.length && !couponLoading.value) {
      void loadCoupons();
    }
    calculateQuote();
  } else {
    quote.value = undefined;
    quoteError.value = '';
    submitError.value = '';
  }
});

async function loadAddresses() {
  if (!itemIds.value.length) return;
  addressLoading.value = true;
  addressError.value = '';
  try {
    addresses.value = (await buyerApi.addresses()) as BuyerAddress[];
    if (!addressId.value || !addresses.value.some((item) => item.id === addressId.value)) {
      addressId.value = preferredAddressId(addresses.value);
    } else {
      calculateQuote();
    }
  } catch (error: any) {
    addresses.value = [];
    addressId.value = undefined;
    quote.value = undefined;
    quoteError.value = '';
    addressError.value = formatError(error, '收货地址加载失败');
  } finally {
    addressLoading.value = false;
  }
}

async function loadCoupons() {
  if (!itemIds.value.length) return;
  couponLoading.value = true;
  couponError.value = '';
  try {
    coupons.value = await buyerApi.coupons();
  } catch (error: any) {
    couponError.value = formatError(error, '券包加载失败');
  } finally {
    couponLoading.value = false;
  }
}

async function submit() {
  if (
    loading.value ||
    quoteLoading.value ||
    !quote.value ||
    !addressId.value ||
    !itemIds.value.length ||
    !!addressError.value
  )
    return;
  loading.value = true;
  submitError.value = '';
  try {
    await buyerApi.createOrder(orderAttempt.value.data, orderAttempt.value.key);
    router.replace('/buyer/orders');
  } catch (error: any) {
    submitError.value = formatError(error, '提交结果未确认，请在当前页面重试');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (itemIds.value.length > 0) {
    void loadAddresses();
    void loadCoupons();
  }
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">确认结算</h1>
        <p class="market-subtitle">
          {{ itemIds.length ? '核对收货地址与优惠券，确认订单金额明细后提交。' : '请先在购物车中勾选需要结算的商品。' }}
        </p>
      </div>
    </header>

    <section v-if="!itemIds.length" class="market-panel empty-selection-panel" aria-labelledby="empty-checkout-title">
      <div class="empty-selection-content">
        <div class="empty-icon-wrap" aria-hidden="true">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
          </svg>
        </div>
        <div class="empty-copy">
          <h2 id="empty-checkout-title" class="empty-title">未选择结算商品</h2>
          <p class="empty-desc">请先在购物车中勾选需要结算的商品，确认规格与数量后发起结算。</p>
        </div>
        <Button type="primary" @click="router.push('/buyer/cart')">
          前往购物车
        </Button>
      </div>
    </section>

    <Spin v-else :spinning="loading || quoteLoading || addressLoading">
      <div class="checkout-layout">
        <div class="checkout-options">
          <section class="market-panel" aria-labelledby="checkout-address-title">
            <div class="section-heading">
              <h2 id="checkout-address-title" class="market-panel-title">收货地址</h2>
              <Button @click="router.push({ path: '/buyer/addresses', query: { redirect: route.fullPath } })">
                {{ addresses.length ? '管理地址' : '新增地址' }}
              </Button>
            </div>
            <Alert v-if="addressError" :message="addressError" show-icon type="error" class="panel-alert">
              <template #action>
                <Button size="small" danger ghost :loading="addressLoading" @click="loadAddresses">
                  重试
                </Button>
              </template>
            </Alert>
            <Radio.Group v-if="addresses.length" v-model:value="addressId" :disabled="loading || quoteLoading" class="addresses">
              <Radio v-for="address in addresses" :key="address.id" :value="address.id">
                <span class="address-recipient">{{ address.recipientName }} <span>{{ address.mobile }}</span></span>
                <span class="address-detail">{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</span>
              </Radio>
            </Radio.Group>
            <Empty v-else-if="!addressLoading && !addressError" description="暂无收货地址，请先新增地址" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
          </section>

          <section class="market-panel" aria-labelledby="checkout-coupon-title">
            <div class="section-heading">
              <h2 id="checkout-coupon-title" class="market-panel-title">选择优惠券</h2>
              <Button @click="router.push({ path: '/buyer/coupons', query: { redirect: route.fullPath } })">
                查看券包
              </Button>
            </div>
            <Alert v-if="couponError" :message="couponError" description="仍可不使用优惠券结算。" show-icon type="warning" class="panel-alert">
              <template #action>
                <Button size="small" danger ghost :loading="couponLoading" @click="loadCoupons">
                  重试
                </Button>
              </template>
            </Alert>
            <Radio.Group v-model:value="couponNo" :disabled="loading || quoteLoading" class="addresses">
              <Radio value="">不使用优惠券</Radio>
              <Radio v-for="coupon in availableCoupons" :key="coupon.couponNo" :value="coupon.couponNo">
                <span class="coupon-name">{{ coupon.couponType === 'MALL' ? '平台券' : '店铺券' }} · 减 {{ money(coupon.discountAmount) }}</span>
                <span class="coupon-rule">满 {{ money(coupon.minimumAmount) }} 可用 · {{ coupon.couponNo }}</span>
              </Radio>
            </Radio.Group>
          </section>
        </div>

        <aside class="checkout-summary">
          <section class="market-panel" aria-labelledby="checkout-quote-title">
            <h2 id="checkout-quote-title" class="market-panel-title">结算明细</h2>
            <Alert v-if="quoteError" :message="quoteError" show-icon type="error" class="panel-alert">
              <template #action>
                <Button size="small" danger ghost :loading="quoteLoading" @click="retryQuote">
                  重新试算
                </Button>
              </template>
            </Alert>
            <template v-else-if="quote">
              <div class="quote-lines">
                <div class="quote-line"><span>商品金额</span><strong>{{ money(quote.totalAmount) }}</strong></div>
                <div class="quote-line"><span>优惠抵扣</span><strong class="discount">−{{ money(quote.discountAmount) }}</strong></div>
                <div class="quote-line"><span>运费</span><strong>{{ money(freightTotal) }}</strong></div>
                <div class="quote-line quote-payable"><span>应付总额</span><strong>{{ money(quote.payableAmount) }}</strong></div>
              </div>
              <div v-if="merchantQuotes.length" class="merchant-breakdown">
                <h3>按商家拆分</h3>
                <div v-for="item in merchantQuotes" :key="item.merchantId" class="merchant-quote">
                  <span>商家 #{{ item.merchantId }}</span>
                  <span>商品 {{ money(item.amount) }} · 优惠 {{ money(item.discount) }} · 运费 {{ money(item.freight) }}</span>
                </div>
              </div>
            </template>
            <p v-else class="market-note">请选择有效收货地址，系统将自动发起金额试算。</p>
            <p class="quote-footnote">应付总额由结算服务试算确认。</p>
          </section>

          <section class="market-panel submit-panel" aria-label="提交订单">
            <Alert v-if="submitError" :message="submitError" description="可在当前页面重试；未修改结算内容时不会重复创建订单。" show-icon type="error" class="panel-alert" />
            <div class="checkout-actions">
              <Button
                :disabled="!addressId || !itemIds.length || !quote || quoteLoading || loading || !addresses.length || !!addressError"
                :loading="loading"
                size="large"
                type="primary"
                class="btn-submit"
                @click="submit"
              >
                提交订单
              </Button>
              <Button size="large" class="btn-back" @click="router.push('/buyer/cart')">
                返回购物车
              </Button>
            </div>
          </section>
        </aside>
      </div>
    </Spin>
  </main>
</template>

<style scoped src="./buyer-checkout.css"></style>
