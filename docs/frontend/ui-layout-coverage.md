# 商城前端布局与排版覆盖范围

本记录覆盖实际运行的 `micro-server-own-web-vben/apps/web-antd` 应用框架和所有启用页面。下文 2026-10-09/10 的结果是历史验收；2026-10-22 的视觉改造与验证另列在文末，不能用历史通过结果替代本轮检查。

## 统一规范

- 应用入口加载一份公共样式；正文、标题、页面间距、面板、表格、表单及挂载在 body 下的弹出层使用同一套规范。
- 内容最大宽度 1680px，页面主标题按视口为 22–28px，正文与表单一般为 13–14px，常规控件高度 36px；表格只在自己的容器内横向滚动。
- 双栏内容在中等宽度下堆叠；窄屏工具栏换行，抽屉不超过视口宽度，登录页面在横屏时可滚动。
- 默认浅色，支持暗色；保留键盘焦点和减少动态效果偏好。
- 品牌资源随前端打包，登录、主框架、公共提示页、异常页保持统一。

## 实际路由清单

路由唯一来源为 `apps/web-antd/src/router/routes/modules/marketplace.ts`，当前有 **25 条业务路由，复用 21 个业务页面文件**。

| 业务端 | 路由 | 页面 | 本轮结果 |
| --- | --- | --- | --- |
| 买家 | `/buyer/workspace` | 工作台 | 通过 |
| 买家 | `/buyer/catalog` | 商品浏览与详情/评价抽屉 | 通过 |
| 买家 | `/buyer/favorites` | 收藏及商品抽屉 | 通过 |
| 买家 | `/buyer/cart` | 购物车 | 通过 |
| 买家 | `/buyer/coupons` | 优惠券 | 通过 |
| 买家 | `/buyer/orders` | 订单、物流与支付状态 | 通过 |
| 买家 | `/buyer/after-sales` | 售后申请与进度 | 通过 |
| 买家 | `/buyer/addresses` | 地址簿及编辑抽屉 | 通过 |
| 买家 | `/buyer/account` | 资料与会话 | 通过 |
| 买家 | `/buyer/checkout` | 结算（菜单外入口） | 通过 |
| 商家 | `/merchant/workspace` | 工作台 | 通过 |
| 商家 | `/merchant/products` | 商品发布、编辑及列表 | 通过 |
| 商家 | `/merchant/price-audits` | 价格审计 | 通过 |
| 商家 | `/merchant/reviews` | 评价与回复 | 通过 |
| 商家 | `/merchant/inventory` | 库存与预警 | 通过 |
| 商家 | `/merchant/orders` | 发货履约 | 通过 |
| 商家 | `/merchant/after-sales` | 售后处理 | 通过 |
| 商家 | `/merchant/coupons` | 优惠券模板 | 通过 |
| 商家 | `/merchant/freight` | 运费 | 通过 |
| 商家 | `/merchant/settlement` | 余额与提现 | 通过 |
| 商家 | `/merchant/account` | 资料与会话 | 通过 |
| 系统 | `/system/workspace` | 工作台 | 通过 |
| 系统 | `/system/accounts` | 账号与商家授权 | 通过 |
| 系统 | `/system/roles` | 角色功能授权 | 通过 |
| 系统 | `/system/reviews` | 评价治理 | 通过 |

公共范围检查结果：

| 公共入口 | 本轮结果 |
| --- | --- |
| 侧栏、顶栏、页签、用户菜单及主题切换 | 通过（全路由主题/视口矩阵及导航交互） |
| 账号密码登录及验证码、扫码、注册、找回密码提示页 | 通过（20 项；后 4 项保留未开放提示） |
| 关于平台弹窗 | 通过（桌面、平板和手机视口） |
| 404 页面 | 通过（4 种视口；亮色模式） |
| 403 无权访问组件 | 通过（测试运行时临时注册路由；4 种视口 × 亮/暗主题） |
| 500 服务异常组件 | 通过（测试运行时临时注册路由；4 种视口 × 亮/暗主题） |
| 离线状态组件 | 通过（测试运行时临时注册路由；4 种视口 × 亮/暗主题） |
| 待上线状态组件 | 通过（测试运行时临时注册路由；4 种视口 × 亮/暗主题） |

