<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useMediaQuery } from '@vueuse/core';
import { useRouter } from 'vue-router';
import { Alert, Button, Drawer, Empty, Form, Input, InputNumber, Select, Space, Table, Tag } from 'ant-design-vue';
import { catalogApi, merchantApi } from '#/api/marketplace';
import { useAuthStore } from '#/store/auth';

type ProductRow = Record<string, number | string | undefined>;
type ProductForm = {
  categoryId?: number;
  name: string;
  originalPrice?: number;
  partyId?: number;
  priceChangeReason: string;
  promotionPrice?: number;
  skuCode: string;
};

interface CategoryOption {
  id: number;
  name: string;
}

const router = useRouter();
const authStore = useAuthStore();
const listLoading = ref(false);
const actionLoading = ref(false);
const listError = ref('');
const rows = ref<ProductRow[]>([]);
const page = ref(0);
const total = ref(0);
const editingId = ref<number>();
const editorOpen = ref(false);
const compactLayout = useMediaQuery('(max-width: 1000px)');
const actionFeedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const drawerFeedback = ref<null | { text: string; type: 'error' | 'success' }>(null);
const pageSize = 20;
const form = reactive<ProductForm>(emptyForm());

const categories = ref<CategoryOption[]>([]);
const categoriesLoading = ref(false);
const categoriesFailed = ref(false);
const manualCategoryInput = ref(false);

const pendingRequestKey = ref('');
const lastPayloadKey = ref('');

const columns = [
  { dataIndex: 'name', key: 'name', minWidth: 200, title: '商品信息', width: 240 },
  { dataIndex: 'skuCode', key: 'skuCode', title: 'SKU 编码', width: 130 },
  { dataIndex: 'originalPrice', key: 'originalPrice', title: '价格', width: 140 },
  { dataIndex: 'saleStatus', key: 'saleStatus', title: '销售状态', width: 96 },
  { key: 'actions', title: '操作', width: 230 },
];

const categoryOptions = computed(() => {
  const opts = categories.value.map((c) => ({ label: `${c.name} (ID: ${c.id})`, value: c.id }));
  if (form.categoryId && !opts.some((o) => o.value === form.categoryId)) {
    opts.unshift({ label: `类目 #${form.categoryId}`, value: form.categoryId });
  }
  return opts;
});

const drawerWidth = computed(() => (compactLayout.value ? '100%' : 540));

function emptyForm(): ProductForm {
  return { categoryId: undefined, name: '', originalPrice: undefined, partyId: undefined, priceChangeReason: '', promotionPrice: undefined, skuCode: '' };
}

function message(error: unknown, fallback: string) {
  if (!(error instanceof Error) || /^request failed with status code \d{3}$/i.test(error.message)) {
    return fallback;
  }
  return error.message;
}

function formatPrice(value: unknown) {
  if (value === null || value === undefined || value === '') return '—';
  const amount = Number(value);
  return Number.isFinite(amount) ? amount.toFixed(2) : '—';
}

function categoryLabel(id: unknown) {
  if (id == null || id === '') return '';
  const numId = Number(id);
  const found = categories.value.find((c) => c.id === numId);
  return found ? found.name : `类目 #${numId}`;
}

function filterCategoryOption(input: string, option?: { label?: unknown }) {
  return String(option?.label ?? '').toLowerCase().includes(input.toLowerCase());
}

function reset() {
  editingId.value = undefined;
  drawerFeedback.value = null;
  pendingRequestKey.value = '';
  lastPayloadKey.value = '';
  manualCategoryInput.value = false;
  Object.assign(form, emptyForm());
}

function openCreate() {
  reset();
  if (authStore.actorId) form.partyId = authStore.actorId;
  editorOpen.value = true;
}

function closeEditor() {
  editorOpen.value = false;
  reset();
}

async function loadCategories() {
  categoriesLoading.value = true;
  categoriesFailed.value = false;
  try {
    const res = await catalogApi.categories();
    const list = Array.isArray(res) ? res : res && typeof res === 'object' && Array.isArray((res as any).items) ? (res as any).items : [];
    categories.value = list.map((item: any) => ({ id: Number(item.id), name: String(item.name || `类目 #${item.id}`) }));
  } catch {
    categoriesFailed.value = true;
    categories.value = [];
  } finally {
    categoriesLoading.value = false;
  }
}

