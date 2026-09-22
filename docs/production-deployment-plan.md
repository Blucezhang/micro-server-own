# 生产部署与渠道接入计划

本项目当前的微信、支付宝与结算实现应先以模拟适配器运行；模拟密钥只能用于本地或测试环境，绝不可提交真实私钥、证书或渠道密钥。

## 目标拓扑

```text
Internet -> WAF / Ingress -> API Gateway (JWT verification) -> service pods
                                              |-> MySQL primary + backup
                                              |-> Neo4j
                                              |-> Redis (rate limit / token revocation)
                                              |-> Object storage
                                              `-> Prometheus / logs / traces
```

## 环境与密钥

- 使用三套隔离环境：dev、staging、production；数据库、对象存储桶、JWT 签名密钥和支付商户号不可复用。
- Kubernetes Secret 或云 KMS 保存 `JWT_SIGNING_KEY`、`INTERNAL_SERVICE_TOKEN`、本地演示用 `PAYMENT_MOCK_CALLBACK_TOKEN`、微信 API v3 Key/商户私钥、支付宝应用私钥；配置仓库只保留变量名。
- `INTERNAL_SERVICE_TOKEN` 只在服务工作负载间传递，用于库存、优惠券、地址快照、举证文件、订单回调和结算运维的内部路径；不得由浏览器、移动端或日志输出携带。轮换时应先让调用端支持新旧密钥重叠，再逐步切换被调服务并撤销旧值。
- Ingress 必须启用 TLS、回调域名白名单和限流；支付回调仅允许渠道 IP 或 mTLS/VPN 网络入口。
- 当前 `/settlement/api/v1/payments/mock-callbacks/{channel}` 只用于演示：它校验独立回调密钥、渠道、渠道流水和金额。接入真实渠道时必须由微信/支付宝验签和渠道流水去重替换，不能继续暴露该模拟入口。

## 结算规则（当前约定）

- 支付成功后为商家记 80% 应收、平台记 20% 服务费；退款从尚未结算的商家应收中直接冲回。
- 每周生成一次结算批次；可提现余额大于等于 100.00 元才可申请提现。
- 模拟支付渠道直接将回调视为成功；真实渠道接入前必须替换为验签、金额核对、渠道流水去重、对账与人工差错处理。

## 发布与回滚

1. 在 staging 运行迁移备份演练、渠道沙箱回调、库存/订单并发和结算对账测试。
2. 生产迁移只允许前向脚本，先备份并验证恢复；滚动发布时保持旧/新 API 兼容。
3. 监控支付回调失败、Outbox 积压、库存负数保护、结算批次差错、JWT 验签失败和提现失败。
4. 回滚应用镜像而不回滚已执行的数据库迁移；由补偿脚本修复业务状态。
