import { computed, type Ref } from 'vue';

import type { ColumnType } from 'ant-design-vue/es/table';

export function useBuyerOrderTableColumns(
  phoneLayout: Readonly<Ref<boolean>>,
  viewportWidth: Readonly<Ref<number>>,
) {
  const orderColumns: ColumnType[] = [
    { dataIndex: 'orderNo', key: 'orderNo', title: '订单号', width: 220 },
    { dataIndex: 'status', key: 'status', title: '状态', width: 110 },
    { dataIndex: 'payableAmount', key: 'payableAmount', title: '应付金额', width: 130 },
    { key: 'createdAt', title: '创建时间', width: 190 },
    { key: 'actions', title: '操作', width: 130, fixed: 'right' },
  ];
  const subOrderColumns: ColumnType[] = [
    { dataIndex: 'subOrderNo', key: 'subOrderNo', title: '子订单号', width: 220 },
    { dataIndex: 'status', key: 'status', title: '状态', width: 110 },
    { key: 'logistics', title: '物流', width: 220 },
    { key: 'actions', title: '操作', width: 146, fixed: 'right' },
  ];
  const itemColumns: ColumnType[] = [
    { dataIndex: 'productName', key: 'productName', title: '商品', width: 260 },
    { dataIndex: 'quantity', key: 'quantity', title: '数量', width: 90 },
    { dataIndex: 'lineTotal', key: 'lineTotal', title: '小计', width: 120 },
    { key: 'actions', title: '操作', width: 100, fixed: 'right' },
  ];

  return {
    items: computed<ColumnType[]>(() => phoneLayout.value
      ? [
          { dataIndex: 'productName', key: 'productName', title: '商品信息', width: Math.max(120, viewportWidth.value - 166) },
          { key: 'actions', title: '操作', width: 100 },
        ]
      : itemColumns),
    orders: computed<ColumnType[]>(() => phoneLayout.value
      ? [
          { dataIndex: 'orderNo', key: 'orderNo', title: '订单信息', width: Math.max(120, viewportWidth.value - 166) },
          { key: 'actions', title: '操作', width: 100 },
        ]
      : orderColumns),
    subOrders: computed<ColumnType[]>(() => phoneLayout.value
      ? [
          { dataIndex: 'subOrderNo', key: 'subOrderNo', title: '配送信息', width: Math.max(100, viewportWidth.value - 212) },
          { key: 'actions', title: '操作', width: 146 },
        ]
      : subOrderColumns),
  };
}
