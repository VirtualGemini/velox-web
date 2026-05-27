# Velox Development Spec

本文档描述 `velox-pro` 当前架构下的开发规范，重点约束模块边界、依赖方向、职责划分与禁止项。

## 1. 总体方向

当前项目按六层组织：

- `dependencies`：所有依赖管理模块
- `common`：存放业务公共模块
- `framework`：一切可独立可复制、配置驱动、启用/禁用语义、API/SPI/NoOp、自包含边界
- `infra`：基建模块
- `system`：系统业务模块
- `server`：项目启动模块

依赖方向必须保持单向：

`velox-dependencies`
-> `velox-common`
-> `velox-framework/*`
-> `velox-infra/*`
-> `velox-system`
-> `velox-server`

禁止反向依赖、禁止循环依赖、禁止把业务代码重新塞回 framework 或 starter。

## 2. 各模块职责

### 2.1 dependencies

职责：

- 统一第三方依赖版本
- 统一各模块依赖版本
- 收口纯 BOM、版本号、依赖治理

约束：

- 不承载业务代码
- 不承载 Spring Boot 自动装配
- 不承载 API、SPI、实体、Mapper、配置类

### 2.2 common

职责：

- 提供业务公共语言和公共模型
- 放业务共享的返回结构、异常体系、多语言文案、基础 DDD 抽象
- 为 `system` 提供公共表达，而不是技术 starter

当前适合放入 `common` 的内容：

- `Result`、`PageResult`
- 公共异常、错误码
- 业务共享枚举
- 多语言文案
- 业务基类、审计基类、分页基类等

约束：

- `common` 是业务共享层，不是万能工具箱
- 不把可独立复用的技术能力做进 `common`
- 不把系统业务用例、业务流程、业务 provider 放进 `common`

### 2.3 framework

职责：

- 只放可以被外部项目直接依赖的完整能力模块
- 每个模块都必须具备明确边界、独立价值、配置驱动和启用/禁用语义
- 暴露清晰的 API / SPI / NoOp / AutoConfiguration

framework 模块必须满足：

- 能脱离当前业务单独复用
- 是完整能力，不是半成品零件
- 只装配自己，不扫描整站业务
- disabled 时行为显式，不允许静默失效

当前 `velox-framework` 下实际存在：

- `velox-web-spring-boot-starter`
- `velox-security-spring-boot-starter`
- `velox-persistence-spring-boot-starter`
- `velox-file-spring-boot-starter`
- `velox-email-spring-boot-starter`
- `velox-id-spring-boot-starter`
- `velox-redis-spring-boot-starter`

约束：

- framework 只能提供完整能力，不能只暴露半成品零件
- 一个 starter 可以在模块内部组合多项技术细节，但对外必须是单一能力入口
- 纯第三方依赖模块禁止继续以 `starter` 名义存在于 framework
- starter 不允许依赖 `velox-common`
- 禁止新增 `velox-core-starter`、`velox-all-starter` 这类无边界聚合模块
- 禁止为了技术拆分而新增 `velox-mysql-starter`、`velox-postgresql-starter`、`velox-redis-driver-starter` 这类对外无消费价值的模块

### 2.4 infra

职责：

- 承接产品级基建装配
- 组合 framework 能力，形成当前产品默认接线方式
- 放产品级扫描策略、基础设施组合策略、少量无法通用化的装配规则

当前 `infra` 现状：

- 顶层为 `velox-infra`
- 当前已落地模块为 `velox-infra-persistence`

infra 只做三件事：

- 声明产品级扫描策略
- 组合 framework 原子能力并暴露默认入口
- 承载少量无法继续抽象到 framework 的装配规则

不应该放入 infra 的内容：

- 业务用例
- Controller
- 业务错误码
- 业务枚举
- 领域实体
- 业务 Mapper

### 2.5 system

职责：

- 只放系统业务能力和产品实现
- 只消费 `common`、`framework`、`infra` 提供的能力
- 承载当前产品的数据模型、业务用例、业务 provider 实现

当前业务范围包括：

- 认证登录
- 用户管理
- 角色权限
- 菜单能力
- 文件资源管理
- 数据库存储型文件 provider

包结构约束：

