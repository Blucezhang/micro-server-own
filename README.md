# micro-server-own

> 基于 Spring Cloud 的多商家商城后端演示项目

## 项目定位

micro-server-own 是一个学习型、演示型的多商家商城后端。它将用户、商品、促销、库存、订单、文件、消息和工作流模块组织为一条交易链路：

> 买家浏览商品 → 加入购物车 → 选择地址和优惠券 → 试算 → 多商家拆单与锁库存 → 模拟支付 → 商家发货 → 买家签收 → 售后退款。

项目适合学习微服务拆分、库存预占、订单状态机、幂等处理、Outbox、JWT/RBAC 和容器化配置。

## 项目概览

| 项目 | 说明 |
| --- | --- |
| 项目类型 | 多商家商城后端；不包含前端页面 |
| 开发语言 | Java 8 |
| 核心框架 | Spring Boot 1.5.9、Spring Cloud Edgware |
| 数据存储 | MySQL 5.7、Neo4j 3.5 |
| 网关地址 | http://localhost:9632 |
| 本地启动 | [快速开始](#快速开始) |
| 能力路线 | [商城能力路线图](docs/product/mall-capability-roadmap.md) |

开始前请先了解两条边界：

1. `/user/login/token` 使用 BCrypt 校验账号密码并签发短时 HS256 Access JWT 与可轮换 Refresh Token；网关默认启用 JWT，会忽略客户端提交的 `X-Actor-*`，而以令牌声明重建身份头。登录、刷新/退出会话、使用旧密码的 `/user/login/updatePassword`、商品公开浏览和模拟渠道回调是受限匿名入口，其余业务接口须携带 Bearer JWT。
2. own-settlement 目前是本地模拟支付与退款账本；微信、支付宝及商家结算参数已经预留，但不连接真实渠道。
3. Docker Compose 仅把网关 `9632` 暴露给外部网络；Eureka、Config Server 仅绑定到宿主机 `127.0.0.1` 用于本地排障，各业务服务仅在 Compose 后端网络监听，不能作为业务入口。网关会覆盖来访者提交的内部令牌并注入受保护的共享令牌；业务服务启用服务边界校验后拒绝缺少该令牌的请求，从而不能通过直连伪造 `X-Actor-*`。

## 版本与技术栈

| 项目版本 | Java | Spring Boot | Spring Cloud | 状态 |
| --- | --- | --- | --- | --- |
| master / 1.0-ALPHA | 8 | 1.5.9.RELEASE | Edgware.SR6 | 当前维护分支 |

> Spring Boot 1.5.9、Spring Cloud Edgware 和 Neo4j 3.5 均为旧技术栈。升级需要独立完成兼容性设计、回归测试和数据迁移。

## 目录

- [项目定位](#项目定位)
- [项目概览](#项目概览)
- [当前实现摘要](#当前实现摘要)
- [业务模型](#业务模型)
- [功能清单](#功能清单)
- [服务与路由](#服务与路由)
- [技术栈与版本治理](#技术栈与版本治理)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [生产部署](#生产部署)
- [验证与验收](#验证与验收)
- [许可证](#许可证)

## 当前实现摘要

当前代码库已形成以多商家交易为主线的后端演示闭环。下表是“现在可以从代码和本地测试确认”的能力摘要；后续章节保留各接口、配置和演示命令的完整说明。

| 范围 | 当前功能 | 实现边界 |
| --- | --- | --- |
| 账号与权限 | 匿名买家注册、BCrypt 登录、HS256 Access JWT、Refresh Session 轮换/撤销、登录失败锁定、个人资料与会话管理 | 网关以 JWT 覆写身份头；尚未接入 OAuth、短信/邮箱验证或工作负载身份 |
| 角色管理 | `ROLE_BUYER`、`ROLE_MERCHANT`、`ROLE_SYSTEM` 路由 RBAC；SYSTEM 可授予商家角色并撤销目标账号 Refresh Session；可向图谱角色追加功能权限 | 商家角色与功能关系保存于既有 Neo4j 图谱；管理接口要求 SYSTEM 身份与 `Idempotency-Key` |
| 商品与运营 | Product 作为可售 SKU、商家商品创建/编辑/上下架、公开浏览与筛选、价格审计、买家收藏、评价/回复/举报/审核 | 未引入 SPU/SKU 双层模型或前端页面 |
| 买家交易 | 地址簿、购物车、实时试算、多商家拆单、库存预占、优惠券领取/占用/核销、订单查询、发货、物流轨迹、签收与自动签收 | 价格、商家归属或可售状态变化会阻止直接下单；订单保存商品、优惠、运费和地址快照 |
| 商家运营 | 固定运费与免邮门槛、库存人工调整及台账、低库存阈值、子订单工作台、店铺券模板与汇总 | 库存服务是实时库存唯一准绳；订单预占和人工调整均有边界校验 |
| 可靠性与可观测性 | 写接口幂等、库存行锁、15 分钟未支付关闭、关联 ID、订单 Outbox、基础 Actuator 指标、敏感地址访问审计 | Outbox 的 `PENDING` 只代表持久化待投递；Webhook 需 HTTPS、至少 32 字节密钥和接收端验签去重 |
| 文件、消息与工作流 | 归属文件上传/提升/私有下载、售后举证文件校验；短信/邮件/推送适配接口；既有工作流查询与流转接口 | 消息客户端只接受明确的 `data=success`，不把 HTTP 200 误报为投递成功；未配置真实渠道时不得视为已送达 |
| 本地模拟资金流程 | 本地模拟支付、售后退款账本、商家应收与模拟结算相关代码仍保留 | 不连接真实微信/支付宝；本轮不继续扩展支付回调、退款、对账或资金划拨 |

本次收尾还完成了以下质量修正：根 Maven 父 POM 统一管理内部模块、插件与显式依赖版本；消息正文、手机号和邮箱不再进入常规控制器/消息日志；消息与工作流中的无效请求会返回明确错误，而不是静默成功或输出标准打印。

**最近本地验证。** 使用 JDK 8 执行 `./mvnw -B -ntp clean verify` 已通过全部 14 个模块；`scripts/validate-migrations.sh` 已验证 MySQL 迁移 01–33；`git diff --check` 通过。这些结果不等同于真实 Docker、MySQL、Neo4j、短信/邮件、物流或生产环境验收。

## 业务模型

### 业务边界

项目以多商家交易闭环为核心：商品以 Product 作为可售 SKU；买家从地址簿选择收货地址；订单按商家拆分为子订单；库存服务是实时库存唯一准绳；订单保存商品、价格、优惠、运费与地址快照。

| 角色 | 可以做什么 |
| --- | --- |
| 买家 BUYER | 浏览商品、管理地址、加购、领券、试算、下单、支付、确认收货、申请售后 |
| 商家 MERCHANT | 管理自己的 SKU、运费和库存，查询自己的子订单，发货与审核售后 |
| 系统 SYSTEM | 初始化库存、模拟支付回调、查询本地 Outbox 与敏感访问审计 |

SYSTEM 可通过 `PUT /user/api/v1/system/accounts/{loginUserId}/merchant-role` 为指定账号持久授予 `ROLE_MERCHANT`。该写操作必须携带 `Idempotency-Key`，并会撤销目标账号全部 Refresh Session；目标账号须重新登录或刷新令牌后才能取得商家身份声明。此接口只能由 `ROLE_SYSTEM` 的 SYSTEM 令牌调用，不能由买家或商家自行提升权限。

SYSTEM 还可通过 `PUT /user/api/v1/system/roles/{roleId}/functions` 为既有角色追加功能图谱权限，正文为 `{"functionIds":[3,4]}`。角色与功能必须存在；重复 ID 或重复请求不会重复创建图谱边，且该接口同样要求 `Idempotency-Key`。

### 交易规则

- 每个写请求必须带 Idempotency-Key；相同 actor、路由、请求体和幂等键在 24 小时内重放同一成功结果。
- 母订单：PENDING_PAYMENT → PAID → FULFILLING → COMPLETED；子订单：PENDING_PAYMENT → TO_SHIP → SHIPPED → RECEIVED。
- 库存预占、优惠券占用和待支付订单默认 15 分钟到期；商品变化时下单返回 409 PRODUCT_CHANGED；模拟支付成功、失败及整单退款在同一支付单行锁内串行推进，已取消订单的迟到支付回调会返回 409，不能重新激活订单。
- 平台券每个母订单最多一张，店铺券每个商家子订单最多一张；优惠不抵扣运费。试算只校验并计算优惠，不占用券；创建订单时才预占。
- 订单事件写入本地 Outbox。PENDING 仅表示待投递，不能解释为外部系统已送达。

## 功能清单

标记说明：✅ 已实现并有本地测试覆盖；🧪 仅本地模拟或依赖外部配置；🚧 尚未实现。

### 商城交易

| 状态 | 功能 | 说明 |
| --- | --- | --- |
| ✅ | 商品与 SKU | 商家创建、编辑、上下架自有商品；公开分页浏览、关键词/分类/商家筛选；买家可收藏自有可售商品；已签收买家可评价一次，SYSTEM 可维护评价禁用词库 |
| ✅ | 购物车与试算 | 买家购物车、商品实时重校验、多商家拆单金额试算 |
| ✅ | 地址簿 | 买家维护地址和默认地址；订单仅保存地址快照 |
| ✅ | 优惠券 | 平台券、店铺券、领取、占用、核销、释放和到期回收；首领券库存与用户券占用加锁，同一请求不能重复计价同一张券 |
| ✅ | 库存 | 仅创建缺失库存行的幂等目录初始化、预占、确认扣减、释放、退款回补、人工调整审计和商家低库存阈值预警查询 |
| ✅ | 订单履约 | 母子订单、商家发货、物流轨迹、自动收货、商家订单工作台 |
| 🧪 | 支付与整单退款 | `MOCK_WECHAT`、`MOCK_ALIPAY` 本地渠道单、模拟成功/失败回调与整单退款；不连接真实支付渠道 |
| 🧪 | 售后与换货 | 仅退款、退货退款、同 SKU 同数量换货、退款账本与退货/换货库存台账；不支持券分摊或跨 SKU 换货 |
| 🧪 | 模拟商家结算 | 支付成功按商家子订单归集应收（商家 80%、平台 20%）；7 天后结算，商家余额大于 100 元可申请模拟提现；退款直接冲回应收，已提现时形成待追偿余额 |
| 🚧 | 真实支付与商家结算 | 微信/支付宝验签、对账、分账、抽佣、真实提现付款与财务总账 |
| ✅ | JWT 与角色授权 | BCrypt 登录、HS256 JWT、网关身份头覆写及路由 RBAC；旧 Neo4j 角色/功能图谱进入令牌声明，`ROLE_BUYER`、`ROLE_MERCHANT`、`ROLE_SYSTEM` 分别限制交易、运营与系统操作，显式功能权限可补充对应动作 |
| 🚧 | 前端与 OAuth/服务间认证 | Web/小程序前端、OAuth、服务间 mTLS/工作负载身份和细粒度资源策略 |

### 基础服务

| 状态 | 功能 | 说明 |
| --- | --- | --- |
| ✅ | 网关与注册发现 | Zuul 网关、Eureka 注册发现、Config Server Native 配置 |
| ✅ | 可靠性 | 幂等记录、库存行锁、订单超时关闭、Outbox、受约束的关联 ID 透传、基础 Actuator 指标 |
| ✅ | 文件服务 | 临时上传、文件提升、路径校验与永久文件存储；新建的归属文件可绑定买家/商家并用于售后举证 |
| 🧪 | 消息服务 | 短信、邮件、极光推送接口；需要各渠道凭据和真实环境验证。请求日志不记录收件地址、手机号或消息正文，SMTP 协议调试默认关闭；短信 SOAP 请求使用 UTF-8 并在完成或失败后关闭流和连接。调用方只有在统一响应包明确返回 `data=success` 时才视为已受理，HTTP 成功但业务返回 `failed` 不会被伪装成投递成功 |
| 🧪 | 工作流 | 保留既有工作流模块；订单 Outbox 默认不投递到外部工作流。流程详情查询要求正数 `processId` 与 `bizType`，无效请求返回 `422`，不会继续进入查询层 |
| 🚧 | 生产运维 | 集群配置、集中日志、链路追踪、共享限流、告警、容灾与安全基线 |

公共控制器切面仅记录请求方法、URL 和参数数量，不会序列化控制器参数；关联 ID 由过滤器写入日志上下文和响应头。服务间 HTTP 调试日志及短信、邮件更新/查询日志不输出请求体、正文、手机号或邮箱地址。生产日志采集仍应设置访问日志脱敏规则，并限制日志系统的读取权限。

## 服务与路由

| 模块 | 端口 | 说明 |
| --- | ---: | --- |
| own-common | - | 公共 DTO、异常、JWT、交易请求头、幂等基础能力 |
| own-config | 8001 | Config Server；config-repo 保存本地演示配置 |
| own-eureka-server | 8002 | Eureka 服务注册中心 |
| own-api-gateway | 9632 | API 网关、服务路由、单实例内存限流 |
| own-user-party | 6006（仅容器内部） | 用户、组织、角色、地址簿 |
| own-product | 6007（仅容器内部） | 商品/SKU、商家商品运营、公开浏览 |
| own-promotion | 6005（仅容器内部） | 促销、券模板、用户券 |
| own-inventory | 6009（仅容器内部） | 库存与库存预占台账 |
| own-order | 6010（仅容器内部） | 购物车、试算、订单、物流、售后、Outbox |
| own-settlement | 6011（仅容器内部） | 本地模拟支付、退款与售后退款账本 |
| own-file | 6004（仅容器内部） | 文件上传、下载与存储提升 |
| own-send-server | 6003（仅容器内部） | 短信、邮件、推送适配接口 |
| own-workflow | 6008（仅容器内部） | 既有工作流模块 |

### 网关路由

| 前缀 | 服务 | 例子 |
| --- | --- | --- |
| /product/** | 商品 | GET /product/api/v1/categories、GET /product/api/v1/products、GET /product/api/v1/products/{id} |
| /user/** | 用户与地址 | POST /user/api/v1/addresses |
| /sale/** | 促销与券 | POST /sale/api/v1/coupons/claim |
| /inventory/** | 库存 | GET /inventory/api/v1/stocks/{productId} |
| /order/** | 订单与售后 | POST /order/api/v1/orders |
| /settlement/** | 模拟结算 | POST /settlement/api/v1/payments |
| /file/**、/flow/**、/Info/** | 原有服务 | 保持原有接口定义 |

## 技术栈与版本治理

| 技术 | 用途 | 当前版本/实现 |
| --- | --- | --- |
| Java | 运行时 | 8 |
| Spring Boot | 服务开发与 Actuator | 1.5.9.RELEASE |
| Spring Cloud | 配置、注册发现、网关 | Edgware.SR6 |
| Spring Data JPA | MySQL 数据访问 | Spring Boot 管理版本 |
| Neo4j | 商品、用户、促销的既有图数据 | 3.5.35 Community（Compose） |
| MySQL | 交易、库存、订单、券、地址、退款账本 | 5.7.44（Compose） |
| Maven | 构建与依赖治理 | 根 pom.xml 集中管理版本 |
| Docker Compose | 单机演示编排 | Compose v2 |

## 快速开始

### 环境要求

JDK 8、Maven Wrapper，以及运行 Compose 所需的 Docker Engine 和 Docker Compose v2。

### 1. 构建

```bash
git clone https://github.com/Blucezhang/micro-server-own.git
cd micro-server-own
./mvnw -B -ntp clean verify
```

### 2. 配置

```bash
cp .env.example .env
chmod 600 .env
```

至少替换 MYSQL_ROOT_PASSWORD、MYSQL_PASSWORD、NEO4J_PASSWORD、JWT_SECRET、INTERNAL_SERVICE_TOKEN 和 PAYMENT_MOCK_CALLBACK_TOKEN。完整变量见 [.env.example](.env.example)，不要提交 .env、私钥、证书或真实渠道密钥。

### 3. 启动与停止

```bash
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d

set -a
. ./.env
set +a
bash scripts/smoke-test.sh

docker compose --env-file .env down
```

已有 MySQL 数据卷不会自动执行新增迁移，请使用 `scripts/apply-migrations.sh`；迁移前先备份。

## 接口示例

网关地址默认为 `http://localhost:9632`。写请求统一携带 `Idempotency-Key`，受保护接口携带 `Authorization: Bearer <accessToken>`。

### 注册、登录和加购

```bash
BASE=http://localhost:9632

curl -X POST "$BASE/user/api/v1/auth/registrations" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: buyer-registration-001' \
  -d '{"loginName":"buyer01","password":"replace-me","name":"演示买家","email":"buyer01@example.test","phone":"13800138000"}'

curl -X POST "$BASE/user/login/token" \
  -H 'Content-Type: application/json' \
  -d '{"loginUserName":"buyer01","password":"replace-me","actorType":"BUYER"}'

TOKEN=<data.accessToken>
curl -X POST "$BASE/order/api/v1/cart/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Idempotency-Key: cart-001' \
  -H 'Content-Type: application/json' \
  -d '{"productId":1,"quantity":1}'
```

### 模拟支付

```bash
curl -X POST "$BASE/settlement/api/v1/payments/mock-callbacks/MOCK_ALIPAY" \
  -H "X-Mock-Payment-Token: $PAYMENT_MOCK_CALLBACK_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"paymentNo":"PAY-...","providerPaymentNo":"ALIPAY-...","amount":128.00,"result":"SUCCESS"}'
```

模拟支付不调用真实微信或支付宝。完整多商家下单、发货、签收、退款和换货顺序见 [商城能力路线图](docs/product/mall-capability-roadmap.md)。

## 配置说明

服务配置位于 `own-config/src/main/resources/config-repo/`，Compose 使用 `SPRING_PROFILES_ACTIVE=local` 加载。生产环境应使用独立的受控配置。

| 配置项 | 默认或用途 |
| --- | --- |
| `trade.security.jwt.enabled` | Compose 默认开启 Bearer JWT |
| `trade.security.jwt.secret` | HS256 密钥，部署时使用随机值 |
| `trade.security.service-boundary.enabled` | 服务边界校验，Compose 开启 |
| `trade.idempotency.retention-hours` | 成功幂等记录保留 24 小时 |
| `trade.inventory.reservation-ttl-minutes` | 库存预占 15 分钟过期 |
| `trade.order.payment-timeout-minutes` | 待支付订单 15 分钟关闭 |
| `trade.order.auto-receipt-days` | 7 天自动签收 |
| `OWN_FILE_TEMP_ROOT`、`OWN_FILE_PERMANENT_ROOT` | 文件存储目录 |
| `ORDER_OUTBOX_WEBHOOK_URL`、`ORDER_OUTBOX_WEBHOOK_SECRET` | 可选 HTTPS Outbox Webhook |

订单事件 `PENDING` 只表示已写入 Outbox，不表示外部系统已送达。未配置真实消息渠道时不会伪造成功。

## 生产部署

docker-compose.yml 是单机演示编排，可作为构建、端口与环境变量参考，不能直接视为生产部署方案。

生产环境拓扑、密钥管理、发布/回滚、模拟微信/支付宝替换为真实渠道的验收要求，见 [生产部署与渠道接入计划](docs/production-deployment-plan.md)。当前业务约定为商家 80%、平台 20%、每周结算、金额严格大于 100 元才可提现；退款直接冲回应收，已提现时产生待追偿余额。

### 部署前检查清单

- [ ] 使用托管或高可用 MySQL、Neo4j，执行备份、恢复和升级演练；数据库端口不暴露公网。
- [ ] 仅向公网暴露网关，在其前配置 HTTPS、反向代理、WAF、访问日志、请求体限制和真实客户端 IP 传递。
- [ ] 将 Eureka、Config Server、业务服务放入私有网络；仅网关可接收外部请求，避免绕过 JWT 网关过滤器。
- [ ] 使用 Secrets Manager、Kubernetes Secret 或受控部署平台保存数据库密码、邮件/SMS 凭据及支付密钥；严禁提交 .env、私钥或证书。
- [ ] 显式覆盖 MYSQL_URL、NEO4J_URI、EUREKA_DEFAULT_ZONE、文件存储路径与日志/指标采集配置；删除所有 change-me 默认值。
- [ ] 在预发环境验证订单超时、库存并发、Outbox 积压、备份恢复和滚动发布。

### 数据库迁移与发布

1. 备份数据库，并在同版本副本演练迁移和恢复。
2. 不要依赖已有 Docker 数据卷的初始化行为。使用 `scripts/apply-migrations.sh` 执行迁移；它在目标库创建 `micro_schema_migration`，记录版本、文件名和 SHA-256，拒绝被篡改的已应用脚本。
3. 对已有且已确认执行到第 20 个迁移的数据库，先显式建立基线，再只执行后续迁移：

```bash
MYSQL_HOST=db.example.internal MYSQL_PORT=3306 MYSQL_USER=micro MYSQL_PASSWORD='...' MYSQL_DATABASE=micro \\
  bash scripts/apply-migrations.sh --baseline-through 20
```

   `--baseline-through` 不检查业务表结构，只有在备份和人工核对后才可使用；新库不带该参数直接执行。也可以将该脚本放入 DBA 的受控迁移作业。
4. 先发布兼容新表/字段的服务，再切换流量；发布后检查健康状态、错误率、库存异常及 Outbox 积压。

## 验证与验收

```bash
./mvnw -B -ntp clean verify
bash scripts/validate-migrations.sh
docker compose --env-file .env config --quiet
```

这些命令只证明构建、单元测试、迁移编号与 Compose 语法通过。真实 MySQL/Neo4j、容器运行、支付、物流、消息投递、性能、安全、容灾与合规必须在对应环境单独验收。

## 项目文档

- [商城能力路线图](docs/product/mall-capability-roadmap.md)：功能阶段、业务边界和后续规划。
- [生产部署与渠道接入计划](docs/production-deployment-plan.md)：部署拓扑、密钥、支付和结算接入要求。
- [数据库迁移目录](database/mysql/)：MySQL 前向迁移脚本。
- [环境变量示例](.env.example)：本地 Compose 配置模板。

## 许可证

仓库中的 LICENSE.htm 内容与项目不匹配，不能视为有效项目许可证。本文档不授予商业使用权；在作者明确许可证前，请避免商业分发。
