<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import { onMounted, reactive, ref } from 'vue';

import { Alert, Button, DatePicker, Empty, Form, Input, InputNumber, Space, Table, Tag } from 'ant-design-vue';
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
type Feedback = { text: string; type: 'error' | 'success' };

const listLoading = ref(false);
const formLoading = ref(false);
const statusLoading = ref(false);
const listError = ref('');
const rows = ref<CouponRow[]>([]);
const editingId = ref<number>();
const formFeedback = ref<Feedback | null>(null);
const actionFeedback = ref<Feedback | null>(null);
const copyFeedback = ref<Feedback | null>(null);
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
  { dataIndex: 'name', key: 'name', title: '券信息', ellipsis: true },
  { align: 'center' as const, dataIndex: 'status', key: 'status', title: '状态', width: 96 },
  { align: 'right' as const, dataIndex: 'discountAmount', key: 'discountAmount', title: '优惠金额', width: 110 },
  { align: 'right' as const, dataIndex: 'minimumAmount', key: 'minimumAmount', title: '使用门槛', width: 110 },
  { align: 'right' as const, dataIndex: 'availableQuantity', key: 'availableQuantity', title: '剩余数量', width: 120 },
  { key: 'expiresAt', title: '有效期至', width: 170 },
  { align: 'center' as const, key: 'actions', title: '操作', width: 130 },
];

const STATUS_CONFIG: Record<string, { color: string; label: string }> = {
  ACTIVE: { color: 'success', label: '已启用' },
  DISABLED: { color: 'default', label: '已停用' },
};

function getStatusDisplay(status: string) {
  return STATUS_CONFIG[status] ?? { color: 'default', label: '未知状态' };
}

function message(error: unknown, fallback: string) {
  if (error && typeof error === 'object') {
    const err = error as Record<string, unknown>;
    const response = err.response as Record<string, unknown> | undefined;
    const resData = response?.data as Record<string, unknown> | undefined;
    const directMsg = (resData?.message || response?.message || err.message) as string | undefined;
    if (typeof directMsg === 'string' && directMsg.trim()) {
      const trimmed = directMsg.trim();
      const isRawTransport = /status code|network error|timeout|failed to fetch|econnrefused|gateway|abort/i.test(trimmed);
      if (!isRawTransport && /[\u4e00-\u9fa5]/.test(trimmed)) {
        return trimmed;
      }
    }
  }
  return fallback;
}

function formatDate(value: CouponRow['expiresAt']) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString('zh-CN');
}

async function copyTemplateId(id: number) {
  copyFeedback.value = null;
  try {
    if (!navigator.clipboard?.writeText) throw new Error('clipboard unavailable');
    await navigator.clipboard.writeText(String(id));
    copyFeedback.value = { text: `模板 ID ${id} 已复制，可发给买家领取`, type: 'success' };
  } catch {
    copyFeedback.value = { text: `复制失败，请手动选择并复制模板 ID：${id}`, type: 'error' };
  }
}

function reset() {
  editingId.value = undefined;
  formFeedback.value = null;
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
  listLoading.value = true;
  listError.value = '';
  try {
    rows.value = merchantCouponRows((await merchantApi.coupons()) as Parameters<typeof merchantCouponRows>[0]) as CouponRow[];
  } catch (error) {
    listError.value = message(error, '优惠券列表加载失败，请重试');
  } finally {
    listLoading.value = false;
  }
}

function edit(raw: Record<string, unknown>) {
  const row = raw as CouponRow;
  if (Number(row.availableQuantity) !== Number(row.totalQuantity)) {
    formFeedback.value = { text: '已有用户领取的优惠券不能编辑，可使用停用操作。', type: 'error' };
    return;
  }
  formFeedback.value = null;
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
  if (formLoading.value) return;
  formLoading.value = true;
  formFeedback.value = null;
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
    formFeedback.value = { text: '优惠券模板已保存', type: 'success' };
    await load();
  } catch (error) {
    formFeedback.value = { text: message(error, '保存失败，请稍后重试'), type: 'error' };
  } finally {
    formLoading.value = false;
  }
}