- `com.velox.module.system.auth`
- `com.velox.module.system.user`
- `com.velox.module.system.role`
- `com.velox.module.system.menu`
- `com.velox.module.system.permission`
- `com.velox.module.system.file`

禁止项：

- system 新增 framework 组合逻辑
- system 新增 starter 自动装配逻辑
- controller 直接依赖 Mapper
- 一个业务模块直接依赖另一个业务模块的实现类

### 2.6 server

职责：

- 作为唯一启动模块
- 提供 Spring Boot 启动入口
- 承载应用级配置文件、环境配置、日志配置

约束：

- 不承载业务实现
- 不承载基础能力实现
- 不承载通用 starter

## 3. 自动装配规则

带运行时装配逻辑的 starter 才需要提供：

- 自己的 `pom.xml`
- 自己的 `AutoConfiguration`
- 自己的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

禁止：

- 扫描整个 `com.velox`
- framework starter 互相做大范围 `@ComponentScan`
- 在自动装配类中写业务逻辑

当前实现中：

- framework 里的能力 starter 只装配自己
- `velox-infra` 负责把 persistence 等能力组合成当前产品默认形态
- infra 侧组合配置应集中在独立的 infra 包边界内，不要扩散到 system 或 server

## 4. 配置规则

当前代码已经完成的是模块拆分，不是配置命名空间迁移。

因此现阶段仍保留已有低层配置入口：

- `velox.datasource.*`
- `velox.security.*`
- `velox.email.*`
- `velox.file.*`

后续目标才是统一收口到：

```yaml
velox:
  infra:
    persistence:
    cache:
    lock:
    file:
    mail:
```

没有完成迁移前，不要在文档里把 `velox.infra.*` 写成“已实现”。

## 5. 新增模块判定标准

判断一个新模块该放哪层：

放 framework：

- 它是不是一个对外可直接消费的完整能力
- 它能不能脱离业务独立复用

例如：

- Web 基础能力
- 安全基础能力
- 持久化能力
- 邮件发送
- 文件存储抽象
- Redis 缓存能力

放 infra：

- 它是不是在做产品级装配和扫描策略

例如：

- Mapper 扫描与业务级持久化组合
- 当前产品默认能力接线方式

放 system：

- 它是不是业务用例
- 它是不是当前产品的数据模型或 provider 实现

例如：

- 登录注册
- 用户管理
- 角色菜单
- 文件资源管理
- 数据库存储 provider

## 6. 当前重构边界

本次已落地：

- `velox-common` 作为顶层业务共享模块
- capability starter 平铺到 `velox-framework/*`
- `velox-infra` 作为产品级基建接入层
- `velox-infra-persistence` 负责声明当前产品启用 persistence starter
- `velox-file-spring-boot-starter` 只保留 SPI、通用 provider 与自动装配
- 数据库存储 provider 与业务文件模型保留在 `velox-system`
- `velox-server` 作为唯一启动模块

本次没有完成：

- `velox.infra.*` 配置命名空间迁移
- 将 system 中仍然直接使用的部分三方类进一步收口到 infra 门面

## 7. 保留禁止项

以下禁止项继续有效，不因文档重写而改变：

- 禁止反向依赖
- 禁止循环依赖
- 禁止把业务代码重新塞回 framework 或 starter
- 纯第三方依赖模块禁止继续以 `starter` 名义存在于 framework
- starter 不允许依赖 `velox-common`
- 禁止新增 `velox-core-starter`、`velox-all-starter` 这类无边界聚合模块
- 禁止为了技术拆分而新增 `velox-mysql-starter`、`velox-postgresql-starter`、`velox-redis-driver-starter` 这类对外无消费价值的模块
- system 新增 framework 组合逻辑
- system 新增 starter 自动装配逻辑
- controller 直接依赖 Mapper
- 一个业务模块直接依赖另一个业务模块的实现类
- 扫描整个 `com.velox`
- framework starter 互相做大范围 `@ComponentScan`
- 在自动装配类中写业务逻辑
- 禁止使用 lombok

## 8. 一句话原则

`dependencies` 管版本，`common` 管业务公共语言，`framework` 管可插拔能力，`infra` 管产品级基建装配，`system` 管系统业务实现，`server` 管启动与配置。
