<script lang="ts" setup>
import type { BuyerAddress } from '#/api/marketplace-models';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import {
  Alert,
  Button,
  Drawer,
  Empty,
  Form,
  Input,
  message,
  Popconfirm,
  Space,
  Spin,
  Switch,
  Tag,
} from 'ant-design-vue';

import { buyerApi } from '#/api/marketplace';

const route = useRoute();
const router = useRouter();

const addresses = ref<BuyerAddress[]>([]);
const loading = ref(false);
const saving = ref(false);
const actionLoading = ref(false);
const error = ref('');
const drawerOpen = ref(false);
const editingId = ref<number>();

const form = reactive({
  recipientName: '',
  mobile: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  defaultAddress: false,
});

const canReturnToCheckout = computed(() => {
  const redirect = route.query.redirect;
  return typeof redirect === 'string' && redirect.startsWith('/buyer/checkout');
});

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

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const list = (await buyerApi.addresses()) as BuyerAddress[];
    addresses.value = list || [];
  } catch (reason) {
    error.value = formatError(reason, '收货地址加载失败，请重试');
  } finally {
    loading.value = false;
  }
}

function openForm(address?: BuyerAddress) {
  editingId.value = address?.id;
  Object.assign(form, {
    recipientName: address?.recipientName || '',
    mobile: address?.mobile || '',
    province: address?.province || '',
    city: address?.city || '',
    district: address?.district || '',
    detail: address?.detail || '',
    defaultAddress: address?.defaultAddress || false,
  });
  drawerOpen.value = true;
}

async function save() {
  if (
    Object.entries(form).some(
      ([key, value]) => key !== 'defaultAddress' && !String(value).trim(),
    )
  ) {
    message.error('请填写完整的收货地址');
    return;
  }
  if (!/^1\d{10}$/.test(form.mobile.trim())) {
    message.error('请输入 11 位手机号');
    return;
  }

  saving.value = true;
  try {
    await buyerApi.saveAddress({ ...form }, editingId.value);
    drawerOpen.value = false;
    message.success(editingId.value ? '收货地址更新成功' : '收货地址新增成功');
    await load();
  } catch (reason) {
    message.error(formatError(reason, '保存地址失败，请重试'));
  } finally {
    saving.value = false;
  }
}

async function setDefault(id: number) {
  actionLoading.value = true;
  try {
    await buyerApi.defaultAddress(id);
    message.success('已设为默认地址');
    await load();
  } catch (reason) {
    message.error(formatError(reason, '设置默认地址失败，请重试'));
  } finally {
    actionLoading.value = false;
  }
}

async function remove(id: number) {
  actionLoading.value = true;
  try {
    await buyerApi.deleteAddress(id);
    message.success('地址删除成功');
    await load();
  } catch (reason) {
    message.error(formatError(reason, '删除地址失败，请重试'));
  } finally {
    actionLoading.value = false;
  }
}

