# 框架升级兼容性基线

本文记录 P0 的可重复审计入口，不把静态匹配数量等同于实际改造工作量。

## 运行方式

```bash
# 输出当前阻断项数量，P0/P1 迁移期间使用
bash scripts/framework-upgrade-inventory.sh

# 在 P1 完成后作为门禁；仍有匹配项时返回非零状态
bash scripts/framework-upgrade-inventory.sh --check
```

检查范围包括 Jakarta API、Neo4j OGM/旧 Repository、Eureka/Config/Zuul/旧 Feign、Springfox、旧 MySQL 驱动、Spotify Docker 插件和 `bootstrap` 配置。

## 判读规则

- `javax.crypto`、`javax.crypto.spec` 属于 JDK API，不迁移为 Jakarta。
- 匹配行数用于观察趋势；是否完成必须结合编译、测试、配置和真实数据副本验证。
- P0 允许报告阻断项，但必须保持旧框架构建可用。
- P1 的 `--check`、JDK 17 `clean verify` 和过时引用检查必须同时通过。
- Neo4j 代码 API 兼容在 P1 解决；服务端版本、备份恢复和真实数据验证在 P4 完成。

## 当前已确认的高风险点

1. 父 POM 的无条件运行依赖已在 P0 收敛；版本仍由根 POM/BOM 管理，各模块只声明实际使用的依赖。
2. JPA、Servlet 和 Mail 使用大量 `javax.*`；不能用全局文本替换误伤 `javax.crypto`。
3. 用户与商品服务仍使用 Neo4j OGM 注解、`GraphRepository` 和 `Neo4jOperations`。
4. 网关基于 Zuul Servlet Filter，迁移到 Gateway 时必须改为 WebFlux 模型并重新验证身份头边界。
5. 配置仍由 Config Server 和 `bootstrap` 加载；Nacos Data ID、Group、Namespace 与本地默认值需要逐项映射。
6. MySQL 5.7 和 Neo4j 3.5 只能作为迁移输入，不应被描述为目标生产运行版本。

## 2026-09-22 基线结果

- 初始审计匹配 538 行。P0 将父 POM 的隐式依赖展开为模块显式声明后，当前匹配 558 行：Jakarta 399、Neo4j 57、服务治理 37、Springfox 15、旧 MySQL 18、Spotify Docker 插件 22、`bootstrap` 文件 10。数量增加反映了真实依赖归属，不是新增运行功能。
- `--check` 按预期返回失败；该门禁应在 P1 清零后启用。
- 使用 Amazon Corretto 8 和 Maven Wrapper 3.9.16 执行全量 `clean verify` 成功。
- 为恢复可信基线，Springfox 启用注解改为 2.10 对应的 Web MVC 注解；两处 Jackson `readTree` 调用按旧版本签名捕获 `IOException`。
