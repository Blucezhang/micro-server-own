---
name: Vben Playground
description: 清晰的操作台；记录演示应用现有设计，并将改进方向与实现事实分开。
colors:
  primary: 'hsl(212 100% 45%)'
  primary-foreground: 'hsl(0 0% 98%)'
  background: 'hsl(0 0% 100%)'
  background-deep: 'hsl(216 20.11% 95.47%)'
  foreground: 'hsl(210 6% 21%)'
  card: 'hsl(0 0% 100%)'
  card-foreground: 'hsl(222.2 84% 4.9%)'
  muted: 'hsl(240 4.8% 95.9%)'
  muted-foreground: 'hsl(240 3.8% 46.1%)'
  border: 'hsl(240 5.9% 90%)'
  accent: 'hsl(240 5% 96%)'
  success: 'hsl(144 57% 58%)'
  warning: 'hsl(42 84% 61%)'
  destructive: 'hsl(348 100% 61%)'
  dark-background: 'hsl(222.34deg 10.43% 12.27%)'
  dark-background-deep: 'hsl(220deg 13.06% 9%)'
  dark-foreground: 'hsl(0 0% 95%)'
  dark-card-foreground: 'hsl(210 40% 98%)'
  dark-muted: 'hsl(240 3.7% 15.9%)'
  dark-muted-foreground: 'hsl(240 5% 64.9%)'
  dark-border: 'hsl(240 3.7% 22%)'
  dark-popover: 'hsl(0 0% 14.2%)'
  dark-accent: 'hsl(216 5% 19%)'
typography:
  body:
    fontFamily: "-apple-system, blinkmacsystemfont, 'Segoe UI', roboto, 'Helvetica Neue', arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji'"
    fontSize: '1rem'
    lineHeight: 1.15
  title:
    fontSize: '1.125rem'
    fontWeight: 600
    lineHeight: '1.75rem'
  chart-title:
    fontSize: '1.25rem'
    fontWeight: 600
  label:
    fontSize: '0.875rem'
    fontWeight: 500
    lineHeight: '1.25rem'
  badge:
    fontSize: '0.75rem'
    fontWeight: 500
    lineHeight: '1rem'
rounded:
  sm: '0.25rem'
  md: '0.375rem'
  lg: '0.5rem'
  xl: '0.75rem'
spacing:
  unit: '0.25rem'
  xs: '0.5rem'
  sm: '0.75rem'
  md: '1rem'
  page-demo: '1.25rem'
  lg: '1.5rem'
components:
  button-primary:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.primary-foreground}'
    typography: '{typography.label}'
    rounded: '{rounded.md}'
    height: '2.25rem'
    padding: '0.5rem 1rem'
  button-outline:
    backgroundColor: '{colors.background}'
    textColor: '{colors.foreground}'
    typography: '{typography.label}'
    rounded: '{rounded.md}'
    height: '2.25rem'
    padding: '0.5rem 1rem'
  button-ghost:
    typography: '{typography.label}'
    rounded: '{rounded.md}'
    height: '2.25rem'
    padding: '0.5rem 1rem'
  input:
    backgroundColor: '{colors.background}'
    textColor: '{colors.foreground}'
    rounded: '{rounded.md}'
    height: '2.5rem'
    padding: '0.5rem 0.75rem'
  card:
    backgroundColor: '{colors.card}'
    textColor: '{colors.card-foreground}'
    rounded: '{rounded.xl}'
    padding: '1.5rem 0'
  badge-primary:
    backgroundColor: '{colors.primary}'
    textColor: '{colors.primary-foreground}'
    typography: '{typography.badge}'
    padding: '0.125rem 0.5rem'
  tabs:
    backgroundColor: '{colors.muted}'
    textColor: '{colors.muted-foreground}'
    rounded: '{rounded.md}'
    height: '2.25rem'
    padding: '0.25rem'
  demo-output:
    backgroundColor: '{colors.muted}'
    rounded: '{rounded.md}'
    padding: '{spacing.md}'
---

# Design System: Vben Playground

## Overview

**Creative North Star: "清晰的操作台"**

