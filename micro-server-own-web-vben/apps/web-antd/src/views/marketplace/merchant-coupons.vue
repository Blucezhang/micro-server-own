<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import { onMounted, reactive, ref } from 'vue';

import { Alert, Button, DatePicker, Form, Input, InputNumber, Space, Table, Tag } from 'ant-design-vue';
import dayjs from 'dayjs';

import { merchantApi } from '#/api/marketplace';
import { merchantCouponRows } from '#/api/marketplace-models';

type CouponRow = {
  availableQuantity: number | string;
  claimEndsAt?: null | number | string;
  claimStartsAt?: null | number | string;
  discountAmount: number | string;
  expiresAt: number | string;
  id: number;
  minimumAmount: number | string;
  name: string;
  status: string;
  totalQuantity: number | string;
};

const loading = ref(false);
const rows = ref<CouponRow[]>([]);
const editingId = ref<number>();
const feedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const form = reactive<{
  claimEndsAt?: Dayjs;
  claimStartsAt?: Dayjs;
  discountAmount?: number;
  expiresAt?: Dayjs;
  minimumAmount?: number;
  name: string;
  totalQuantity?: number;
}>({ name: '' });

const columns = [
  { dataIndex: 'name', key: 'name', title: '券名称' },
  { dataIndex: 'status', key: 'status', title: '状态' },
  { dataIndex: 'availableQuantity', key: 'availableQuantity', title: '剩余数量' },
  { dataIndex: 'minimumAmount', key: 'minimumAmount', title: '使用门槛' },
  { dataIndex: 'discountAmount', key: 'discountAmount', title: '优惠金额' },
  { key: 'expiresAt', title: '有效期至' },
  { key: 'actions', title: '操作' },
];

