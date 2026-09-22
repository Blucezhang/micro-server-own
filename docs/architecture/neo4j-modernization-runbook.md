# Neo4j 数据访问现代化运行手册

> 状态：代码层查询迁移与单元测试已完成；尚未对真实 Neo4j 数据执行备份、恢复和业务回归。

## 目标与边界

将遗留 Neo4j OGM 风格的数据访问迁移到 Spring Data Neo4j 的现代驱动模型，保持现有节点标签、属性、关系方向和业务接口不变。本阶段不修改 MySQL 交易表，也不在没有备份的共享数据库上执行 DDL、DML 或删除操作。

## 已完成的代码迁移

| 服务 | 已迁移 DAO |
| --- | ---: |
| `own-user-party` | `BaseDao`、`LoginUserDao`、`RoleDao`、`FunDao`、`PersonDao`、`OrgDao` |
| `own-product` | `BaseDao`、`ProductDao`、`CategoryDao`、`PersonDao`、`OrgDao` |
| `own-promotion` | `BaseDao`、`ProductDao`、`PromotionDao`、`MallTicketDao`、`StoreTicketDao`、`SellerDao`、`PromotionTypeDao`、`ScopeDao` |

上述 DAO 的 `START`、`node(...)` 和 OGM `{0}` 位置参数已替换为现代 `MATCH` 与 `$0` 参数绑定；`CategoryDao` 接收任意 Cypher 文本的测试入口已移除。通用 DAO 中关系类型不能安全参数化，因此改为固定的 `DATA` 或 `CONTAIN` 方法；旧的动态关系方法会明确拒绝调用，避免重新引入字符串拼接 Cypher。

已通过的本地测试：`own-user-party`、`own-product`、`own-promotion` 及其公共模块依赖链。它们证明源代码、DAO 注解和 Mock 契约可构建、可执行，但不证明目标 Neo4j 中的实际图数据已完成迁移。

## 真实数据迁移顺序

1. 在隔离环境导出 Neo4j 3.5 数据并恢复到目标 Neo4j；记录节点、关系、标签和关键属性计数。
2. 将应用指向恢复后的副本，确认 Bolt URI、认证方式与目标 Neo4j 服务端版本匹配。
3. 分服务执行商品、用户、促销读写和关系契约测试，再执行真实副本抽样比对。
4. 验证固定 `DATA`、`CONTAIN`、`JOIN`、`BELONG` 等关系的方向与数量；若发现历史数据依赖动态关系类型，先补专用 DAO 方法，不得恢复动态 Cypher。
5. 只有备份、恢复演练和应用验证均成功后，才允许 Compose 或生产部署使用新的 Neo4j 配置。

## 迁移前检查

```bash
rg -n '@Query\(' \
  own-user-party/src/main/java/com/own/user/party/dao \
  own-product/src/main/java/com/own/product/dao \
  own-promotion/src/main/java/com/own/promotion/dao | rg 'START|node\(|\{[0-9]+\}'

JAVA_HOME=<jdk17> ./mvnw -B -ntp clean verify
```

## 真实环境验收

- 迁移前后按标签统计节点数，按关系类型统计关系数。
- 抽样验证 `LoginUser → Role → Fun`、`Product → Category`、`Promotion → Scope` 的方向与属性。
- 验证登录/RBAC、商品查询、商品运营、促销试算和券领取的读写路径。
- 失败时从备份恢复；应用配置不切换到新库。

仅有本地编译或 Mock 测试通过时，不能声称 Neo4j 数据已升级。
