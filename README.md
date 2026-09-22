# micro-server-own

> 基于 Spring Cloud 的多商家商城后端演示项目

## ⚠️ 项目说明

micro-server-own 是一个学习型、演示型的多商家商城后端。它将用户、商品、促销、库存、订单、文件、消息和工作流模块组织为一条交易链路：

> 买家浏览商品 → 加入购物车 → 选择地址和优惠券 → 试算 → 多商家拆单与锁库存 → 模拟支付 → 商家发货 → 买家签收 → 售后退款。

项目适合学习微服务拆分、库存预占、订单状态机、幂等处理、Outbox、JWT/RBAC 和容器化配置。

## 🐶 新手必读

| 项目 | 说明 |
| --- | --- |
| 项目类型 | 多商家商城后端；不包含前端页面 |
| 开发语言 | Java 8 |
| 核心框架 | Spring Boot 1.5.9、Spring Cloud Edgware |
| 数据存储 | MySQL 5.7、Neo4j 3.5 |
| 网关地址 | http://localhost:9632 |
| 本地启动 | [快速开始](#-快速开始) |
| 能力路线 | [商城能力路线图](docs/product/mall-capability-roadmap.md) |

开始前请先了解两条边界：

1. `/user/login/token` 使用 BCrypt 校验账号密码并签发短时 HS256 Access JWT 与可轮换 Refresh Token；网关默认启用 JWT，会忽略客户端提交的 `X-Actor-*`，而以令牌声明重建身份头。登录、刷新/退出会话、使用旧密码的 `/user/login/updatePassword`、商品公开浏览和模拟渠道回调是受限匿名入口，其余业务接口须携带 Bearer JWT。
2. own-settlement 目前是本地模拟支付与退款账本；微信、支付宝及商家结算参数已经预留，但不连接真实渠道。
3. Docker Compose 仅把网关 `9632` 暴露给外部网络；Eureka、Config Server 仅绑定到宿主机 `127.0.0.1` 用于本地排障，各业务服务仅在 Compose 后端网络监听，不能作为业务入口。网关会覆盖来访者提交的内部令牌并注入受保护的共享令牌；业务服务启用服务边界校验后拒绝缺少该令牌的请求，从而不能通过直连伪造 `X-Actor-*`。

## 🐰 版本说明

| 项目版本 | Java | Spring Boot | Spring Cloud | 状态 |
| --- | --- | --- | --- | --- |
| master / 1.0-ALPHA | 8 | 1.5.9.RELEASE | Edgware.SR6 | 当前维护分支 |

> Spring Boot 1.5.9、Spring Cloud Edgware 和 Neo4j 3.5 均为旧技术栈。升级需要独立完成兼容性设计、回归测试和数据迁移。

## 📌 当前实现总览

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

## 🐯 项目简介

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

## 🐼 内置能力

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

## 🏗️ 项目结构

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

## 🐨 技术栈

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

## 🚀 快速开始

### 1. 构建并检查代码

~~~
./mvnw -B -ntp clean verify
bash scripts/validate-migrations.sh
git diff --check
~~~

### 2. 创建本地配置

~~~
cp .env.example .env
chmod 600 .env
~~~

替换 .env 中所有 change-me 占位值：

| 环境变量 | 说明 |
| --- | --- |
| MYSQL_ROOT_PASSWORD | MySQL root 密码 |
| MYSQL_PASSWORD | 应用 MySQL 用户 micro 的密码 |
| NEO4J_PASSWORD | Neo4j neo4j 用户密码 |
| EUREKA_USER | Eureka 基本认证用户，默认 own |
| EUREKA_PASSWORD | Eureka 基本认证密码 |
| JWT_ENABLED | 网关是否强制 Bearer JWT，默认 `true` |
| JWT_SECRET、JWT_PREVIOUS_SECRET | HS256 当前签名密钥与可选上一把密钥；轮换时先配置上一把用于验签，待旧令牌自然过期后清空；示例值必须替换为随机且至少 32 字节的值 |
| JWT_ISSUER、JWT_EXPIRATION_SECONDS、JWT_REFRESH_EXPIRATION_SECONDS | Access JWT 签发方与有效期（默认 7200 秒），Refresh Session 默认 14 天 |
| JWT_REFRESH_SESSION_CLEANUP_DELAY_MS | 服务端过期刷新会话清理间隔（默认 3600000 毫秒）；登出撤销刷新会话，已签发 Access JWT 在短有效期内自然失效 |
| LOGIN_MAX_FAILURES、LOGIN_LOCK_MINUTES | 同一账号连续失败阈值和临时锁定分钟数，默认 5 次/15 分钟；成功登录会清零计数 |
| LOGIN_ATTEMPT_RETENTION_DAYS、LOGIN_ATTEMPT_CLEANUP_DELAY_MS | 登录失败元数据最长保留天数及清理频率，默认 90 天/24 小时；仍处于锁定期的账号记录不会被清理 |
| PAYMENT_CHANNEL | 模拟支付默认渠道：`MOCK_WECHAT` 或 `MOCK_ALIPAY` |
| PAYMENT_MOCK_CALLBACK_TOKEN | 模拟微信/支付宝回调密钥；回调请求使用 `X-Mock-Payment-Token` 传递 |
| INTERNAL_SERVICE_TOKEN、INTERNAL_SERVICE_PREVIOUS_TOKEN | 网关、订单、库存、优惠券、地址、文件、通知、工作流与结算服务之间的共享密钥。当前值必须是随机且至少 32 个 UTF-8 字节（服务端会拒绝缺失或更短的值）；Compose 会以它开启服务边界校验，网关会替换客户端伪造值。滚动轮换时将旧值短暂放入 `INTERNAL_SERVICE_PREVIOUS_TOKEN`，全部服务切换完成后清空该变量。二者绝不能交给浏览器或写入日志。 |

### 3. 启动演示环境

~~~
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d

set -a
. ./.env
set +a
./scripts/smoke-test.sh
~~~

### 3.1 MySQL 备份与完整性校验

生产环境应在每次数据库迁移前后以及按既定备份策略执行逻辑备份。下面脚本只读取 MySQL，不会执行恢复；`BACKUP_DIR` 必须是仓库外的绝对路径并由备份系统加密保管。迁移与备份脚本仅接受字母、数字和下划线组成的 `MYSQL_DATABASE`，避免错误环境变量被解释为命令参数；校验脚本只接受绝对归档路径，并同时验证 gzip 完整性、单行 SHA-256 清单格式和实际摘要：

~~~
export MYSQL_HOST=db.example.internal MYSQL_PORT=3306 MYSQL_USER=micro
export MYSQL_PASSWORD='replace-me' MYSQL_DATABASE=micro
export BACKUP_DIR=/srv/backups/micro-server-own
./scripts/backup-mysql.sh
./scripts/verify-mysql-backup.sh /srv/backups/micro-server-own/micro-YYYYMMDDTHHMMSSZ.sql.gz
~~~

MySQL 逻辑备份不包含文件服务的永久对象；文件卷或对象存储必须按同一恢复点目标单独快照和演练恢复。恢复操作会覆盖数据，必须在隔离环境先验证并经过运维变更审批。

SYSTEM 运维排查可在内部令牌保护下调用 `GET /order/api/v1/internal/sensitive-access-audits/page?page=0&size=20&orderNo=...` 分页读取地址访问审计。此接口不会对买家或商家公开。

停止服务并保留本地数据卷：

~~~
docker compose --env-file .env down
~~~

首次创建 MySQL 数据卷时，容器会执行 `database/mysql/micro.sql` 与当前仓库中的 `02` 至 `32` 号前向迁移。已有数据卷不会自动执行新迁移；请使用下文的受控迁移脚本。

### 4. 调用一个交易接口

先登录获得令牌。角色名遵循 `ROLE_BUYER`、`ROLE_MERCHANT`、`ROLE_SYSTEM`；没有图谱角色的个人用户默认只能以 BUYER 身份登录。网关默认强制 Bearer JWT，并以令牌声明覆写外部 `X-Actor-*` 头；下面所有受保护接口均使用 `Authorization: Bearer`：

首次使用可先匿名创建演示买家。此入口只创建 BUYER，不签发令牌、不创建商家或 SYSTEM 权限；需要使用相同 `Idempotency-Key` 重试时会返回原成功响应。真实短信/邮箱验证与反滥用策略尚未接入，生产环境应在网关外增加相应能力：

~~~
BASE=http://localhost:9632
curl -X POST "$BASE/user/api/v1/auth/registrations" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: buyer-registration-001' \
  -d '{"loginName":"buyer01","password":"replace-me","name":"演示买家","email":"buyer01@example.test","phone":"13800138000"}'
~~~

`loginName` 仅允许 3–64 位英文字母、数字、`.`、`_`、`-`；新密码必须为 8–128 个字符且不能包含控制字符。规则仅在创建/改密时生效，不会使既有 BCrypt 密码立即失效。

~~~
curl -X POST "$BASE/user/login/token" \
  -H 'Content-Type: application/json' \
  -d '{"loginUserName":"buyer01","password":"replace-me","actorType":"BUYER"}'
# 从返回 data.accessToken 取值并设置：TOKEN=...
~~~

登录响应同时包含 `refreshToken`。Access JWT 临近过期时可调用 `POST /user/login/refresh` 轮换；`GET /user/login/sessions` 查看当前账号仍有效的会话，携带 `Idempotency-Key` 的 `DELETE /user/login/sessions/{id}` 可撤销指定会话。两者都会复核 JWT 用户与主体归属。不要把 Refresh Token 放入前端日志或 URL。

若设备丢失或怀疑 Refresh Token 泄露，登录后可调用 `DELETE /user/api/v1/account/sessions` 并携带 `Idempotency-Key` 撤销该账号所有服务端 Refresh Session；服务端会复核 JWT 中的用户、主体归属与请求身份三者，不能借 `X-User-Id` 撤销他人会话。已签发的 Access JWT 不会被服务器逐个收回，只会在其短有效期到达后失效。

使用旧密码修改密码成功后，系统会撤销该账号全部 Refresh Session；已有 Access JWT 仍只在其配置的短有效期内可用。密码修改失败也会进入与登录相同的失败计数和临时锁定保护，不能作为绕过登录限流的验证入口。

登录后可调用 `GET /user/api/v1/account/authorization` 查看当前 JWT 主体对应的持久化 `actorId`、角色和功能权限。该接口会复核 JWT 用户、请求主体与持久化账号的归属关系，只用于买家或商家自查，不能读取其他账号或 SYSTEM 权限。

买家历史订单保留兼容的 `GET /order/api/v1/orders` 全量接口；新客户端应使用 `GET /order/api/v1/orders/page?page=0&size=20&status=PAID&from=<epochMillis>&to=<epochMillis>` 分页读取自己的订单。`status`、`from`、`to` 均可省略，单页最大 100 条。

购物车接口为 `POST /order/api/v1/cart/items`、`GET /order/api/v1/cart/items`、`PUT /order/api/v1/cart/items/{cartItemId}` 与 `DELETE /order/api/v1/cart/items/{cartItemId}`；三个写操作都必须携带 `Idempotency-Key`。同一买家再次加入同一 SKU 会合并并累加数量；更新数量会重新验证 SKU 的商家、名称、价格与可售状态，商品发生变化时返回 `409 PRODUCT_CHANGED` 且不改写购物车。

`GET /order/api/v1/orders/{orderNo}/detail` 和售后详情中的 `events` 是面向买家/所属商家的业务时间线，仅包含事件类型、关联子订单/售后单号和发生时间；不会暴露 Outbox 投递状态、失败原因、内部载荷或操作主体。系统排障仍通过受内部令牌保护的 Outbox 接口进行。

商家调用 `GET /order/api/v1/merchant/sub-orders/{subOrderNo}` 时，响应中的 `events` 只包含该子订单的履约事件，不包含同一母订单中其他商家的事件或母订单级内部投递信息。

售后列表同样保留旧接口；新客户端使用 `GET /order/api/v1/after-sales/page?page=0&size=20&status=APPLYING` 查询自己的售后，商家使用 `GET /order/api/v1/after-sales/merchant/page` 查询自有售后。状态可省略，单页最大 100 条。

买家在商家审核前可使用 `POST /order/api/v1/after-sales/{afterSaleNo}/close` 撤销自己的售后申请；该操作只允许 `APPLYING → CLOSED`，不会触发退款、库存变动或物流动作。

登录后的买家和商家可使用 `GET /user/api/v1/account/profile` 读取本人资料，并携带 `Idempotency-Key` 调用 `PUT /user/api/v1/account/profile` 更新 `name`、`email` 或 `phone`。该入口不能修改登录名、密码、主体归属、角色、组织或权限；密码仍只能通过旧密码校验后的 `/user/login/updatePassword` 修改。

换货流程中，商家验收退件后进入 `EXCHANGE_PENDING_SHIPMENT`，填写替换件物流后变为 `EXCHANGE_SHIPPED`；仅原买家可调用 `POST /order/api/v1/after-sales/{afterSaleNo}/exchange-receive` 确认收到替换件，最终进入 `EXCHANGED`。该确认不涉及退款。

`GET /order/api/v1/after-sales/{afterSaleNo}` 可由原买家或所属商家读取；服务端会复核归属，商家不能借该路由读取其他商家的售后单。旧 `/file/**` 与 `/category/**` 接口因缺少商城资源归属模型，网关仅允许 SYSTEM 迁移/维护角色访问；业务客户端须使用 `/file/api/v1/files/**` 和商品公开浏览接口。

同理，旧 `/Info/**` 通知历史和 `/flow/**` 工作流图接口只允许 SYSTEM 维护角色访问，避免短信、邮箱或流程记录被普通业务身份横向读取。它们不是面向买家/商家的通知中心；若以后提供此能力，必须新增按收件人或业务资源隔离的版本化 API。

商家在买家签收前可用 `POST /order/api/v1/sub-orders/{subOrderNo}/shipment-correction` 修正物流公司和单号。每次修正写入物流轨迹和订单事件，不重置原发货时间，签收后返回 `409`。

买家和子订单所属商家均可读取 `GET /order/api/v1/sub-orders/{subOrderNo}/logistics-traces`；追加轨迹的 `POST` 接口仅限该子订单所属商家。订单服务会再次校验订单或商家归属，网关角色通过不代表可以读取他人的轨迹。

买家与所属商家读取售后列表、分页、详情或执行售后状态操作时，响应使用售后展示投影，不返回持久化的 `buyerId`、`merchantId` 或内部数据库主键；双方的归属校验仍在服务端以这些内部字段完成。

商家通过 `GET /product/api/v1/merchant/products?page=0&size=20&saleStatus=OFF_SHELF` 查看自己的完整 SKU 目录（包含下架商品），可按分类、关键词与上下架状态筛选；公开 `GET /product/api/v1/products` 与 `GET /product/api/v1/products/{id}` 始终只返回可售 SKU 的展示投影，不包含初始化库存和销量字段。商家修改 SKU 原价或促销价时可选传 `priceChangeReason`（最多 200 字）；系统保存前后价格和操作者审计记录，可通过 `GET /product/api/v1/merchant/products/{id}/price-audits?page=0&size=20` 查看自有 SKU 的历史。

商品与评价仅开放四个匿名读取路径：`GET /product/api/v1/categories?level=1`、`GET /product/api/v1/products`、`GET /product/api/v1/products/{productId}` 及 `GET /product/api/v1/products/{productId}/reviews?page=0&size=20`。分类 `level` 仅允许 1–9，且只返回 ID、名称和层级；评价返回 `total`、`page`、`size` 与 `items`，单页最大 100 条。公开及商家回复响应只包含展示字段，不暴露买家内部 ID、举报信息或审核状态。买家在子订单签收后，携带 `Idempotency-Key` 调用 `POST` 同一路径提交一次 `{"rating": 1..5, "content":"..."}`；未签收、重复评价或商家身份都会被拒绝。

`REVIEW_PROHIBITED_TERMS` 可配置逗号分隔的评价与商家回复禁用词，作为部署级兜底规则；命中时返回 `422`。SYSTEM 还可用 `GET /product/api/v1/system/review-prohibited-terms?page=0&size=20` 查询持久化词库，以 `POST` 创建 `{"term":"..."}`，并用 `PUT /product/api/v1/system/review-prohibited-terms/{id}` 更新 `{"term":"...","active":true|false}`。这些写操作必须带 `Idempotency-Key`，且词条统一按去除首尾空白后的小写形式去重；静态与持久化词库均会同时校验买家评价和商家回复。它们仍只是第一层拦截，买家举报和 SYSTEM 审核用于后续处置。

遗留 `/product/**` 与 `/sale/**` 图谱接口全部仅限 SYSTEM，用于迁移或维护；买家和商家必须使用本文列出的 `/api/v1` 商城接口。

商家可通过 `POST /sale/api/v1/coupons/merchant/templates` 创建固定金额店铺券模板，字段为 `name`、`totalQuantity`、`minimumAmount`、`discountAmount`、`expiresAt`，可选 `claimStartsAt`、`claimEndsAt`（均为毫秒时间戳）。`GET /sale/api/v1/coupons/merchant/templates` 仅列出自己的模板及实时 `availableQuantity`；`POST /sale/api/v1/coupons/merchant/templates/{id}/status?value=DISABLED` 可停止后续领取。买家以 `{"couponType":"STORE","merchantTemplateId":123}` 调用 `/sale/api/v1/coupons/claim` 领取；同一买家对同一新模板只能成功领取一次，数据库唯一键覆盖并发竞争。模板停用不影响已领取用户券的订单快照与既有状态。

商家可通过 `POST /product/api/v1/merchant/reviews/{reviewId}/reply` 回复自己商品的评价；买家可使用 `POST /product/api/v1/products/{productId}/reviews/{reviewId}/reports` 提交一次 `{"reason":"..."}` 举报。SYSTEM 可通过 `GET /product/api/v1/system/review-reports?status=PENDING` 查看待处理举报、用 `POST /product/api/v1/system/review-reports/{reportId}/resolve` 标记 `RESOLVED` 或 `DISMISSED`，并通过 `PUT /product/api/v1/products/{productId}/reviews/{reviewId}/moderation`（请求体 `{"published":false}`）独立隐藏不当内容，恢复时传 `true`。公开列表只返回 `PUBLISHED` 评价。

以下示例创建收货地址；返回的 data.id 可在试算和下单时作为 addressId 使用：

~~~
BASE=http://localhost:9632
TOKEN=<data.accessToken>

curl -X POST "$BASE/user/api/v1/addresses" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Idempotency-Key: address-001' \
  -H 'Content-Type: application/json' \
  -d '{"recipientName":"演示买家","mobile":"13800138000","province":"上海市","city":"上海市","district":"浦东新区","detail":"示例路 1 号","defaultAddress":true}'
~~~

完整的多商家下单、模拟支付、发货、签收、整单退款和售后流程见 [商城能力路线图](docs/product/mall-capability-roadmap.md)。

模拟渠道回调使用支付单返回的 `paymentNo`、`providerPaymentNo` 和金额；支付宝示例：

~~~
curl -X POST "$BASE/settlement/api/v1/payments/mock-callbacks/MOCK_ALIPAY" \
  -H "X-Mock-Payment-Token: $PAYMENT_MOCK_CALLBACK_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"paymentNo":"PAY-...","providerPaymentNo":"ALIPAY-...","amount":128.00,"result":"SUCCESS"}'
~~~

微信将路径中的渠道改为 `MOCK_WECHAT`，并使用支付单返回的 `WXPAY-...` 渠道流水。该入口只用于本地模拟，不会调用真实微信或支付宝。

### 5. 上传售后举证材料（可选）

售后申请的 `evidenceFileNames` 只能引用新归属文件接口创建、且已由同一买家提升为正式存储的文件。旧 `/file/picture`、`/file/promote` 接口保留兼容性，但不产生归属元数据，不能作为售后举证材料。

~~~
# 先上传。记录返回 data.storedName。
curl -X POST "$BASE/file/api/v1/files" \
  -H "Authorization: Bearer $TOKEN" \
  -F 'file=@./proof.jpg'

# 再以相同买家身份提升为正式存储。
curl -X POST "$BASE/file/api/v1/files/<storedName>/promote" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Idempotency-Key: file-promote-001'

# 创建售后时传入："evidenceFileNames":["<storedName>"]
~~~

归属文件提升后，所有者可用 `GET /file/api/v1/files/{storedName}/content` 下载二进制内容；该接口要求与文件所有者匹配的 JWT。历史 `/file/Picture/**` 路由仍是旧兼容接口，不具备该私有下载校验。

## ⚙️ 配置说明

服务配置在 own-config/src/main/resources/config-repo/，Compose 使用 SPRING_PROFILES_ACTIVE=local 加载。生产应使用独立的受控配置，而不是直接修改演示文件。

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| MYSQL_URL、MYSQL_USER、MYSQL_PASSWORD | Compose 内部 MySQL | 交易、地址、券、库存、订单、结算数据源 |
| NEO4J_URI、NEO4J_USER、NEO4J_PASSWORD | Compose 内部 Neo4j | 商品、用户、促销图数据源 |
| EUREKA_DEFAULT_ZONE | Eureka 地址 | 服务注册连接串 |
| OWN_FILE_TEMP_ROOT、OWN_FILE_PERMANENT_ROOT | /data/files/... | 文件临时与永久存储根目录 |
| REVIEW_PROHIBITED_TERMS | 空 | 可选、逗号分隔的部署级评价/回复禁用词；与 SYSTEM 管理的持久化词库叠加生效 |
| trade.idempotency.retention-hours | 24 | 成功写请求幂等记录保留时间 |
| trade.inventory.reservation-ttl-minutes | 15 | 库存预占过期时间 |
| trade.order.payment-timeout-minutes | 15 | 待支付订单关闭时间 |
| trade.order.auto-receipt-days | 7 | 自动签收天数 |
| trade.security.audit-retention-days | 180 | 敏感访问审计元数据保留天数 |
| trade.security.jwt.enabled | true | 网关是否强制 Bearer JWT；必须配置非空高强度密钥 |
| trade.security.jwt.secret | 空 | HS256 签名密钥，不能提交到仓库 |
| trade.security.jwt.expiration-seconds | 7200 | JWT 过期时间 |
| trade.security.jwt.refresh-expiration-seconds | 1209600 | Refresh JWT 有效期（秒） |
| trade.security.jwt.refresh-session-cleanup-delay-ms | 3600000 | 服务端过期刷新会话清理间隔（毫秒） |
| GATEWAY_RATELIMIT_BEHIND_PROXY | false | 网关是否从可信反向代理提供的客户端地址识别来源 IP。只有网关不直接暴露、且前置代理会覆写而非透传客户端伪造的转发头时才可设为 true；否则保持 false。 |
| trade.security.service-boundary.enabled | false（Compose 为 true） | 启用后，除健康检查外每个业务服务只接受网关注入或受信任服务携带的内部令牌；本地直接启动调试默认关闭，不能把该默认值用于部署环境 |
| trade.security.login.max-failures、lock-minutes | 5、15 | 持久化账号登录失败阈值和临时锁定分钟数；不保存密码或来源 IP |
| trade.security.login.attempt-retention-days、attempt-cleanup-delay-ms | 90、86400000 | 登录失败元数据的保留期（天）与清理频率（毫秒）；清理任务保留仍在锁定期的账号记录 |
| trade.internal.service-token、trade.internal.previous-service-token | 空 | 服务间调用密钥；当前密钥缺失时订单支付/退款回调、结算金额投影、库存预占与回补、优惠券占用/核销/释放、订单读取地址快照、售后举证文件校验、售后退款回调，以及订单 Outbox/敏感审计与结算运维内部接口均拒绝执行。上一把密钥只用于滚动轮换的短暂验签兼容，不能作为长期双密钥配置。 |
| X-Correlation-Id | 自动生成 | 可选请求关联 ID。网关会将合法值覆写并转发给下游服务，同时在响应中返回；缺失、超过 100 个字符或包含非字母数字、`.`、`_`、`:`、`-` 的值会被替换为新的 UUID。排障时可将该响应头提供给日志检索，不应承载用户信息或密钥。 |
| ORDER_OUTBOX_WEBHOOK_URL、ORDER_OUTBOX_WEBHOOK_SECRET | 空 | 可选订单事件 Webhook。URL 必须为绝对 HTTPS 地址，且两者同时配置、密钥至少 32 个 UTF-8 字节才启用；请求为 JSON，并携带 `X-Order-Event-Id`、`X-Order-Event-Type` 与覆盖 JSON 请求体的 `X-Order-Event-Signature: sha256=<hex>`。接收方必须先以同一密钥验签、再按事件 ID 去重；缺失/非 HTTPS URL 或弱密钥时事件保持 `PENDING`，不会伪造送达。 |
| trade.settlement.merchant-rate | 0.80 | 商家应收比例；平台比例为剩余金额 |
| trade.settlement.cycle-days | 7 | 支付成功后进入结算批次前的等待天数 |
| trade.settlement.withdrawal-minimum | 100.00 | 提现金额必须严格大于该值 |

商家可以通过 PUT /inventory/api/v1/stocks/{productId}/low-stock-alert-rule 设置自有 SKU 的非负低库存阈值和启停状态；GET /inventory/api/v1/merchant/low-stock-alerts 只返回当前可用库存不高于阈值的自有 SKU。它是实时查询，不表示已发送短信、邮件或推送。

商家可使用 `GET /sale/api/v1/coupons/merchant/summary` 查询自有店铺券的已发放总数及 `available`、`reserved`、`used`、`expired` 聚合数量；该接口不返回买家券号或身份信息。券领取会校验旧券模板配置的领取起止时间。

精确库存读取 `GET /inventory/api/v1/stocks/{productId}?merchantId=...` 仅允许库存所属商家或 SYSTEM；商家 ID 与令牌身份不一致会被拒绝。买家前台不暴露库存数量，仅通过商品可售状态和下单预占结果判断能否购买。

SYSTEM 可通过 `POST /inventory/api/v1/stocks/bootstrap/catalogue` 从商品目录创建尚未存在的库存行。该操作使用数据库唯一键的原子“仅插入”语义：重复执行不会覆盖已售、已预占或人工调整后的库存；要修改现有库存，应使用 SYSTEM 设置库存或商家带原因的库存调整接口。

库存确认扣减与预占过期释放会先锁定同一预占记录并在锁内复核状态，因此两者竞争时只会有一个状态转换成功；已提交的预占不会被过期任务错误回补。

## 🐳 生产部署

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

~~~
MYSQL_HOST=db.example.internal MYSQL_PORT=3306 MYSQL_USER=micro MYSQL_PASSWORD='...' MYSQL_DATABASE=micro \\
  bash scripts/apply-migrations.sh --baseline-through 20
~~~

   `--baseline-through` 不检查业务表结构，只有在备份和人工核对后才可使用；新库不带该参数直接执行。也可以将该脚本放入 DBA 的受控迁移作业。
4. 先发布兼容新表/字段的服务，再切换流量；发布后检查健康状态、错误率、库存异常及 Outbox 积压。

## ✅ 验证范围

~~~
./mvnw -B -ntp clean verify
bash scripts/validate-migrations.sh
docker compose --env-file .env config --quiet
~~~

这些命令只证明构建、单元测试、迁移编号与 Compose 语法通过。真实 MySQL/Neo4j、容器运行、支付、物流、消息投递、性能、安全、容灾与合规必须在对应环境单独验收。

## 📄 许可证

仓库中的 LICENSE.htm 内容与项目不匹配，不能视为有效项目许可证。本文档不授予商业使用权；在作者明确许可证前，请避免商业分发。
