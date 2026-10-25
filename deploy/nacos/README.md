# Nacos 配置交付

`config/` 是平台切换后唯一会被 Nacos 导入的服务配置。Data ID 使用文件名，Group 默认是 `MICRO_SERVER`，Namespace 默认是 `public`。

```bash
export NACOS_SERVER=http://localhost:8848
export NACOS_USERNAME=nacos
export NACOS_PASSWORD='replace-with-a-real-secret'
bash scripts/import-nacos-config.sh
```

脚本先通过 Nacos 3 Admin API 登录取得短期 `accessToken`，再逐个发布配置；同一 Data ID、Group、Namespace 的重复执行会更新配置，不会创建副本。

服务必须使用 `spring.config.import` 加载这里发布的 Nacos 配置；仓库已不再包含旧 Config Server 配置仓库。

Java 后端的运行默认值按“共享 `trade-runtime.yaml` → 服务 `*-runtime.yaml` → Nacos”导入；Nacos 中同名配置覆盖打包默认值，密钥仍从部署环境提供。不要在 Java 的 `@Value` 或定时任务注解里再维护一份非空默认值。配置归属、覆盖示例与校验边界见[后端配置与常量规范](../../docs/quality/backend-configuration-conventions.md)。
