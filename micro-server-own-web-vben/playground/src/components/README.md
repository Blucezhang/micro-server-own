# 演示页公共组件

此目录仅服务于 `playground`。跨应用组件继续优先使用 `@vben/common-ui` 和已有基础组件；不要把演示页特有模式直接推广成商城业务约定。

## DemoOutput

用于展示已经格式化的 JSON、表单值或 HTML 源文本。格式化留在调用方，组件只渲染文本，不解析 JSON，也不执行 HTML。

```vue
<script setup lang="ts">
import DemoOutput from '#/components/demo-output.vue';
</script>

<template>
  <DemoOutput :text="formattedValues" />
  <DemoOutput
    :text="formattedValues"
    class="max-h-105 p-3 text-muted-foreground"
    size="xs"
  />
  <DemoOutput
    :text="htmlSource"
    class="rounded-xl border border-border"
    size="inherit"
  />
</template>
```

| 属性 | 类型 | 默认值 | 用途 |
| --- | --- | --- | --- |
| `text` | `string` | 空字符串 | 原样展示的文本，包括换行和缩进 |
| `size` | `'sm' \| 'xs' \| 'inherit'` | `'sm'` | 小字号、紧凑字号，或继承原页面字号 |
| `class` | Vue `HTMLAttributes['class']` | 未设置 | 使用已有工具类调整圆角、边框、内边距和最大高度 |

默认复用 `bg-muted`、`overflow-auto`、`rounded-md`、`p-4`；`cn` 合并调用方样式，避免产生多个冲突的内边距或圆角类。普通 HTML 属性传递到唯一的 `<pre>` 根元素，不增加外层容器或新的操作。

适用：只读文本输出。结构化树形 JSON、语法高亮、可编辑输入应继续使用各自已有组件，不往此处堆叠无关功能。

本次迁移覆盖数组表单、偏好扩展、值格式化及 Tiptap 的 5 处输出。保留各处原有内容来源、字号与容器差异；没有新增颜色或间距数值，也没有修改表单、上传和偏好行为。
