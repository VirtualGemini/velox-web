# Velox Persistence Spring Boot Starter

Velox 持久化能力 starter，提供可热插拔的数据源装配、数据库方言发现与 MyBatis Plus 集成；该模块是必选能力模块，不提供 `noop`，而是采用显式失败语义。

## 能力说明

- 基于 `velox.datasource.*` 自动装配主 `DataSource`
- 通过 starter SPI 解析当前激活数据库方言
- 默认装配 MyBatis Plus 分页拦截器
- 允许开发者接管数据源创建、数据源定制、数据库方言实现、MyBatis Plus 定制
- 当持久化配置缺失或不支持时，启动阶段直接抛出 starter 自有异常

## 包结构

- `api`：稳定对外契约，例如 mapper 与持久化上下文
- `spi`：官方支持的扩展点
- `core`：starter 默认实现
- `support`：内建方言与 MyBatis 辅助实现
- `properties`：配置绑定
- `autoconfigure`：分层自动装配
- `exception`、`common`：模块异常与通用消息

## 配置示例

```yaml
velox:
  datasource:
    type: postgresql
    configs:
      postgresql:
        driver-class-name: org.postgresql.Driver
        url: jdbc:postgresql://localhost:5432/velox
        username: postgres
        password: postgres
```

该 starter 不支持关闭。若 `type` 找不到匹配的 `DatabaseDialect` Bean，或当前激活数据源配置不完整，将在启动阶段显式抛出 `PersistenceConfigException`。

该 starter 不预设默认数据库类型，项目必须显式声明 `velox.datasource.type`，避免把运行姿态隐藏在 starter 默认值里。

## 可扩展点

需要局部接管或完全重写时，可实现或覆盖以下 Bean：

- `com.velox.framework.persistence.spi.dialect.DatabaseDialect`
- `com.velox.framework.persistence.spi.dialect.DatabaseDialectResolver`
- `com.velox.framework.persistence.spi.datasource.PersistenceContextFactory`
- `com.velox.framework.persistence.spi.datasource.PersistenceDataSourceFactory`
- `com.velox.framework.persistence.spi.datasource.DataSourceCustomizer`
- `com.velox.framework.persistence.spi.mybatis.MybatisDbTypeResolver`
- `com.velox.framework.persistence.spi.mybatis.MybatisPlusConfigurer`
- `com.velox.framework.persistence.support.datasource.HikariDataSourceCustomizer`

内建 MySQL、PostgreSQL 方言仅在对应 JDBC 驱动位于 classpath 时自动提供；业务也可以自行实现方言并完全替换默认实现。

该 starter 故意不默认注册 `MetaObjectHandler`。`createTime`、`updateTime`、`createBy`、`updateBy` 这类审计字段属于领域约定，不属于可复用持久化默认能力。若项目需要自动填充，请由业务侧显式提供自己的 `MetaObjectHandler` Bean。
