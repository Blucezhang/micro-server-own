# 后端配置与常量规范

本次整理的是 Java 后端的运行参数、服务地址和共享协议值，不改变业务接口、数据库状态含义或事务边界。不是把所有变量移出代码：局部变量、实体字段、算法表达式仍应由代码管理。

## 1. 按用途归属

| 内容 | 放置位置 | 使用方式 |
| --- | --- | --- |
| 地址、超时、重试次数、扫描间隔、预留时间、结算比例、提现门槛、文件根路径 | 服务自己的 `src/main/resources/*-runtime.yaml` | `@ConfigurationProperties` 或构造器 `@Value` 注入，不在调用点重复写默认值 |
| 多服务共用的令牌有效期、幂等保留时间、历史客户端地址 | `own-common/src/main/resources/trade-runtime.yaml` | 由每个应用的配置导入，再由部署环境覆盖 |
| 数据库地址、凭据、内部令牌、JWT 密钥、支付渠道密钥 | Nacos 配置引用的环境变量／部署密钥 | 不写入 Java，不把真实密钥提交到仓库 |
| 订单恢复状态、事件类型、请求头、取消原因 | 对应领域的常量文件或枚举 | 直接引用，保持原有持久化与协议字符串值 |
| 安全上限、协议约束 | 所属配置／领域类的具名常量 | 不允许用运维配置任意突破安全上限 |
| 请求路径、JSON 字段、SQL、数据库列长度、错误文案 | 对应接口、实体、迁移或业务代码 | 它们是契约或逻辑，不是部署参数，不能机械外置 |
| 测试输入、预期请求地址、预期协议字符串 | 测试文件 | 保持独立预期，避免测试与实现引用同一常量造成错误同时通过 |

不要创建囊括所有业务的 `Constants` 大类，也不要为只出现一次、含义明确的普通表达式增加无意义的配置项。

## 2. 配置文件位置

所有 10 个可执行应用均按下列顺序导入配置：

1. 共享 `classpath:trade-runtime.yaml`。
2. 本服务的运行默认值（如有）。
3. 当前环境的 Nacos Data ID。

同一导入列表中，后面的 Nacos 配置覆盖前面的打包默认值；环境变量、命令行等仍遵循 Spring Boot 的配置优先级。这里调整的不是 Nacos 配置发布流程。

| 服务 | 服务专属默认值文件 | 主要内容 |
| --- | --- | --- |
| 网关 | `own-api-gateway/src/main/resources/api-gateway-runtime.yaml` | 原有 JWT 开关默认值 |
| 用户 | `own-user-party/src/main/resources/user-party-runtime.yaml` | 登录失败锁定、会话与失败记录清理 |
| 库存 | `own-inventory/src/main/resources/inventory-runtime.yaml` | 预留时效、扫描间隔、商品目录地址 |
| 优惠券 | `own-promotion/src/main/resources/promotion-runtime.yaml` | 预留时效、清理间隔 |
| 订单 | `own-order/src/main/resources/order-runtime.yaml` | 服务地址、HTTP、恢复、Saga、Outbox、提醒、审计、自动收货 |
| 结算 | `own-settlement/src/main/resources/settlement-runtime.yaml` | 订单地址、HTTP、恢复、渠道、结算比例与提现门槛 |
| 商品 | `own-product/src/main/resources/product-runtime.yaml` | 订单评价资格地址、价格审计投影批次与间隔 |
| 文件 | `own-file/src/main/resources/file-runtime.yaml` | 临时／永久存储根目录 |
| 消息、工作流 | 仅共享默认值；现有服务配置保持原位置 | 不虚构未接入的服务能力 |

文件服务打包默认目录仍为 `/opt/temp`、`/opt/real`；现有 Nacos 文件中的 `/data/files/temp`、`/data/files/real` 继续优先，不会因为外置默认值而改变已配置部署的目录。

## 3. 如何覆盖

例如，向订单服务对应的 Nacos YAML 增加以下配置，即可覆盖其打包默认值：

```yaml
trade:
  http:
    connect-timeout-millis: 2000
    read-timeout-millis: 12000
  recovery:
    max-attempts: 5
    retry-delay-millis: 45000
    scan-delay-millis: 45000
  services:
    inventory-base-url: http://own-inventory
```

也可使用标准环境变量，如 `TRADE_HTTP_READ_TIMEOUT_MILLIS`、`TRADE_RECOVERY_MAX_ATTEMPTS`、`TRADE_SERVICES_INVENTORY_BASE_URL`。合并 YAML 时应编辑已有 `trade` 节点，不能重复声明根键。

现代交易客户端只允许 HTTP(S) 服务根地址，不允许地址携带用户名密码、路径、查询参数或片段；末尾 `/` 会被规范化。接口路径、请求头、幂等键生成规则保持代码契约。现有 RestTemplate 仍带有服务发现／负载均衡语义；修改根地址不等于已配置绕过负载均衡的外部直连。

历史 `Face` 客户端的旧地址保持原值，统一放在 `trade.legacy-services.*-base-url` 下。这不代表这些历史服务已启用或经过联调。旧 `FileFace` 改为真正读取 `FileServiceUrl` 属性；使用该旧入口必须提供适配旧协议的真实地址，本次不猜测默认地址，也不新增正式功能。

