# 商城前端布局与排版覆盖范围

本次目标是统一实际运行的 `micro-server-own-web-vben/apps/web-antd` 应用框架和所有启用页面。仅修改若干页面的局部样式、或只验证工作台，不足以说明全站完成。

## 统一规范

- 应用入口加载一份公共样式；正文、标题、页面间距、面板、表格、表单及挂载在 body 下的弹出层使用同一套规范。
- 内容最大宽度 1600px，页面标题 18–22px，正文与表单 13px，常规控件高度 36px；表格在自己的容器内横向滚动。
- 双栏内容在中等宽度下堆叠；窄屏工具栏换行，抽屉不超过视口宽度，登录页面在横屏时可滚动。
- 默认浅色，支持暗色；保留键盘焦点和减少动态效果偏好。
- 品牌资源随前端打包，登录、主框架、公共提示页、异常页保持统一。

## 实际路由清单

路由唯一来源为 `apps/web-antd/src/router/routes/modules/marketplace.ts`，当前有 **25 条业务路由，复用 21 个业务页面文件**。

| 业务端 | 路由 | 页面 |
| --- | --- | --- |
| 买家 | `/buyer/workspace` | 工作台 |
| 买家 | `/buyer/catalog` | 商品浏览与详情/评价抽屉 |
| 买家 | `/buyer/favorites` | 收藏及商品抽屉 |
| 买家 | `/buyer/cart` | 购物车 |
| 买家 | `/buyer/coupons` | 优惠券 |
| 买家 | `/buyer/orders` | 订单、物流与支付状态 |
| 买家 | `/buyer/after-sales` | 售后申请与进度 |
| 买家 | `/buyer/addresses` | 地址簿及编辑抽屉 |
| 买家 | `/buyer/account` | 资料与会话 |
| 买家 | `/buyer/checkout` | 结算（菜单外入口） |
| 商家 | `/merchant/workspace` | 工作台 |
| 商家 | `/merchant/products` | 商品发布、编辑及列表 |
| 商家 | `/merchant/price-audits` | 价格审计 |
| 商家 | `/merchant/reviews` | 评价与回复 |
| 商家 | `/merchant/inventory` | 库存与预警 |
| 商家 | `/merchant/orders` | 发货履约 |
| 商家 | `/merchant/after-sales` | 售后处理 |
| 商家 | `/merchant/coupons` | 优惠券模板 |
| 商家 | `/merchant/freight` | 运费 |
| 商家 | `/merchant/settlement` | 余额与提现 |
| 商家 | `/merchant/account` | 资料与会话 |
| 系统 | `/system/workspace` | 工作台 |
| 系统 | `/system/accounts` | 账号与商家授权 |
| 系统 | `/system/roles` | 角色功能授权 |
| 系统 | `/system/reviews` | 评价治理 |

公共范围还包括侧栏/顶栏/页签/用户菜单、登录与 4 个认证提示页、关于平台弹窗、403/404/500/离线/待上线状态组件。未实现的短信、扫码、注册与找回密码功能继续明确提示未开放。

## 未启用的模板与其他应用

`routes/index.ts` 只加载商城路由模块。`views/dashboard`、`views/demos`、`views/_core/profile` 及对应 Vben 示例路由未启用，未为它们接入模拟业务；它们不能计作商城功能交付。本轮不把这些上游模板逐个重写称为“全站优化”，覆盖标准是应用级公共框架加上述全部实际入口。

Vben monorepo 的其他 UI 库示例应用、仓库中未跟踪的 `micro-server-own-web/` 原型也不属于当前运行的商城前端。

## 可重复的验收

在 `micro-server-own-web-vben` 中启动 `@vben/web-antd`，端口使用 5180，然后运行：

```sh
node scripts/qa/mall-layout-smoke.mjs
```

需本地已安装 Playwright Chromium；可用 `PLAYWRIGHT_CHROMIUM_EXECUTABLE` 指定本机浏览器路径。`MALL_UI_URL` 可指定另一本地端口，`MALL_UI_OUTPUT` 可指定截图目录。测试只允许 localhost/127.0.0.1，所有业务请求由隔离夹具拦截，不接触真实账号或数据库。

脚本从真实路由模块读取全部业务路由，在 1440、1024、390px 三种宽度及浅/暗主题下检查容器边界、页面挂载、错误提示和浏览器异常；另查登录替代页、抽屉、表单校验、关于弹窗和 404 返回。截图及逐路由结果输出至临时目录。它验证布局与交互，不代替支付、提现或其他接口的真实业务验收。

## 本轮验收结果（2026-10-02）

- 全部 25 条业务路由在 1440、1024、390px 三种宽度及浅/暗主题下通过，共 150 组页面组合；未发现页面级横向溢出、容器越界、意外接口错误或浏览器未捕获异常。
- 登录及认证提示页通过 15 组宽屏、手机和横屏检查；目录详情抽屉、地址抽屉和必填校验、关于弹窗、侧栏/工作台入口导航、404 返回工作台均通过。
- `vue-tsc --noEmit --skipLibCheck`、本次变更文件 ESLint 检查及 `pnpm run build:antd` 均通过。
- 浏览器测试使用 localhost 服务和测试夹具，不连接后端真实服务，不验证支付、提现等业务 API 的真实写入行为。
- 可复核截图与结果 JSON 位于运行时临时目录 `/private/tmp/mall-ui-work-20261002/final`，不会作为项目源码提交。