本文件仅覆盖 `playground` 演示应用及其实际引用的共享组件，不是商城 `apps/web-antd` 的品牌规范，也不覆盖其他示例应用。现状为 Vue 3、Vben、Tailwind CSS 4、Reka UI 基础组件与 `antdv-next` 业务组件共同组成的后台演示界面。

用户确认的气质是轻盈、柔和：圆角与留白承载主要秩序，阴影辅助表达层次。该描述用于理解现有设计和指导后续工作，不表示当前每个页面都已经符合它。用户确认的改进方向为“美观、大方、简洁、减少模板感”；本次只记录，不修改页面，不新增品牌承诺，不将演示数据变为真实业务。

**Key Characteristics:**

- 任务与信息优先，视觉表达服务于阅读、定位和操作。
- 用共享语义变量连接基础组件与 Ant 业务组件，保留主题与密度设置。
- 采用柔和圆角、留白与辅助阴影；具体数值以实现来源为准。
- 严格区分源码事实、用户确认的方向与未经执行的视觉验收。

提取日期：2026-10-26。证据为本地源码；浏览器工具因现有会话占用未能连接，独立 Playwright 检查也未找到其配置的浏览器可执行文件，未安装浏览器或修改环境。本次没有实际页面截图、计算样式或响应式验收。此前 `detect` 的 188 个文件扫描仅为正则检测，不能证明对比度、字号和布局合格。

主要实现来源：

- [应用主题入口](src/app.vue)、[偏好覆盖](src/preferences.ts)、[组件适配器](src/adapter/component/index.ts)。
- [共享默认偏好](../packages/@core/preferences/src/config.ts)、[运行时变量更新](../packages/@core/preferences/src/update-css-variables.ts)。
- [亮色变量](../packages/@core/base/design/src/design-tokens/default.css)、[暗色变量](../packages/@core/base/design/src/design-tokens/dark.css)。
- [Tailwind 桥接与全局样式](../internal/tailwind-config/src/theme.css)、[Ant 变量映射](../packages/effects/hooks/src/use-design-tokens.ts)。

## Colors

### Primary

**操作蓝**：`colors.primary` 表示默认主色输入，用于主要操作、链接及部分选中状态。它是框架默认值，不是确认过的商城或企业品牌色。主色可由偏好设置及内置主题改变，不要在页面重新硬编码。

### Neutral

**冷灰底**：`background-deep` 对应亮色布局底，白色 `background`、`card` 与边界色共同形成内容层次；次级文字用 `muted-foreground`，正文用 `foreground`。

**石墨暗底**：`dark-background-deep` 对应暗色布局底；内容背景对应 `dark-background`，卡片复用该背景；浮层使用 `dark-popover`。正文、卡片文字、弱文字和边界均有独立暗色值，不能只反转画布颜色。

### Semantic states and runtime precedence

成功、警告、破坏性操作分别使用 `success`、`warning`、`destructive`。这些是状态角色，不是新增的装饰性配色。`useAntdDesignTokens` 把 Ant 的 `colorInfo` 映射到主色，而非 CSS 中的 `--info` 初值。

前置 YAML 是默认配置输入及默认主题 CSS 的来源快照，不是浏览器计算值。状态色与主色输入来自共享偏好，其余颜色来自共享主题 CSS；运行时会生成色阶、更新基础变量，并可读取持久化偏好。共享偏好默认主题为暗色；`playground` 未覆盖该设置。CSS 中破坏色的亮暗初值与偏好输入不同，不能把三者同时宣称为启动后的最终值。

**The Semantic Theme Rule.** 复用当前主题的语义变量；不得把本文件的默认快照复制为组件里的固定颜色。完整主题变体仍以共享 CSS、偏好设置和实际计算样式为准。

## Typography

**Body Font:** 系统无衬线字体栈，详见 `typography.body`。项目未在此层固定一款中文品牌字体；中文最终字体由系统回退决定，不应宣称已统一为某一字体。

### Hierarchy