function message(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

function formatDate(value: CouponRow['expiresAt']) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

function reset() {
  editingId.value = undefined;
  Object.assign(form, {
    claimEndsAt: undefined,
    claimStartsAt: undefined,
    discountAmount: undefined,
    expiresAt: undefined,
    minimumAmount: undefined,
    name: '',
    totalQuantity: undefined,
  });
}

async function load() {
  loading.value = true;
  feedback.value = null;
  try {
    rows.value = merchantCouponRows((await merchantApi.coupons()) as Parameters<typeof merchantCouponRows>[0]) as CouponRow[];
  } catch (error) {
    feedback.value = { text: message(error, '优惠券列表加载失败，请刷新重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

function edit(raw: Record<string, unknown>) {
  const row = raw as CouponRow;
  if (Number(row.availableQuantity) !== Number(row.totalQuantity)) {
    feedback.value = { text: '已有用户领取的优惠券不能编辑，可使用停用操作。', type: 'error' };
    return;
  }
  editingId.value = row.id;
  Object.assign(form, {
    claimEndsAt: row.claimEndsAt == null ? undefined : dayjs(row.claimEndsAt),
    claimStartsAt: row.claimStartsAt == null ? undefined : dayjs(row.claimStartsAt),
    discountAmount: Number(row.discountAmount),
    expiresAt: dayjs(row.expiresAt),
    minimumAmount: Number(row.minimumAmount),
    name: row.name,
    totalQuantity: Number(row.totalQuantity),
  });
}

async function save() {
  if (loading.value) return;
  loading.value = true;
  feedback.value = null;
  try {
    await merchantApi.saveCoupon({
      claimEndsAt: form.claimEndsAt?.valueOf(),
      claimStartsAt: form.claimStartsAt?.valueOf(),
      discountAmount: String(form.discountAmount),
      expiresAt: form.expiresAt?.valueOf(),
      minimumAmount: String(form.minimumAmount),
      name: form.name.trim(),
      totalQuantity: form.totalQuantity,
    }, editingId.value);
    reset();
    feedback.value = { text: '优惠券模板已保存', type: 'success' };
    await load();
  } catch (error) {
    feedback.value = { text: message(error, '保存失败，请稍后重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

async function changeStatus(raw: Record<string, unknown>) {
  const row = raw as CouponRow;
  if (loading.value) return;
  loading.value = true;
  feedback.value = null;
  try {
    await merchantApi.changeCouponStatus(row.id, row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE');
    await load();
  } catch (error) {
    feedback.value = { text: message(error, '状态更新失败，请稍后重试'), type: 'error' };
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · MARKETING</span>
        <h1 class="market-heading">营销优惠</h1>
        <p class="market-subtitle">管理店铺优惠券；被领取的模板不可编辑，但可启用或停用。</p>
      </div>
      <Button :loading="loading" @click="load">刷新列表</Button>
    </header>

    <div class="market-grid">
      <section class="market-panel" aria-labelledby="coupon-form-title">
        <h2 id="coupon-form-title" class="market-panel-title">{{ editingId ? '编辑优惠券' : '创建优惠券' }}</h2>
        <Form :disabled="loading" :model="form" layout="vertical" @finish="save">
          <Form.Item label="券名称" name="name" :rules="[{ required: true, message: '请填写券名称' }]">
            <Input v-model:value="form.name" />
          </Form.Item>
          <div class="form-pair">
            <Form.Item label="使用门槛" name="minimumAmount" :rules="[{ required: true, message: '请填写使用门槛' }]">
              <InputNumber v-model:value="form.minimumAmount" :min="0" class="full" />
            </Form.Item>
            <Form.Item label="优惠金额" name="discountAmount" :rules="[{ required: true, message: '请填写优惠金额' }]">
              <InputNumber v-model:value="form.discountAmount" :min="0.01" class="full" />
            </Form.Item>
          </div>
          <Form.Item label="发放总量" name="totalQuantity" :rules="[{ required: true, message: '请填写发放总量' }]">
            <InputNumber v-model:value="form.totalQuantity" :min="1" class="full" />
          </Form.Item>
          <Form.Item label="领取时间"><DatePicker v-model:value="form.claimStartsAt" show-time class="full" /></Form.Item>
          <Form.Item label="结束领取时间"><DatePicker v-model:value="form.claimEndsAt" show-time class="full" /></Form.Item>
          <Form.Item label="有效期至" name="expiresAt" :rules="[{ required: true, message: '请选择有效期' }]">
            <DatePicker v-model:value="form.expiresAt" show-time class="full" />
          </Form.Item>
          <Alert v-if="feedback" :message="feedback.text" :type="feedback.type" class="notice" show-icon />
          <Space>
            <Button :loading="loading" html-type="submit" type="primary">{{ editingId ? '保存编辑' : '创建优惠券' }}</Button>
            <Button v-if="editingId" :disabled="loading" @click="reset">取消编辑</Button>
          </Space>
        </Form>
      </section>

      <section class="market-panel" aria-labelledby="coupon-list-title">
        <h2 id="coupon-list-title" class="market-panel-title">模板列表</h2>
        <Table :columns="columns" :data-source="rows" :loading="loading" :scroll="{ x: 820 }" row-key="id">
          <template #bodyCell="{ column, record }">
            <Tag v-if="column.key === 'status'" :color="record.status === 'ACTIVE' ? 'green' : 'default'">{{ record.status }}</Tag>
            <template v-else-if="column.key === 'expiresAt'">{{ formatDate(record.expiresAt) }}</template>
            <Space v-else-if="column.key === 'actions'">
              <Button type="link" @click="edit(record)">编辑</Button>
              <Button type="link" @click="changeStatus(record)">{{ record.status === 'ACTIVE' ? '停用' : '启用' }}</Button>
            </Space>
          </template>
        </Table>
      </section>
    </div>
  </main>
</template>

<style src="./marketplace-page.css"></style>
<style scoped>
.form-pair { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.full { width: 100%; }
.notice { margin-bottom: 16px; }
@media (max-width: 640px) { .form-pair { grid-template-columns: 1fr; gap: 0; } }
</style>
