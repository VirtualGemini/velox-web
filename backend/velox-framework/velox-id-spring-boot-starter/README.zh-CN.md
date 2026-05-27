# velox-id-spring-boot-starter

Velox 的热插拔业务主键 starter，默认运行姿态为：

- `strategy=snowflake`
- 数据库和业务主键使用 Snowflake 原始数字串
- Base62 仅作为显式可选编码，用于展示或传输
- 也支持切换为 `uuid`
- `enabled=false` 时切到数据库默认主键路径，但仍保持可注入
- 支持开发者替换核心发号引擎、编码器、数据库操作器，或完全重写 facade

## 能力概览

- `BusinessIdGenerator` 继续作为历史兼容的注入入口。
- `IdGeneratorEngine` 是核心 SPI，开发者可以自定义发号策略。
- `IdCodec` 负责前端可见 ID 的文本编码。
- `DatabaseIdOperator` 负责数据库默认主键发号扩展。
- `enabled=false` 时不会让依赖方注入失败；若外部由 infra 或应用模块提供了 `DatabaseIdOperator`，就会走数据库发号。

## 默认行为

`BusinessIdGenerator` 通过以下通用方法返回最终落库的源值 ID：

- `next()`
- `next("your-domain")`
- `nextGeneratedId("your-domain")`

当 `strategy=snowflake` 时，这些值是十进制 Snowflake 字符串；当 `strategy=uuid` 时，这些值是 UUID 字符串。

如果需要源值或元数据，可以使用：

- `nextGeneratedId(...)`
- `nextSourceId(...)`
- `nextRawId(...)`
- `encode(...)`
- `decode(...)`
- `metadata()`

其中 `encode(...)` / `decode(...)` 用于显式 Base62 编解码，`nextRawId(...)` 仅适用于 Snowflake 这类数值源策略。

## 配置示例

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

`strategy` 支持 `snowflake` 和 `uuid`，未显式配置时默认走 `snowflake`。

其中 `database` 配置段只表达 starter 元数据姿态；数据库初始化、差异告警和表结构对齐必须由提供 `DatabaseIdOperator` 的 infra 或应用模块自行实现。

## 扩展方式

替换核心发号引擎：

```java
@Bean
public IdGeneratorEngine customIdGeneratorEngine() {
    return new MyIdGeneratorEngine();
}
```

替换数据库默认主键发号实现：

```java
@Bean
public DatabaseIdOperator databaseIdOperator() {
    return new MyDatabaseIdOperator();
}
```

完全重写对外 facade：

```java
@Bean
public BusinessIdGenerator businessIdGenerator(IdGeneratorEngine engine, IdCodec codec) {
    return new MyBusinessIdGenerator(engine, codec);
}
```

## Disabled 模式

当 `velox.id.snowflake.enabled=false` 时：

- `BusinessIdGenerator` 仍可注入
- `IdCodec` 仍可使用
- `metadata()` 仍然可读
- starter 可委托外部提供的 `DatabaseIdOperator` 走数据库默认主键路径
- 如果数据库路径不可用，发号方法会显式抛出 `VeloxIdGeneratorException`