- **Body**：根节点默认字号由 `--font-size-base` 控制，根行高见前置 YAML；这不是所有正文组件的行高。`text-base` 工具类另有 1.5 的行高，Ant 也有自己的排版算法。
- **Title**：`Page` 标题使用 `text-lg font-semibold`；图表卡片使用 `text-xl`，与共享 `CardTitle` 组合。它们是两个真实层级，不强行合并。
- **Label**：基础按钮采用 `text-sm font-medium`；菜单字体另由 `--menu-font-size` 计算，默认是基础字号的 0.875 倍。
- **Badge**：基础标签使用 `text-xs font-medium`，仅适合短状态文字。

尺寸均为默认根字号下的来源描述。调大根字号后，rem 尺寸与主题圆角会随之变化；Ant 适配器把圆角 rem 值按 16 换算，不能断言两套组件始终像素一致。

**改进方向，尚未实施或逐页验证：** 优先检查中文长标题、按钮文案和多行说明的换行、行高与截断，避免为了整齐而压缩可读信息；不增加装饰性英文大标题来制造层级。

## Layout

默认框架为侧边导航，侧栏宽 224px、收起宽 60px，顶部栏高 50px，多标签栏高 38px。默认内容宽松布局；紧凑内容宽度预设为 1200px，但默认不启用该内容模式。以上为可配置框架值，不是所有屏幕必须固定遵守的尺寸。

Tailwind 间距基数为 `spacing.unit`。共享 `Page` 内容采用 `spacing.md`；分析示例页外边距采用 `spacing.page-demo`；基础卡片纵向内边距及内部区块横向内边距采用 `spacing.lg`。这些约定并存，不把单个示例的间距扩展成全局唯一标准。

分析总览采用单列，在 `md` 切为两列、`lg` 切为四列；下方三个图表在 `md` 以上横向排列。`md` 与 `lg` 来自当前 Tailwind 默认断点。其他页面需按各自实现检查，不因本例具备断点而标记全站响应式通过。

`Page` 自动内容高度模式使用 `min-h-0`、尺寸包含和内部滚动；不要以任意固定高度替代该契约。参考 [Page](../packages/effects/common-ui/src/components/page/page.vue)、[分析页](src/views/dashboard/analytics/index.vue) 和 [总览网格](../packages/effects/common-ui/src/ui/dashboard/analysis/analysis-overview.vue)。

**改进方向，尚未实施：** 简化重复容器，统一同类页面的标题、工具栏和内容节奏；手机端应重新核对操作排列与溢出，而不只是缩小桌面布局。

## Elevation & Depth

现状不是完全扁平：基础卡片使用 Tailwind `shadow-sm`，主按钮源码使用 `shadow`，描边按钮使用 `shadow-sm`；浮层另有共享 `--shadow-float`。这些阴影的职责不同，不统一替换为一种“大而柔”的装饰阴影。

`.impeccable/design.json` 保存已提取的具体阴影与动效值。路由、侧栏、折叠区存在不同动画，不能把一个组件的时长写成全局唯一规范，也未验证所有动画对减少动态效果设置的响应。

**已确认的描述与后续方向：** 背景、留白和边界先建立层次，阴影作辅助。该方向不意味着现有所有阴影、动画或对比度已验收合格。

## Shapes

基础圆角由 `--radius` 控制；前置 YAML 的圆角表为默认值的解析快照。按钮和输入采用 `md`，基础卡片采用 `xl`；标签为胶囊形，导航圆润样式由各自组件控制。普通纵向菜单圆润样式为 8px，而混合布局的 `NormalMenu` 圆润样式为 6px，不能混为一条变量规则。

边框使用主题 `--border` 或 `--input`；组件和暗色状态可能另有透明度。保留现有图标尺寸、裁切和文字省略行为，未经页面验证不改写成新的形态禁令。

## Components

### Buttons

Vben 基础按钮与 `antdv-next` 按钮是两套组件。前置 YAML 中的按钮尺寸、圆角和间距来自 [基础按钮变体](../packages/@core/ui-kit/shadcn-ui/src/ui/button/button.ts)，不强制套用到 Ant。

基础按钮具备主、描边、幽灵、次级、破坏性、链接等变体；主按钮悬停使用主色透明度，描边与幽灵按钮悬停使用 `accent`。键盘焦点有环形提示，禁用态降低透明度；`VbenButton` 加载时显示图标并同时禁用。