async function load(clearActionFeedback = false) {
  listLoading.value = true;
  listError.value = '';
  if (clearActionFeedback) actionFeedback.value = null;
  try {
    const result = (await merchantApi.products({ page: page.value, size: pageSize })) as { items?: ProductRow[]; total?: number };
    rows.value = result.items || [];
    total.value = Number(result.total || 0);
  } catch (error) {
    listError.value = message(error, '商品列表加载失败，请重试');
  } finally {
    listLoading.value = false;
  }
}

function edit(row: ProductRow) {
  reset();
  editingId.value = Number(row.id);
  const rowPartyId = Number(row.partyId);
  Object.assign(form, {
    categoryId: Number(row.categoryId) || undefined,
    name: String(row.name || ''),
    originalPrice: row.originalPrice == null || row.originalPrice === '' ? undefined : Number(row.originalPrice),
    partyId: authStore.actorId ?? (Number.isSafeInteger(rowPartyId) && rowPartyId > 0 ? rowPartyId : undefined),
    priceChangeReason: '',
    promotionPrice: row.promotionPrice == null ? undefined : Number(row.promotionPrice),
    skuCode: String(row.skuCode || ''),
  });
  editorOpen.value = true;
}

function validateForm(): string | null {
  if (!form.name || !form.name.trim()) return '请填写商品名称';
  if (!form.skuCode || !form.skuCode.trim()) return '请填写 SKU 编码';
  if (form.partyId == null || !Number.isFinite(form.partyId) || form.partyId <= 0) {
    return authStore.actorId ? '商户身份未正确绑定' : '请填写商户 ID';
  }
  if (form.categoryId == null || !Number.isFinite(form.categoryId) || form.categoryId <= 0) return '请选择或填写有效的商品类目';
  if (form.originalPrice == null || !Number.isFinite(form.originalPrice) || form.originalPrice < 0) return '请填写大于或等于 0 的原价';
  if (form.promotionPrice != null) {
    if (!Number.isFinite(form.promotionPrice) || form.promotionPrice < 0) return '促销价必须大于或等于 0';
    if (form.promotionPrice > form.originalPrice) return '促销价不能高于原价';
  }
  if (form.priceChangeReason && form.priceChangeReason.length > 200) return '改价原因不能超过 200 个字符';
  return null;
}

async function save() {
  if (actionLoading.value) return;
  const validationError = validateForm();
  if (validationError) {
    drawerFeedback.value = { text: validationError, type: 'error' };
    return;
  }
  const partyId = authStore.actorId ?? Number(form.partyId);
  const payload = {
    categoryId: Number(form.categoryId),
    name: form.name.trim(),
    originalPrice: Number(form.originalPrice),
    partyId,
    priceChangeReason: form.priceChangeReason ? form.priceChangeReason.trim() : '',
    promotionPrice: form.promotionPrice == null ? undefined : Number(form.promotionPrice),
    skuCode: form.skuCode.trim(),
  };
  const payloadSignature = JSON.stringify({ editingId: editingId.value, ...payload });
  if (!pendingRequestKey.value || payloadSignature !== lastPayloadKey.value) {
    pendingRequestKey.value = crypto.randomUUID();
    lastPayloadKey.value = payloadSignature;
  }
  actionLoading.value = true;
  drawerFeedback.value = null;
  try {
    await merchantApi.saveProduct(payload, editingId.value, pendingRequestKey.value);
    pendingRequestKey.value = '';
    lastPayloadKey.value = '';
    closeEditor();
    actionFeedback.value = { text: '商品信息已保存；改价记录可在价格审计中查看。', type: 'success' };
    await load();
  } catch (error) {
    drawerFeedback.value = { text: message(error, '保存失败，请检查后重试'), type: 'error' };
  } finally {
    actionLoading.value = false;
  }
}

async function changeStatus(row: ProductRow) {
  if (actionLoading.value) return;
  actionLoading.value = true;
  actionFeedback.value = null;
  try {
    const id = Number(row.id);
    await merchantApi.changeProductStatus(id, row.saleStatus === 'AVAILABLE' ? 'OFF_SHELF' : 'AVAILABLE');
    actionFeedback.value = { text: `商品「${row.name || id}」状态已更新`, type: 'success' };
    await load();
  } catch (error) {
    actionFeedback.value = { text: message(error, '商品状态更新失败，请稍后重试'), type: 'error' };
  } finally {
    actionLoading.value = false;
  }
}

async function changePage(pagination: { current?: number }) {
  page.value = (pagination.current || 1) - 1;
  await load();
}

onMounted(() => {
  load();
  loadCategories();
});
</script>