function returnToCheckout() {
  const redirect = route.query.redirect;
  if (typeof redirect === 'string' && redirect.startsWith('/buyer/checkout')) {
    router.push(redirect);
  }
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <h1 class="market-heading">收货地址</h1>
        <p class="market-subtitle">订单会保存当前收货信息快照；默认地址可用于预选结算。</p>
      </div>
      <Space wrap>
        <Button v-if="canReturnToCheckout" type="primary" @click="returnToCheckout">返回结算</Button>
        <Button :type="canReturnToCheckout ? 'default' : 'primary'" @click="openForm()">新增地址</Button>
        <Button :loading="loading" @click="load">刷新</Button>
      </Space>
    </header>

    <Alert
      v-if="error && addresses.length"
      :message="error"
      type="error"
      show-icon
      closable
      class="notice-alert"
      @close="error = ''"
    >
      <template #action>
        <Button size="small" danger ghost :loading="loading" @click="load">重试</Button>
      </template>
    </Alert>

    <section class="market-panel" :aria-busy="loading" aria-labelledby="address-list-title">
      <div class="panel-header">
        <h2 id="address-list-title" class="market-panel-title">地址簿</h2>
        <span v-if="addresses.length" class="address-count">共 {{ addresses.length }} 处地址</span>
      </div>

      <div v-if="loading && !addresses.length" class="loading-wrap">
        <Spin tip="正在加载地址列表..." />
      </div>

      <Alert
        v-else-if="error && !addresses.length"
        :message="error"
        type="error"
        show-icon
        class="address-error-alert"
      >
        <template #action>
          <Button size="small" danger ghost :loading="loading" @click="load">重试</Button>
        </template>
      </Alert>

      <div v-else-if="addresses.length" class="address-grid">
        <article
          v-for="address in addresses"
          :key="address.id"
          :class="['address-card', { 'is-default': address.defaultAddress }]"
        >
          <header class="card-head">
            <div class="user-meta">
              <strong class="user-name">{{ address.recipientName }}</strong>
              <span class="user-phone">{{ address.mobile }}</span>
            </div>
            <Tag v-if="address.defaultAddress" color="blue" class="default-tag">默认地址</Tag>
          </header>

          <div class="address-body">
            <div class="region-text">
              {{ address.province }} {{ address.city }} {{ address.district }}
            </div>
            <div class="detail-text">{{ address.detail }}</div>
          </div>

          <footer class="card-footer">
            <Space>
              <Button type="link" class="card-btn" @click="openForm(address)">编辑</Button>
              <Button
                v-if="!address.defaultAddress"
                :disabled="actionLoading"
                type="link"
                class="card-btn"
                @click="setDefault(address.id)"
              >
                设为默认
              </Button>
              <Popconfirm
                title="确认删除该地址？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="remove(address.id)"
              >
                <Button danger :disabled="actionLoading" type="link" class="card-btn">
                  删除
                </Button>
              </Popconfirm>
            </Space>
          </footer>
        </article>
      </div>

      <div v-else class="empty-wrap">
        <Empty description="暂无收货地址，请先新增地址">
          <Button type="primary" @click="openForm()">新增地址</Button>
        </Empty>
      </div>
    </section>

    <Drawer
      :open="drawerOpen"
      :title="editingId ? '编辑收货地址' : '新增收货地址'"
      width="min(480px, 100vw)"
      @close="drawerOpen = false"
    >
      <Form :model="form" layout="vertical" @finish="save">
        <div class="form-row-2">
          <Form.Item label="收货人" name="recipientName" :rules="[{ required: true, message: '请填写收货人' }]">
            <Input v-model:value="form.recipientName" placeholder="收货人姓名" />
          </Form.Item>
          <Form.Item label="手机号" name="mobile" :rules="[{ required: true, message: '请填写手机号' }]">
            <Input v-model:value="form.mobile" :maxlength="11" placeholder="11 位手机号" />
          </Form.Item>
        </div>

        <div class="form-row-3">
          <Form.Item label="省份" name="province" :rules="[{ required: true, message: '请填写省' }]">
            <Input v-model:value="form.province" placeholder="省份" />
          </Form.Item>
          <Form.Item label="城市" name="city" :rules="[{ required: true, message: '请填写市' }]">
            <Input v-model:value="form.city" placeholder="城市" />
          </Form.Item>
          <Form.Item label="区 / 县" name="district" :rules="[{ required: true, message: '请填写区/县' }]">
            <Input v-model:value="form.district" placeholder="区 / 县" />
          </Form.Item>
        </div>

        <Form.Item label="详细地址" name="detail" :rules="[{ required: true, message: '请填写详细地址' }]">
          <Input.TextArea v-model:value="form.detail" :rows="3" placeholder="街道、小区、楼栋及门牌号" />
        </Form.Item>

        <Form.Item label="设为默认地址">
          <Switch v-model:checked="form.defaultAddress" />
        </Form.Item>

        <div class="form-actions">
          <Button :loading="saving" html-type="submit" type="primary">保存地址</Button>
          <Button @click="drawerOpen = false">取消</Button>
        </div>
      </Form>
    </Drawer>
  </main>
</template>

<style scoped src="./buyer-addresses.css"></style>
