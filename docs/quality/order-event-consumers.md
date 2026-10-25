# 订单消息处理范围与事务边界

更新：2026-10-25。此版本不打开 RocketMQ 开关，不改变 Broker 数据或消费位点；未运行真实 Broker。

## 当前真正处理的事件

| 接收方 | 事件 | 实际动作 |
| --- | --- | --- |
| 库存 | ORDER_CANCELED、ORDER_EXPIRED | 调用本地 `InventoryService.releaseOrder`，沿用库存锁及状态保护 |
| 营销 | ORDER_CANCELED、ORDER_EXPIRED | 调用本地 `CouponService.releaseOrder`，沿用券归属和释放语义 |
| 结算 | PAYMENT_SUCCEEDED、REFUND_SUCCEEDED | 找到已有付款，调用现有 `SettlementService.recover`，继续原协调流程；不创建付款或第二套资金账本 |

消息是既有恢复流程的补充，不替换订单/结算事实或启动另一个协调器。取消事件在取消完成后才产生，因此库存/券消费者主要是幂等确认，不宣称仅靠这些消息即可恢复尚未完成的取消。

## 消费原子性

移除公共“只登记已消费”的占位消费者。`TradeMessageInboxService.consume` 在同一 JPA 事务插入去重记录并执行接收服务的业务动作；动作失败或提交失败，两者一起回滚，调用者能观察到异常并重投。结算恢复意图仍按其既有设计独立保存，实际付款/退款账本与 Inbox 加入同一外层本地事务。

消息 ID 与消费者唯一索引保留。移除 `INSERT IGNORE`，不隐藏无关完整性错误；并发唯一键竞争向调用方报错，重投后读取已完成记录。相同消息 ID 改变内容会拒绝；指纹按稳定键序 JSON 计算，避免 Map 遍历顺序造成误报。未归该服务处理的事件不会登记为已完成业务。

## 历史切换

新业务消费者使用 `inventory-order-effects-v1`、`promotion-order-effects-v1`、`settlement-order-effects-v1` 的 Inbox 命名空间。旧 `*-saga` 的去重记录来自无业务处理的占位消费者，且使用旧指纹算法；保留它们作为历史，不能据此认定新业务已完成，也不对旧哈希做宽松双读。

本次保留原 Broker group 和位点，不自动重放历史。需要补处理旧消息时，应另行授权，按权威业务事实筛选、保留审计后重放；不能简单清空 Inbox 或重置全部位点。部署需同步 Nacos 的新 consumer-name，避免旧命名空间阻止处理。

## 已验证与未完成

H2/JPA Inbox 持久化测试 6 项覆盖实际本地业务 SQL 与去重记录共同回滚、提交失败、重复消息、异内容冲突、嵌套 Map 顺序及消费者隔离。库存/券消费者各 2 项，结算消费者 3 项验证实际动作委派、无付款时报错和未归属事件不吞入 Inbox；这些委派测试使用 mock 服务。

仍未验证真实投递确认、Broker 重试/死信、跨实例竞争及完整事件乱序。尚未实现所有 Saga 步骤的消息化、死信处置台和告警；不能将本文件称为全链路异步化或 exactly-once 证明。