## 4. 绑定、校验与固定协议

- `TradeHttpProperties`：连接／读取超时必须为 1..60000ms，拒绝无限超时。
- `TradeRecoveryProperties`：尝试次数必须为正；重试／扫描间隔为 1 秒..1 天。
- `ProductPriceAuditProperties`：批次 1..50，扫描间隔 1 秒..1 天；显式调用也不能突破配置批次。
- `ReservationLimits`：库存、优惠券、Saga 和交易客户端共享 60 分钟的协议安全上限；部署默认预留时间仍在 YAML。
- `RecoveryStatus`：`NONE`、`PENDING`、`COMPLETED`、`RECONCILIATION_REQUIRED`。
- `OrderEventTypes`：库存／优惠券／结算消费的四种既有跨服务事件名称。
- `TradeHeaders`：统一复用身份与幂等请求头名称。
- `CancellationReason`：沿用 `TIMEOUT`、`BUYER` 的既有存储值。

新代码不要重新在 `@Value`、`@Scheduled` 中写一份非空默认值。空串回退仍用于可选密钥或可选适配器参数的“未配置”语义，不代表接口成功或渠道已就绪。JWT 开关、安全边界开关和模拟支付的默认行为与整理前一致，本次不宣称生产安全配置已验收。

同时修正订单 Nacos YAML 的缩进：`trade.after-sale`、`trade.security` 不应位于 `trade.outbox` 下面，新增测试防止再次出现“Java 默认值掩盖错误配置”的情况。

## 5. 验证边界

新增检查覆盖全部应用的真实导入清单、YAML 重复键、共享与服务默认值绑定、部署覆盖、非法参数拒绝启动、JWT 构造注入、请求地址／TTL／幂等头、文件路径绑定和商品投影批次上限。测试请求由本地替身拦截，不访问真实业务数据。

验证命令：

```bash
./mvnw -B -ntp verify
```

本机 Mockito 若受动态附加限制，按本机已有 Byte Buddy agent 路径显式传入 `-DargLine=-javaagent:...`。实际执行结果与日志见下方验收记录；编译与本地测试不等于 Nacos、MySQL、Neo4j、消息代理或支付渠道联调。本任务不创建提交，不推送远程。

### 本轮验收记录

2026-10-25 08:30:40（北京时间）完成最终代码级验收：

- 全量 Maven `verify`：12 个模块全部成功，退出码 0。
- 当前源码对应 Surefire 报告：502 项测试，失败 0、错误 0、跳过 0；无找不到对应源码的陈旧报告。本轮新增 15 项测试，前序已有测试同样重跑。
- Python 代码／迁移／本地替身检查：25 项通过；输出中的模拟接口成功不算真实业务联调。
- 全部 10 个可执行应用的 JAR 中，核对应用配置导入、服务运行 YAML，以及依赖 `own-common` JAR 中的共享 YAML 均已随包交付，内容与源码一致。
- Java 生产源码扫描：无非空 `${配置名:默认值}` 回退；共享协议值、普通代码常量不按运行配置处理。
- `git diff --check`、本文与 Nacos 说明中的相对文档链接检查通过。
- 未执行：Nacos 服务实际发布／刷新、负载均衡地址切换、外部数据库／消息代理／支付渠道联调。按用户要求只验证代码，不安装或启动这些环境；这些项目不计为通过，也不是本轮环境阻塞。
- 无提交、无远程推送；前序未提交业务改动保留，旧原型和 `work/` 未作修改。

本机日志：`/tmp/mall-configuration-verify-20261025.log`、`/tmp/mall-configuration-python-20261025.log`。临时日志不纳入版本控制。

## 6. 交付复核

按 `ecc:java-coding-standards`、`ecc:springboot-patterns` 实施了配置归属、不可变配置绑定、构造注入和领域常量拆分；按 `ecc:agent-self-evaluation` 复核了范围与证据。下列评分是交付自检，不替代测试结果：

| 维度 | 评分 | 证据与边界 |
| --- | --- | --- |
| 正确性 | 4/5 | 502 项 Java 测试与打包资源核对通过；尚无实际 Nacos 刷新证据，未将其写成通过 |
| 完整性 | 4/5 | 本轮覆盖后端运行默认值、九个现代 HTTP 客户端地址、共享协议及文件根路径；不是前端或所有历史字面量的逐条语义审计 |
| 清晰度 | 4/5 | 提供配置文件位置、优先级和覆盖示例；共享默认值与服务默认值仍需结合查看 |
| 可操作性 | 4/5 | 可直接修改 Nacos 同名键或环境变量，非法核心配置有失败测试；历史 `FileServiceUrl` 仍须使用者提供适配旧协议的地址 |
| 简洁性 | 4/5 | 未引入依赖或通用配置框架；多模块显式导入带来必要的配置与测试篇幅 |

平均 4.0/5，无评分 ≤2 的关键问题。自检判断：可以交付本轮代码整理；用户应能根据上述范围与实测结果复核，不应理解为全商城功能或生产环境已验收。

后续可改进：实际部署时核对 Nacos 覆盖与刷新行为；继续维护各领域的具名校验限制，不把恰好同值但含义不同的数字合并成一个全局常量。两者均不以本轮本地测试冒充已完成。