未实现的短信、扫码、注册与找回密码功能继续明确提示未开放。

## 未启用的模板与其他应用

`routes/index.ts` 只加载商城路由模块。`views/dashboard`、`views/demos`、`views/_core/profile` 及对应 Vben 示例路由未启用，未为它们接入模拟业务；它们不能计作商城功能交付。本轮不把这些上游模板逐个重写称为“全站优化”，覆盖标准是应用级公共框架加上述全部实际入口。

Vben monorepo 的其他 UI 库示例应用、仓库中未跟踪的 `micro-server-own-web/` 原型也不属于当前运行的商城前端。

## 可重复的验收

在 `micro-server-own-web-vben` 中启动 `@vben/web-antd`，端口使用 5180，然后运行：

```sh
node scripts/qa/mall-layout-smoke.mjs
```

需本地已安装 Playwright Chromium；可用 `PLAYWRIGHT_CHROMIUM_EXECUTABLE` 指定本机浏览器路径。`MALL_UI_URL` 可指定另一本地端口，`MALL_UI_OUTPUT` 可指定截图目录。测试只允许 localhost/127.0.0.1，所有业务请求由隔离夹具拦截，不接触真实账号或数据库。

脚本从真实路由模块读取全部业务路由，在 1440、1024、390、320px 四种宽度及浅/暗主题下检查容器边界、页面挂载、实际主题颜色、错误提示和浏览器异常；另查登录替代页、抽屉、表单校验、关于弹窗、商品关键词搜索/重置/分页、售后状态筛选/分页、404 返回，以及三种角色工作台的唯一入口和目标路由。截图及逐路由结果输出至临时目录。它验证布局与交互，不代替支付、提现或其他接口的真实业务验收。

## 本轮验收结果（2026-10-09）

- 全部 25 条业务路由在 1440、1024、390、320px 四种宽度及浅/暗主题下通过，共 200 组页面组合；未发现页面级横向溢出、容器越界、意外接口错误或浏览器未捕获异常。
- 上表每条路由均逐项完成视觉/布局审查并通过全矩阵冒烟检查；认证提示页与公共框架状态见上表。
- 登录及认证提示页通过 20 组宽屏、手机和横屏检查；目录详情抽屉、地址抽屉和必填校验、关于弹窗、侧栏导航、工作台 15 个任务入口及其目标路由、404 返回工作台均通过。
- 商品关键词筛选、重置与分页以及商家售后状态筛选、分页的请求参数断言通过；暗色空状态插图对比度修复通过浏览器检查。
- 买家、商家与系统工作台统一为不重复的快捷入口；桌面和手机各角色页面经截图复核，角色上下文和流程入口不再重复展示。
- `vue-tsc --noEmit --skipLibCheck`、生产构建、`node --check scripts/qa/mall-layout-smoke.mjs` 及 `git diff --check` 均通过。
- 浏览器测试使用 localhost 服务和测试夹具，不连接后端真实服务，不验证支付、提现等业务 API 的真实写入行为。
- 全量结果包含 `errors: []`、`unexpected: []`；116 张截图（其中每条业务路由的桌面/手机浅/暗主题截图共 100 张）及 `results.json` 位于 `/private/tmp/mall-ui-verification-20261009`，不会作为项目源码提交。
- 本地开发过程中曾观察到 `ResizeObserver loop completed with undelivered notifications`；本条在 2026-10-10 专项诊断中复现，详见补充记录。

## 补充验收（2026-10-10）

`scripts/qa/mall-fallback-smoke.mjs` 在已启动的应用实例中，以浏览器内存中的临时路由挂载原有 403、500、离线和待上线 Vue 组件；它不修改正式路由表或业务接口。测试仍由 `mall-layout-fixtures.mjs` 拦截业务请求。四个组件各在 1440、1024、390、320px 与亮/暗主题下通过，共 32 组，包含标题、操作按钮、主题颜色、横向溢出及“返回工作台”交互检查。2026-10-10 重跑结果为 32/32 通过，`errors: []`、`unexpected: []`；32 张截图及 `results.json` 位于 `/private/tmp/mall-fallback-verification-20261010-rerun`。

