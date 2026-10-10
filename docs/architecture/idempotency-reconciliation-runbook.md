# 幂等 `PROCESSING` 记录只读排查手册

> 状态：只读人工排查，不提供记录修复或解锁能力。适用于业务结果不确定、同一请求键持续返回 `409` 的事件。

## 先记住的边界

- 幂等记录按 `service_name + actor_id + actor_type + request_path + idempotency_key` 唯一限定；请求指纹为请求参数序列化后的 SHA-256。
- `COMPLETED` 记录在保留期内重放缓存的 `Resp`。超过保留期后，可在后续请求到来时删除并重新执行。
- `PROCESSING` 即使 `expires_at` 已过期也不会自动删除；同键请求继续返回 `409`。过期仅表示进入排查候选，不证明原请求已停止或业务未执行。
- 控制器执行抛错时，幂等记录会被删除；但记录完成保存失败时，业务可能已提交而记录仍为 `PROCESSING`。跨服务操作也不具备全局事务或 exactly-once 保证。
- 当前没有系统管理 API 或受支持的 SQL 操作来完成、删除或解锁不确定记录。不要直接修改 `trade_idempotency`。

## 只读排查步骤

1. 从调用方取得服务/操作、actor ID 与类型、请求路径、幂等键、发生时间和关联 ID。不要索取密码、访问令牌或支付凭据；不要把原始幂等键粘贴到 shell 命令、工单或共享日志中。
2. 暂停对该业务命令的自动重试。不要换一个新幂等键重发；这会绕过保护并可能重复副作用。记录重试返回的 `409` 及时间。
3. 在获准的只读数据库会话中，使用预编译语句绑定所有参数，检查精确作用域。`request_path` 应使用服务端实际记录的路径；网关前缀可能已被路由剥除。只读摘要，不读取 `response_body`。

```sql
SELECT id,
       service_name,
       actor_id,
       actor_type,
       request_path,
       SHA2(idempotency_key, 256) AS key_fingerprint,
       request_hash,
       status,
       response_status,
       response_body IS NOT NULL AS has_cached_response,
       created_at,
       expires_at,
       completed_at
FROM trade_idempotency
WHERE service_name = ?
  AND actor_id = ?
  AND actor_type = ?
  AND request_path = ?
  AND idempotency_key = ?;
```

不要在查询结果、截图或工单中展示原始 `idempotency_key` 或 `response_body`。若数据库工具会自动展开所有列，请改用显式字段查询。
`response_status` 对应缓存 `Resp.status` 字段，不等同于 HTTP 传输状态码。

4. 确认应用实例没有仍在执行该请求，按关联 ID、actor 与时间窗查阅拥有该业务操作的服务日志。日志没有业务明细不能作为“未执行”的证据。
5. 由对应领域数据所有者执行只读核对。例如订单创建需分别核对订单/Saga、子订单/明细及库存或优惠券预占；支付需核对支付流水、订单状态和结算记录；图谱与 MySQL 写操作应分别核验。具体核对必须遵循该服务已有的数据所有权和访问审批，不能从单条幂等记录推断其他存储的结果。
6. 记录证据与结论。若结果完整且唯一，保持现有记录不变，由业务负责人通过既有只读业务视图告知调用方结果；若发现部分副作用或无法证明结果，保持 `PROCESSING` 并升级给服务所有者。不要用新键重放，也不要为了“恢复可用”清理记录。

## 明确禁止

- `DELETE`、`UPDATE`、手工改状态、改过期时间、改请求指纹或改业务键。
- 对 `PROCESSING` 行调用通用清理脚本，或仅凭 `expires_at` 已过期而重试。
- 从日志或缓存复制凭据；查询 `response_body` 以寻找原请求内容。该表只保存响应，不保存请求体。

## 建立自动恢复前的前置条件

自动化恢复必须先定义有权限的操作者、审计字段、在途请求判定、每类业务副作用的核验器、部分提交的处理方式、双人复核/批准要求、并发锁与可回滚策略，并有真实依赖故障注入测试。泛化地把 `PROCESSING` 改为 `COMPLETED` 或删除后放行重试都不安全；这些策略尚未定稿，当前手册不授权任何数据修复操作。
