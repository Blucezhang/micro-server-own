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

function humanizeError(reason: unknown, fallback: string): string {
  if (!reason) return fallback;
  const msg = reason instanceof Error ? reason.message : String(reason);
  if (/502|Bad Gateway/i.test(msg)) {
    return '服务网关暂时不可用，请稍后重试';
  }
  if (/504|Gateway Timeout|timeout/i.test(msg)) {
    return '服务响应超时，请重试';
  }
  if (/500|Internal Server Error/i.test(msg)) {
    return '服务端处理异常，请稍后重试';
  }
  if (msg.includes('Request failed with status code')) {
    return '服务请求失败，请稍后重试';
  }
  return msg || fallback;
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const list = (await buyerApi.addresses()) as BuyerAddress[];
    addresses.value = list || [];
  } catch (reason) {
    error.value = humanizeError(reason, '收货地址加载失败，请重试');
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
    message.error(humanizeError(reason, '保存地址失败，请重试'));
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
    message.error(humanizeError(reason, '设置默认地址失败，请重试'));
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
    message.error(humanizeError(reason, '删除地址失败，请重试'));
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
        <span class="market-overline">BUYER · ADDRESS</span>
        <h1 class="market-heading">收货地址</h1>
        <p class="market-subtitle">订单会保存当前收货信息快照；默认地址可用于预选结算。</p>
      </div>
      <Space wrap>
        <Button v-if="canReturnToCheckout" @click="returnToCheckout">返回结算</Button>
        <Button type="primary" @click="openForm()">新增地址</Button>
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
    />

    <section class="market-panel" :aria-busy="loading" aria-labelledby="address-list-title">
      <div class="panel-header">
        <h2 id="address-list-title" class="market-panel-title">地址簿</h2>
        <span v-if="addresses.length" class="market-note">共 {{ addresses.length }} 处地址</span>
      </div>

      <div v-if="loading && !addresses.length" class="loading-wrap">
        <Spin tip="正在加载地址列表..." />
      </div>

      <div v-else-if="error && !addresses.length" class="error-wrap">
        <Empty description="地址服务暂不可用，未能加载收货地址">
          <Button :loading="loading" type="primary" @click="load">重新加载</Button>
        </Empty>
      </div>

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
            <Tag v-if="address.defaultAddress" color="blue">默认地址</Tag>
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

        <Space class="form-actions">
          <Button :loading="saving" html-type="submit" type="primary">保存地址</Button>
          <Button @click="drawerOpen = false">取消</Button>
        </Space>
      </Form>
    </Drawer>
  </main>
</template>

<style scoped>
.notice-alert {
  margin-bottom: 16px;
}

.panel-header {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.loading-wrap,
.empty-wrap,
.error-wrap {
  padding: 36px 0;
  text-align: center;
}

.address-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.address-card {
  border: 1px solid var(--market-line);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.address-card.is-default {
  border-color: hsl(var(--primary));
  box-shadow: 0 0 0 1px hsl(var(--primary) / 0.2);
}

.card-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.user-meta {
  align-items: baseline;
  display: flex;
  gap: 8px;
}

.user-name {
  color: var(--market-text);
  font-size: 15px;
  font-weight: 600;
}

.user-phone {
  color: var(--market-muted);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.address-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 14px;
}

.region-text {
  color: var(--market-muted);
  font-size: 13px;
}

.detail-text {
  color: var(--market-text);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.card-footer {
  border-top: 1px dashed var(--market-line);
  margin-top: auto;
  padding-top: 10px;
}

.card-btn {
  padding: 0 4px;
}

.form-row-2 {
  display: grid;
  gap: 12px;
  grid-template-columns: 1fr 1fr;
}

.form-row-3 {
  display: grid;
  gap: 8px;
  grid-template-columns: 1fr 1fr 1fr;
}

.form-actions {
  margin-top: 8px;
}

@media (max-width: 560px) {
  .form-row-2,
  .form-row-3 {
    grid-template-columns: 1fr;
  }
}
</style>
