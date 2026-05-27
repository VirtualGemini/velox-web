# Velox Security Spring Boot Starter

Velox 的基础安全能力 starter，只承载安全基础设施，不承载账号业务策略。

## 能力边界

本模块只负责：

- token runtime / provider 选择
- session facade
- 权限 SPI 与鉴权服务
- 安全注解与切面
- 密码哈希与升级

本模块不负责：

- 登录策略
- 图形验证码策略
- 验证码发送与校验策略
- 账号登录方式
- MFA 流程
- 邮箱换绑流程

这些业务能力统一归属 `velox-system`。

## 配置

仅保留基础安全配置：

```yaml
velox:
  security:
    swagger-public-enabled: false
    password:
      algorithm: bcrypt
      upgrade-on-login: true
    token:
      mode: stateful
      token-name: Authorization
      timeout: 86400
```

## 约束

- 不要把 `SecuritySessionService` 继续扩展成登录业务服务。
- `velox-framework` 内各 starter 之间禁止相互依赖。