隔离后端写入验收的环境检查：运行 `python3 scripts/local-acceptance.py` 在启动容器前以退出码 1 结束，报错为 `Docker CLI is required; install Docker Desktop or Docker Engine with Compose v2`。本机也未发现 Podman/Colima、MySQL/MariaDB 服务，或对应数据库、Nacos、商城服务监听端口。以下各项均未向真实后端发出请求，因此没有可验证的服务响应、页面成功/失败反馈或持久化结果。

对现有 `scripts/marketplace-e2e.py` 的静态检查显示，它包含商品创建、优惠券模板创建与领券、结算下单/模拟支付、发货、售后退款和商家账号授权；不包含商品编辑、优惠券模板编辑、运费规则、提现和角色功能授权，也不操作前端页面。因此这些代码路径只能作为待运行的 API 验收基础，不能算作本次任何一项真实验收通过。

| 写入流程 | 目标请求 | 本次状态 | 请求、响应与数据证据 |
| --- | --- | --- | --- |
| 商品发布／编辑 | `POST /product/api/v1/merchant/products`、`PUT /product/api/v1/merchant/products/{id}` | 未验证（阻塞） | 隔离服务未启动；请求未发出，响应、页面反馈及数据结果未验证 |
| 结算下单 | `POST /order/api/v1/orders` | 未验证（阻塞） | 同上 |
| 买家申请与商家处理售后 | `POST /order/api/v1/after-sales`、`POST /order/api/v1/after-sales/{no}/audit` | 未验证（阻塞） | 同上 |
| 商家发货 | `POST /order/api/v1/sub-orders/{no}/ship` | 未验证（阻塞） | 同上 |
| 商家优惠券模板 | `POST /sale/api/v1/coupons/merchant/templates`、`PUT /sale/api/v1/coupons/merchant/templates/{id}` | 未验证（阻塞） | 同上 |
| 商家运费规则 | `PUT /order/api/v1/merchant/freight-rule` | 未验证（阻塞） | 同上 |
| 商家提现申请 | `POST /settlement/api/v1/merchant/settlement/withdrawals` | 未验证（阻塞） | 同上；仅涉及现有模拟结算能力，不代表真实付款 |
| 系统账号商家授权 | `PUT /user/api/v1/system/accounts/{id}/merchant-role` | 未验证（阻塞） | 同上 |
| 系统角色功能授权 | `PUT /user/api/v1/system/roles/{id}/functions` | 未验证（阻塞） | 同上 |

解除环境阻塞后，在一次性 Compose 项目和合成测试账号下重新执行上述写入，并逐项保存请求、响应、页面提示与读回状态；不得将浏览器布局夹具的返回值标记为后端验收通过。

`ResizeObserver` 诊断状态：**通过复现与影响检查，未发现可见布局/交互缺陷；未改应用页面行为**。复现脚本为 `scripts/qa/mall-resize-observer-diagnostic.mjs`，仅通过浏览器内存的登录、商品和评价布局夹具访问本地开发服务。阶段化结果显示，1440、1024、390、320px 均在“打开详情抽屉”阶段各记录 1 条 `ResizeObserver loop completed with undelivered notifications`；登录/工作台及目录初始加载阶段均为 0。捕获到的时间关联观察目标是 Ant Design Vue `vc-table` 的测量行 `<td>` 与抽屉内 `.ant-table`；其 `MeasureCell` resize 回调会报告列宽，完整表格 resize 回调会更新表格宽度。浏览器通用 loop-limit 事件不提供唯一观察器 ID，因此这里只能确认阶段及时间关联目标，不能声称某个回调已被证明为唯一原因。