### Inputs / Fields

基础 [Input](../packages/@core/ui-kit/shadcn-ui/src/ui/input/Input.vue) 使用描边与 `md` 圆角，聚焦时用主色内描边。Ant 表单通过适配器注册，错误态使用共享状态色和库内反馈，不根据基础 Input 推断复杂表单的全部状态。

### Cards / Containers

[基础 Card](../packages/@core/ui-kit/shadcn-ui/src/ui/card/Card.vue) 采用边框、轻阴影、`xl` 圆角和垂直区块间距；Header、Content、Footer 分别提供横向内边距。图表卡片复用此结构。不要误记为根容器四边都有同一内边距。

### Chips

[基础 Badge](../packages/@core/ui-kit/shadcn-ui/src/ui/badge/badge.ts) 为胶囊形，提供主色、次级、破坏性及描边变体；交互链接另有悬停状态。Ant Tag 保留自身实现。演示中的业务文字和数据不构成新的状态协议。

### Navigation

侧栏、面包屑和多标签页共同提供定位，框架支持不同导航布局。亮色普通菜单选中项使用半透明主色背景；暗色普通菜单使用 `accent` 背景和对应文字色。菜单收起可显示提示；不同菜单实现的字体、圆角和焦点处理需分别核对。[菜单源码](../packages/@core/ui-kit/menu-ui/src/components/menu.vue)。

### Tabs

基础 Tabs 使用弱背景轨道，活动项采用内容背景、边界或轻阴影提示，并保留键盘焦点和禁用状态。组件面板里的 CSS 摘录不是完整 Tabs 行为实现，也不是路由多标签栏的全部规范。

### Preview boundary

配套数据中的组件是从上述源码展开的、无需 Vue 运行时的视觉示意，使用 `ds-` 前缀和可继承的主题变量；示意里的焦点提示用于可访问预览。它们不是可以替代实际 Vue/Ant 组件的生产代码，不具备全部交互、校验、路由或加载行为。派生色阶仅供色板展示，不会写回运行时主题。

### DemoOutput

[DemoOutput](src/components/demo-output.vue) 是本应用局部提取的只读文本输出组件，供数组表单、偏好扩展、值格式化和 Tiptap 四个页面的五处输出复用；详见 [使用说明](src/components/README.md)。沿用弱背景、滚动和现有间距变量，保留各消费者的圆角、字号、边框与高度差异；不新增设计值或跨应用组件。

文本格式化仍由调用方负责，组件用 `v-text` 输出，不执行 HTML。默认小字号，紧凑输出使用 `size="xs"`，继承原页面字号使用 `size="inherit"`；调用方的 class 经 `cn` 合并。Antigravity 编写组件和页面迁移，Codex 收紧 class 类型并完成整合与验证。

本次组件测试夹具已在真实浏览器的 1440/390px、亮暗主题下比较五种输出容器的计算样式与截图，并检查空文本、更新和 HTML 转义。该证据仅限组件及容器，不是完整示例页或业务链路验收。前文初次文档提取的浏览器限制保留为当时记录。

## Do's and Don'ts

### Do:

- **Do** 复用现有主题变量与组件接口，区分源码事实、用户确认方向和未验证事项。
- **Do** 以“清晰的操作台”组织层级，用留白、圆角和辅助阴影表达轻盈、柔和。
- **Do** 在后续改造时优先改善中文可读性、布局和任务路径，再处理装饰；此项为已确认的改进方向。
- **Do** 对同一页面的亮暗主题、窄屏和真实交互另行执行验证，不以静态扫描代替。

### Don't:

- **Don't** 将操作蓝或演示数据认定为商城品牌或真实经营事实。
- **Don't** 将本文件扩展为 `apps/web-antd`、其他示例应用或全仓库的强制设计规范。
- **Don't** 把默认配置输入当成浏览器最终计算值，也不要把基础组件尺寸强加给 Ant。
- **Don't** 将“美观、大方、简洁、减少模板感”的改进方向写成已经逐页完成的验收结论。