<template>
  <main class="market-page">
    <header class="market-head">
      <div class="market-head-text">
        <span class="market-overline">MERCHANT · CATALOG</span>
        <h1 class="market-heading">商品管理</h1>
        <p class="market-subtitle">维护店铺商品与上下架状态；价格变更由服务端留存审计记录。</p>
      </div>
      <div class="market-head-actions">
        <Space wrap>
          <Button :loading="listLoading" @click="load(true)">刷新</Button>
          <Button type="primary" @click="openCreate">发布商品</Button>
        </Space>
      </div>
    </header>

    <Alert v-if="actionFeedback" :message="actionFeedback.text" :type="actionFeedback.type" closable show-icon @close="actionFeedback = null" />

    <section class="market-panel product-list-panel" aria-labelledby="product-list-title">
      <div class="list-heading">
        <div>
          <h2 id="product-list-title" class="market-panel-title">商品目录</h2>
          <p class="market-note">
            <template v-if="listError">数据加载受阻 · 请点击重试</template>
            <template v-else-if="listLoading && rows.length === 0">正在载入商品数据...</template>
            <template v-else>共 {{ total }} 件商品 · 价格与销售状态以服务端数据为准</template>
          </p>
        </div>
        <Tag v-if="!listError" class="catalog-count">{{ total }} 件</Tag>
        <Tag v-else color="error" class="catalog-status-tag">加载失败</Tag>
      </div>

      <div v-if="listError" class="state-box state-error">
        <div class="state-info">
          <span class="state-title">商品列表加载失败</span>
          <span class="state-desc">{{ listError }}</span>
        </div>
        <Button :loading="listLoading" size="small" type="primary" ghost @click="load(true)">重新加载</Button>
      </div>

      <div v-else class="table-container">
        <Table
          :columns="columns"
          :data-source="rows"
          :loading="listLoading"
          :pagination="total > 0 ? { current: page + 1, pageSize, total, showSizeChanger: false } : false"
          :scroll="compactLayout ? { x: 800 } : undefined"
          row-key="id"
          @change="changePage"
        >
          <template #emptyText>
            <Empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="暂无商品数据" />
          </template>
          <template #bodyCell="{ column, record }">
            <div v-if="column.key === 'name'" class="product-name-cell">
              <div class="product-title-row">
                <strong>{{ record.name || '未命名商品' }}</strong>
                <Tag v-if="compactLayout" :color="record.saleStatus === 'AVAILABLE' ? 'green' : 'default'" class="mobile-status-inline">
                  {{ record.saleStatus === 'AVAILABLE' ? '销售中' : '已下架' }}
                </Tag>
              </div>
              <span class="product-subtext">
                商品 ID {{ record.id }}
                <template v-if="categoryLabel(record.categoryId)"> · {{ categoryLabel(record.categoryId) }}</template>
              </span>
            </div>
            <code v-else-if="column.key === 'skuCode'" class="sku-code">{{ record.skuCode || '—' }}</code>
            <div v-else-if="column.key === 'originalPrice'" class="price-cell">
              <strong>¥{{ formatPrice(record.promotionPrice ?? record.originalPrice) }}</strong>
              <span v-if="record.promotionPrice != null && Number(record.promotionPrice) !== Number(record.originalPrice)">原价 ¥{{ formatPrice(record.originalPrice) }}</span>
            </div>
            <Tag v-else-if="column.key === 'saleStatus'" :color="record.saleStatus === 'AVAILABLE' ? 'green' : 'default'">
              {{ record.saleStatus === 'AVAILABLE' ? '销售中' : '已下架' }}
            </Tag>
            <Space v-else-if="column.key === 'actions'" :size="2" class="row-actions">
              <Button size="small" type="link" @click="edit(record)">编辑</Button>
              <Button size="small" type="link" @click="router.push({ path: '/merchant/price-audits', query: { productId: String(record.id) } })">价格记录</Button>
              <Button size="small" type="link" @click="router.push({ path: '/merchant/reviews', query: { productId: String(record.id) } })">评价</Button>
              <Button :danger="record.saleStatus === 'AVAILABLE'" size="small" type="link" @click="changeStatus(record)">
                {{ record.saleStatus === 'AVAILABLE' ? '下架' : '上架' }}
              </Button>
            </Space>
          </template>
        </Table>
      </div>
    </section>

    <Drawer
      v-model:open="editorOpen"
      :closable="false"
      :width="drawerWidth"
      destroy-on-close
      placement="right"
      @close="closeEditor"
    >
      <template #title>
        <div class="editor-heading">
          <span class="market-overline">MERCHANT · PRODUCT</span>
          <h2>{{ editingId ? '编辑商品' : '发布商品' }}</h2>
          <p>填写商品基础信息，保存后即可在商品目录中管理状态。</p>
        </div>
      </template>
      <Form :disabled="actionLoading" :model="form" layout="vertical" @finish="save">
        <div class="form-pair">
          <Form.Item label="商户 ID" name="partyId">
            <div v-if="editingId" class="bound-actor-card">
              <div class="bound-actor-header">
                <span class="bound-actor-id">ID: {{ form.partyId ?? '—' }}</span>
                <Tag>归属不可转让</Tag>
              </div>
              <span class="bound-actor-desc">已创建商品的归属商户不可变更</span>
            </div>
            <div v-else-if="authStore.actorId" class="bound-actor-card bound-actor-active">
              <div class="bound-actor-header">
                <span class="bound-actor-id">ID: {{ authStore.actorId }}</span>
                <Tag color="blue">当前登录商户</Tag>
              </div>
              <span class="bound-actor-desc">已自动绑定当前已认证商户身份</span>
            </div>
            <template v-else>
              <InputNumber v-model:value="form.partyId" :min="1" class="full" placeholder="本地演示：输入商户 ID" />
              <span class="field-hint">本地演示模式未获取到授权商户，请输入测试商户 ID</span>
            </template>
          </Form.Item>

          <Form.Item label="商品类目" name="categoryId" :rules="[{ required: true, message: '请选择或填写商品类目' }]">
            <div class="category-field-wrap">
              <template v-if="!manualCategoryInput && categories.length > 0">
                <Select
                  v-model:value="form.categoryId"
                  :filter-option="filterCategoryOption"
                  :loading="categoriesLoading"
                  :options="categoryOptions"
                  class="full"
                  placeholder="选择商品类目"
                  show-search
                />
                <Button size="small" type="link" class="category-toggle" @click="manualCategoryInput = true">手动输入编号</Button>
              </template>
              <template v-else>
                <InputNumber v-model:value="form.categoryId" :min="1" class="full" placeholder="输入类目编号" />
                <Button v-if="categories.length > 0" size="small" type="link" class="category-toggle" @click="manualCategoryInput = false">从列表中选择</Button>
                <Button v-else-if="categoriesFailed" :loading="categoriesLoading" size="small" type="link" class="category-toggle" @click="loadCategories">重试载入类目</Button>
              </template>
            </div>
            <span v-if="categoriesFailed && categories.length === 0" class="field-hint hint-warning">类目服务暂未就绪，可直接填写编号</span>
          </Form.Item>
        </div>

        <Form.Item label="商品名称" name="name" :rules="[{ required: true, message: '请填写商品名称' }]">
          <Input v-model:value="form.name" placeholder="清晰描述商品名称" />
        </Form.Item>
        <Form.Item label="SKU 编码" name="skuCode" :rules="[{ required: true, message: '请填写 SKU 编码' }]">
          <Input v-model:value="form.skuCode" placeholder="用于库存与订单识别" />
        </Form.Item>
        <div class="form-pair">
          <Form.Item label="原价（元）" name="originalPrice" :rules="[{ required: true, message: '请填写原价' }]">
            <InputNumber v-model:value="form.originalPrice" :min="0" :precision="2" class="full" placeholder="0.00" />
          </Form.Item>
          <Form.Item label="促销价（元）" name="promotionPrice">
            <InputNumber v-model:value="form.promotionPrice" :min="0" :precision="2" class="full" placeholder="选填（不能高于原价）" />
          </Form.Item>
        </div>
        <Form.Item v-if="editingId" label="改价原因" name="priceChangeReason">
          <Input v-model:value="form.priceChangeReason" :maxlength="200" show-count placeholder="说明价格调整原因，便于审计追溯（最多 200 字）" />
        </Form.Item>
        <Alert v-if="drawerFeedback?.type === 'error'" :message="drawerFeedback.text" class="notice" show-icon type="error" />
        <div class="editor-footer">
          <Button :disabled="actionLoading" @click="closeEditor">取消</Button>
          <Button :loading="actionLoading" html-type="submit" type="primary">{{ editingId ? '保存修改' : '确认发布' }}</Button>
        </div>
      </Form>
    </Drawer>
  </main>