影响检查：随后运行 `MALL_UI_ONLY_ACTOR=buyer node scripts/qa/mall-layout-smoke.mjs`，在四种视口及亮/暗主题检查买家 10 条路由，共 80 组；另执行 20 项认证检查。抽屉边界、横向溢出、表单校验、导航检查均通过，`errors: []`、`unexpected: []`，并捕获到每个视口各 1 条同类浏览器错误事件。此次未发现可见错位或交互失败，因此不改业务页面。专项复现只捕获到浏览器 `window.error` 事件；脚本内的 `console.error`/`console.warn` 钩子与 Playwright 控制台监听均未捕获匹配消息（1440px 单视口复核为 `consoleMessages: []`）。当前线程没有附加 Vite 终端，因此不能证实该行由 Vite 进程输出。它不是 Vite 模块加载异常；现有证据也不足以宣称其他浏览器或生产环境绝不会记录同类通知。

分阶段证据为 `/private/tmp/mall-resize-observer-phased-20261010/diagnostic.json`；四视口买家矩阵为 `/private/tmp/mall-resize-observer-layout-20261010/results.json`，截图位于同目录；此前无追踪完整 200 组结果在 `/private/tmp/mall-layout-recheck-20261010/results.json`。测试使用本地浏览器和非写入布局夹具，不连接真实业务后端。

独立剩余风险：带追踪的全量重跑完成 164 组后，开发服务器出现一次 `Failed to fetch dynamically imported module: .../src/bootstrap.ts`，随后页面挂载超时；该轮结果为**未通过**，不得用于证明余下 36 组。错误发生后本地 `bootstrap.ts` 端点返回 HTTP 200，原因尚未定位。此前未加追踪的完整 200 组检查及 32 组状态组件检查均通过；生产构建结果单独记录，不把开发态偶发导入失败解释为构建失败。

本轮验证：`mall-resize-observer-diagnostic.mjs`、`mall-layout-smoke.mjs` 与 `mall-fallback-smoke.mjs` 的 Node 语法检查通过；分阶段警告诊断和买家四视口矩阵均按上文证据完成。使用项目兼容的 Node/pnpm 运行 `pnpm --filter @vben/web-antd typecheck` 与 `pnpm --filter @vben/web-antd build`，两项本轮均退出 0；完整无追踪浏览器矩阵为 200 组业务路由组合、20 组认证检查，`errors: []`、`unexpected: []`；状态组件矩阵本轮重跑 32 组，`errors: []`、`unexpected: []`。真实后端写入仍按上表保持未验证。

## 全站视觉改造验收（2026-10-22）

范围仍为实际启用的 Vue 3/Vben/Ant Design Vue `web-antd` 商城，不包括未启用的 Vben 示例应用、旧原型或后端改造。本轮将公共页面间距、标题/正文层级、中文排版、控件和亮暗色角色统一到现有 `src/styles/index.css`、`src/preferences.ts`、`src/app.vue`；默认使用中性底色、深石青操作色，保留用户持久化的自定义主色。按“先商家商品核心页验收，再买家、商家其他页、系统、认证与异常页”的顺序实施，页面源码由 Antigravity CLI 编写；系统页 CLI 权限中断、异常页 CLI 流中断后由 Codex 接管复核与必要的原范围修改。各任务的差异、验证和限制记录在 `work/agent-tasks/frontend-craft-*-review-20261022.md` 或对应 handoff 文件。

本轮最终用本地 UI 演示身份逐条进入 **25 条实际业务路由**，每条检查 1440×900、390×844 及亮/暗主题共 **4 组**，合计 **100 组**。核对了实际路径、主标题、主题状态和页面级水平溢出；25 条路由均挂载正确，未见页面级水平溢出。该矩阵是浏览器布局/导航证据，**不证明**真实业务 API 成功。部分页面因本地网关未连接呈现真实请求失败态；没有用前端假响应填充数据。代表性稳定态截图位于 `/private/tmp/frontend-craft-20261022/`；对于重拍过的券包、结算与 404，以文件名包含 `settled` 的图为准，早期捕获到加载遮罩的帧不是完成证据。