async function changeStatus(raw: Record<string, unknown>) {
  const row = raw as CouponRow;
  if (statusLoading.value) return;
  statusLoading.value = true;
  actionFeedback.value = null;
  try {
    await merchantApi.changeCouponStatus(row.id, row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE');
    actionFeedback.value = { text: `优惠券「${row.name}」状态已更新`, type: 'success' };
    await load();
  } catch (error) {
    actionFeedback.value = { text: message(error, '状态更新失败，请稍后重试'), type: 'error' };
  } finally {
    statusLoading.value = false;
  }
}

onMounted(() => load());
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div>
        <span class="market-overline">MERCHANT · MARKETING</span>
        <h1 class="market-heading">营销优惠</h1>
        <p class="market-subtitle">管理店铺优惠券；被领取的模板不可编辑，但可启用或停用。</p>
      </div>
      <Button :loading="listLoading" @click="load">刷新列表</Button>
    </header>

    <div class="market-grid">
      <section class="market-panel" aria-labelledby="coupon-form-title">
        <div class="panel-header-row">
          <h2 id="coupon-form-title" class="market-panel-title">{{ editingId ? '编辑优惠券' : '创建优惠券' }}</h2>
          <span v-if="editingId" class="edit-pill">正在编辑 #{{ editingId }}</span>
        </div>
        <Form :disabled="formLoading" :model="form" layout="vertical" @finish="save">
          <Form.Item label="券名称" name="name" :rules="[{ required: true, message: '请填写券名称' }]">
            <Input v-model:value="form.name" placeholder="请输入优惠券名称" />
          </Form.Item>
          <div class="form-pair">
            <Form.Item label="使用门槛" name="minimumAmount" :rules="[{ required: true, message: '请填写使用门槛' }]">
              <InputNumber v-model:value="form.minimumAmount" :min="0" :precision="2" class="full" placeholder="0.00" prefix="¥" />
            </Form.Item>
            <Form.Item label="优惠金额" name="discountAmount" :rules="[{ required: true, message: '请填写优惠金额' }]">
              <InputNumber v-model:value="form.discountAmount" :min="0.01" :precision="2" class="full" placeholder="0.00" prefix="¥" />
            </Form.Item>
          </div>
          <Form.Item label="发放总量" name="totalQuantity" :rules="[{ required: true, message: '请填写发放总量' }]">
            <InputNumber v-model:value="form.totalQuantity" :min="1" :precision="0" class="full" placeholder="请输入发放张数" />
          </Form.Item>
          <div class="form-pair">
            <Form.Item label="领取时间">
              <DatePicker v-model:value="form.claimStartsAt" class="full" placeholder="开始领取时间" show-time />
            </Form.Item>
            <Form.Item label="结束领取时间">
              <DatePicker v-model:value="form.claimEndsAt" class="full" placeholder="结束领取时间" show-time />
            </Form.Item>
          </div>
          <Form.Item label="有效期至" name="expiresAt" :rules="[{ required: true, message: '请选择有效期' }]">
            <DatePicker v-model:value="form.expiresAt" class="full" placeholder="选择券有效期截止时间" show-time />
          </Form.Item>
          <p class="market-note form-hint">
            提示：优惠券一经用户领取，金额与总量无法修改；如需调整请停用原券并重新创建。
          </p>
          <Alert v-if="formFeedback" :message="formFeedback.text" :type="formFeedback.type" class="notice" closable show-icon @close="formFeedback = null" />
          <Space>
            <Button :loading="formLoading" html-type="submit" type="primary">{{ editingId ? '保存编辑' : '创建优惠券' }}</Button>
            <Button v-if="editingId" :disabled="formLoading" @click="reset">取消编辑</Button>
          </Space>
        </Form>
      </section>

      <section class="market-panel" aria-labelledby="coupon-list-title">
        <div class="panel-header-row">
          <h2 id="coupon-list-title" class="market-panel-title">模板列表</h2>
          <Tag v-if="!listError" class="list-count-tag">{{ rows.length }} 个模板</Tag>
          <Tag v-else color="error" class="list-count-tag">加载失败</Tag>
        </div>
        <Alert v-if="actionFeedback" :message="actionFeedback.text" :type="actionFeedback.type" class="notice" closable show-icon @close="actionFeedback = null" />
        <Alert v-if="copyFeedback" :message="copyFeedback.text" :type="copyFeedback.type" class="notice" closable show-icon @close="copyFeedback = null" />

        <div v-if="listError" class="state-box state-error">
          <div class="state-info">
            <span class="state-title">优惠券列表加载失败</span>
            <span class="state-desc">{{ listError }}</span>
          </div>
          <Button :loading="listLoading" size="small" type="primary" ghost @click="load">点击重试</Button>
        </div>

        <Table v-else :columns="columns" :data-source="rows" :loading="listLoading" :scroll="{ x: 820 }" row-key="id">
          <template #emptyText>
            <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无优惠券模板" />
          </template>
          <template #bodyCell="{ column, record }">
            <div v-if="column.key === 'name'" class="coupon-info-cell">
              <span class="coupon-name">{{ record.name }}</span>
              <span class="coupon-id-row">
                <span class="coupon-id">模板 ID：{{ record.id }}</span>
                <Button
                  :aria-label="`复制模板 ID ${record.id}`"
                  class="copy-id-button"
                  size="small"
                  type="link"
                  @click="copyTemplateId(record.id)"
                >
                  复制
                </Button>
              </span>
            </div>
            <Tag v-if="column.key === 'status'" :color="getStatusDisplay(record.status).color">
              {{ getStatusDisplay(record.status).label }}
            </Tag>
            <template v-else-if="column.key === 'discountAmount'">
              <span class="amount-val">¥{{ record.discountAmount }}</span>
            </template>
            <template v-else-if="column.key === 'minimumAmount'">
              <span>{{ Number(record.minimumAmount) > 0 ? `满 ¥${record.minimumAmount}` : '无门槛' }}</span>
            </template>
            <template v-else-if="column.key === 'availableQuantity'">
              <span>{{ record.availableQuantity }} <span class="sub-qty">/ {{ record.totalQuantity }}</span></span>
            </template>
            <template v-else-if="column.key === 'expiresAt'">
              {{ formatDate(record.expiresAt) }}
            </template>
            <Space v-else-if="column.key === 'actions'">
              <Button :disabled="statusLoading" type="link" @click="edit(record)">编辑</Button>
              <Button :disabled="statusLoading" type="link" @click="changeStatus(record)">
                {{ record.status === 'ACTIVE' ? '停用' : '启用' }}
              </Button>
            </Space>
          </template>
        </Table>
      </section>
    </div>
  </main>
</template>

<style scoped>
.panel-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.panel-header-row .market-panel-title {
  margin-bottom: 0;
}
.list-count-tag {
  margin: 0;
  font-weight: 600;
}
.state-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: var(--mall-radius-base, 8px);
  border: 1px solid hsl(var(--border));
  background: hsl(var(--muted) / 0.4);
}
.state-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.state-title {
  font-size: 13.5px;
  font-weight: 600;
  color: hsl(var(--foreground));
}
.state-desc {
  font-size: 12.5px;
  color: hsl(var(--muted-foreground));
  word-break: break-word;
}
.edit-pill {
  font-size: 12px;
  color: var(--primary, #0960bd);
  background: rgba(9, 96, 189, 0.08);
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.form-pair {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.full {
  width: 100%;
}
.notice {
  margin-bottom: 16px;
}
.form-hint {
  margin-bottom: 16px;
}
.amount-val {
  font-weight: 600;
  color: var(--market-text);
}
.coupon-info-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  min-width: 128px;
  line-height: 1.5;
}
.coupon-name {
  max-width: 100%;
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.coupon-id-row {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--market-muted);
  font-size: 12px;
}
.coupon-id {
  user-select: all;
}
.copy-id-button {
  height: auto;
  padding: 0 4px;
  font-size: 12px;
}
.sub-qty {
  font-size: 12px;
  color: var(--market-muted);
}
@media (max-width: 640px) {
  .form-pair {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
@media (max-width: 480px) {
  .panel-header-row {
    flex-wrap: wrap;
    gap: 8px;
  }
  .state-box {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