</template>

<style scoped>
.market-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.market-head-text { min-width: 0; flex: 1 1 240px; }
.market-head-actions { flex-shrink: 0; }
.form-pair { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.full { width: 100%; }
.notice { margin-bottom: 16px; }
.product-list-panel { padding-bottom: 10px; max-width: 100%; overflow: hidden; }
.list-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.list-heading .market-panel-title { margin-bottom: 4px; }
.catalog-count { margin: 0; padding: 4px 10px; border: 0; border-radius: 999px; background: hsl(var(--accent)); color: hsl(var(--accent-foreground)); font-weight: 600; }
.catalog-status-tag { margin: 0; font-weight: 600; }
.state-box { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 16px 18px; border-radius: var(--mall-radius-base, 8px); border: 1px solid hsl(var(--border)); background: hsl(var(--muted) / 0.4); }
.state-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.state-title { font-size: 13.5px; font-weight: 600; color: hsl(var(--foreground)); }
.state-desc { font-size: 12.5px; color: hsl(var(--muted-foreground)); word-break: break-word; }
.table-container { width: 100%; max-width: 100%; overflow: hidden; }
:deep(.ant-table-content) { overflow-x: auto !important; -webkit-overflow-scrolling: touch; scrollbar-width: thin; }
:deep(.ant-table-content::-webkit-scrollbar) { height: 6px; }
:deep(.ant-table-content::-webkit-scrollbar-thumb) { background: hsl(var(--muted-foreground) / 0.35); border-radius: 3px; }
:deep(.ant-table-content::-webkit-scrollbar-track) { background: hsl(var(--muted) / 0.2); }
.product-name-cell, .price-cell { display: grid; gap: 4px; }
.product-title-row { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.product-name-cell strong, .price-cell strong { color: hsl(var(--foreground)); font-size: 13px; font-weight: 650; }
.product-subtext, .price-cell span { color: hsl(var(--muted-foreground)); font-size: 12px; }
.mobile-status-inline { margin: 0; font-size: 11px; line-height: 1.4; padding: 0 5px; }
.price-cell strong { color: hsl(var(--primary)); font-size: 14px; font-variant-numeric: tabular-nums; }
.sku-code { padding: 3px 6px; border-radius: 5px; background: hsl(var(--muted) / 0.8); color: hsl(var(--foreground)); font-size: 12px; }
.row-actions { flex-wrap: nowrap; white-space: nowrap; }
.editor-heading { padding: 2px 0 4px; }
.editor-heading h2 { margin: 6px 0 5px; color: hsl(var(--foreground)); font-size: 20px; font-weight: 650; }
.editor-heading p { margin: 0; color: hsl(var(--muted-foreground)); font-size: 13px; font-weight: 400; line-height: 1.6; }
.bound-actor-card { padding: 8px 12px; border-radius: var(--mall-radius-base, 6px); background: hsl(var(--muted) / 0.45); border: 1px solid hsl(var(--border)); display: flex; flex-direction: column; gap: 2px; }
.bound-actor-active { background: hsl(var(--primary) / 0.05); border-color: hsl(var(--primary) / 0.25); }
.bound-actor-header { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.bound-actor-id { font-weight: 650; font-size: 13px; color: hsl(var(--foreground)); }
.bound-actor-desc { font-size: 11.5px; color: hsl(var(--muted-foreground)); }
.category-field-wrap { display: flex; flex-direction: column; gap: 4px; }
.category-toggle { align-self: flex-start; padding: 0; height: auto; font-size: 12px; }
.field-hint { display: block; margin-top: 4px; font-size: 12px; color: hsl(var(--muted-foreground)); line-height: 1.4; }
.hint-warning { color: hsl(28 72% 36%); }
.editor-footer { position: sticky; bottom: -24px; display: flex; justify-content: flex-end; gap: 10px; margin: 28px -24px -24px; padding: 14px 20px; border-top: 1px solid hsl(var(--border)); background: hsl(var(--card)); flex-wrap: wrap; }
@media (max-width: 640px) {
  .market-head { flex-direction: column; align-items: stretch; gap: 12px; }
  .market-head-text { flex: 0 1 auto; }
  .market-head-actions { width: 100%; }
  .market-head-actions :deep(.ant-space) { width: 100%; justify-content: flex-start; }
  .form-pair { grid-template-columns: 1fr; gap: 0; }
}
@media (max-width: 480px) {
  .list-heading { flex-direction: column; align-items: flex-start; gap: 8px; }
  .state-box { flex-direction: column; align-items: flex-start; gap: 10px; }
}
</style>