| 路由 | 2026-10-22 布局结果 | 实际检查重点 |
| --- | --- | --- |
| `/buyer/workspace` | 通过（4/4） | 角色入口与快捷操作层级 |
| `/buyer/catalog` | 通过（4/4） | 搜索栏、列表失败态与手机排版 |
| `/buyer/favorites` | 通过（4/4） | 收藏列表失败态 |
| `/buyer/cart` | 通过（4/4） | 购物车失败态与导航 |
| `/buyer/coupons` | 通过（4/4） | 券包请求失败时不再显示空表和无意义滚动轨 |
| `/buyer/orders` | 通过（4/4） | 订单/物流区失败态 |
| `/buyer/after-sales` | 通过（4/4） | 售后申请入口、列表与进度区 |
| `/buyer/addresses` | 通过（4/4） | 地址列表及新增入口 |
| `/buyer/account` | 通过（4/4） | 资料、会话与危险操作分区 |
| `/buyer/checkout` | 通过（4/4） | 无商品空态、条件文案与购物车返回 |
| `/merchant/workspace` | 通过（4/4） | 商家快捷操作层级 |
| `/merchant/products` | 通过（4/4） | 商品列表错误态、发布/编辑入口 |
| `/merchant/price-audits` | 通过（4/4） | 审计筛选与操作区 |
| `/merchant/reviews` | 通过（4/4） | 评价筛选、详情/回复入口 |
| `/merchant/inventory` | 通过（4/4） | 库存筛选与失败态 |
| `/merchant/orders` | 通过（4/4） | 发货操作区与列表失败态 |
| `/merchant/after-sales` | 通过（4/4） | 售后筛选及进度布局 |
| `/merchant/coupons` | 通过（4/4） | 模板列表/表单与错误态 |
| `/merchant/freight` | 通过（4/4） | 运费规则表单与不可用状态 |
| `/merchant/settlement` | 通过（4/4） | 余额、账单与提现操作层级 |
| `/merchant/account` | 通过（4/4） | 资料与会话布局 |
| `/system/workspace` | 通过（4/4） | 系统治理入口 |
| `/system/accounts` | 通过（4/4） | 角色授予表单与风险说明 |
| `/system/roles` | 通过（4/4） | 功能授权表单 |
| `/system/reviews` | 通过（4/4） | 举报处置/评价治理区 |

认证页另以真实未登录状态检查 `/auth/login`、`/auth/code-login`、`/auth/qrcode-login`、`/auth/forget-password`、`/auth/register`：各 1440×900/390×844 × 亮/暗，共 **20/20 组**；逐组核对路径、主题和水平溢出。登录空表单的用户名/密码必填提示通过；商家 UI 演示入口进入 `/merchant/workspace`，退出后回登录页。演示页面持续标注“未连接真实业务网关”，四种未开放认证方式仍仅展示说明。截图 `/private/tmp/frontend-craft-20261022/after-auth-{route}-{width}-{theme}-verified.png`。

正式 404 路由已去除通用大插画，改为紧凑状态提示；桌面/手机亮暗四组无水平溢出，买家身份点击“返回工作台”进入 `/buyer/workspace`。403、500、离线、建设中组件共享新样式但仍**未挂载正式路由**：本轮仅做源码、类型与构建检查，未重新执行其运行态浏览器截图，故本轮状态为“运行态未验证”，不能沿用 2026-10-10 旧版组件的 32/32 结果。没有为验收新增正式业务路由。

关键交互证据：商家商品发布/编辑抽屉及必填提示、价格审计 ID 校验、商家售后筛选、优惠券表单校验、运费与提现在缺少真实前置数据时保持不可用反馈、系统账号空表单校验、登录演示导航、结算无商品返回购物车，以及 404 返回工作台均在各任务复核中执行。多数带真实数据的写入仍受隔离后端未启动阻塞；本轮不把前端演示身份、失败态或静态代码检查说成商品发布、结算、发货、提现等真实业务成功。

最终 `pnpm --filter @vben/web-antd typecheck`、`pnpm --filter @vben/web-antd build`、`git diff --check` 退出 0；改动源码未见冲突标记，所查页面/组件非生成源文件均未超过 500 行。没有提交或推送。2026-10-09 的 1024/320px 全矩阵与 2026-10-10 的四类未挂载状态组件测试属于**旧版视觉**历史结果，本轮未重复运行，不能称为本轮通过。
