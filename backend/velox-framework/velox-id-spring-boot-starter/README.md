# velox-id-spring-boot-starter

Hot-pluggable business ID starter for Velox. The default runtime posture is:

- `strategy=snowflake`
- database/business IDs use raw Snowflake numeric strings
- Base62 is available as an explicit codec for display or transport when needed
- developers can switch to `uuid`
- `enabled=false` delegates issuance to database-default ID objects while keeping beans injectable
- developers can override the core engine, codec, database operator, or the whole facade

## Features

- `BusinessIdGenerator` keeps the original injection entry point.
- `IdGeneratorEngine` is the core SPI for custom generation strategies.
- `IdCodec` controls the frontend-facing textual representation.
- `DatabaseIdOperator` is the SPI for database-default issuance.
- `enabled=false` still registers injectable beans and metadata, and can issue IDs from the database path when an external `DatabaseIdOperator` is provided by infra or the application.

## Default Behavior

`BusinessIdGenerator` returns persisted/source IDs for generic methods such as:

- `next()`
- `next("your-domain")`
- `nextGeneratedId("your-domain")`

For `strategy=snowflake`, these values are decimal Snowflake strings. For `strategy=uuid`, they are UUID strings.

Use `nextGeneratedId(...)`, `nextSourceId(...)`, `nextRawId(...)`, `encode(...)`, `decode(...)`, and `metadata()` when you need explicit source values, Base62 display values, or starter metadata. `nextRawId(...)` is only available for numeric-source strategies such as Snowflake.

## Configuration

```yaml
velox:
  id:
    enabled: true
    strategy: snowflake
    database:
      init-mode: update
      warn-on-prod-mismatch: true
    snowflake:
      worker-id: 1
      datacenter-id: 1
      twepoch: 1288834974657
      time-offset: 2000
      use-system-clock: false
```

`strategy` supports `snowflake` and `uuid`. If not configured, it defaults to `snowflake`.

The `database` subtree is starter-owned metadata only. Concrete database initialization, mismatch warning, and schema reconciliation must be implemented by infra or the application module that provides `DatabaseIdOperator`.

## Extension

Override the core engine:

```java
@Bean
public IdGeneratorEngine customIdGeneratorEngine() {
    return new MyIdGeneratorEngine();
}
```

Override database-default issuance:

```java
@Bean
public DatabaseIdOperator databaseIdOperator() {
    return new MyDatabaseIdOperator();
}
```

Completely replace the public facade:

```java
@Bean
public BusinessIdGenerator businessIdGenerator(IdGeneratorEngine engine, IdCodec codec) {
    return new MyBusinessIdGenerator(engine, codec);
}
```

## Disabled Mode

When `velox.id.snowflake.enabled=false`:

- `BusinessIdGenerator` is still injectable
- `IdCodec` is still usable
- `metadata()` still works
- the starter can delegate issuance through an externally provided `DatabaseIdOperator`
- if the database path is unavailable, issuance fails explicitly with `VeloxIdGeneratorException`
