# Velox Persistence Spring Boot Starter

Persistence capability starter for Velox. It provides hot-pluggable datasource assembly, database dialect discovery, and MyBatis Plus integration while remaining a required module with explicit failure semantics instead of `noop`.

## Features

- Auto-configures a primary `DataSource` from `velox.datasource.*`
- Resolves the active database dialect through starter SPI beans
- Configures MyBatis Plus pagination by default
- Supports developer-owned overrides for datasource creation, datasource customization, dialect implementation, and MyBatis Plus configuration
- Fails fast with starter-owned exceptions when required persistence capability is misconfigured

## Package Topology

- `api`: stable user-facing contracts such as mapper and persistence context
- `spi`: supported extension seams for dialects and datasource assembly
- `core`: default starter implementations
- `support`: built-in dialects and MyBatis helpers
- `properties`: configuration binding
- `autoconfigure`: layered Spring Boot auto-configuration
- `exception`, `common`: starter-owned failure types and reusable messages

## Configuration

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

This starter is required. Do not disable it. If the configured `type` has no matching `DatabaseDialect` bean or the active datasource config is incomplete, startup fails explicitly with `PersistenceConfigException`.

The starter does not assume a default database type. `velox.datasource.type` must be declared explicitly by the project, so runtime posture stays visible in configuration rather than hidden in starter defaults.

## Extension Seams

Implement or override these beans when you need to replace core behavior:

- `com.velox.framework.persistence.spi.dialect.DatabaseDialect`
- `com.velox.framework.persistence.spi.dialect.DatabaseDialectResolver`
- `com.velox.framework.persistence.spi.datasource.PersistenceContextFactory`
- `com.velox.framework.persistence.spi.datasource.PersistenceDataSourceFactory`
- `com.velox.framework.persistence.spi.datasource.DataSourceCustomizer`
- `com.velox.framework.persistence.spi.mybatis.MybatisDbTypeResolver`
- `com.velox.framework.persistence.spi.mybatis.MybatisPlusConfigurer`
- `com.velox.framework.persistence.support.datasource.HikariDataSourceCustomizer`

Built-in MySQL and PostgreSQL dialect beans are only provided when the corresponding JDBC drivers are on the classpath. Custom dialects can replace them completely.

This starter intentionally does not auto-register a `MetaObjectHandler`. Audit fields such as `createTime`, `updateTime`, `createBy`, or `updateBy` are domain conventions, not reusable persistence defaults. If a project needs auto-fill behavior, it should provide its own `MetaObjectHandler` bean.
