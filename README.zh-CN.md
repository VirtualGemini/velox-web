<h2 align="center" id="top">Velox Pro</h2>
<p align="center">Velox 管理后台的后端实现，基于分层 DDD 和可插拔 starter 组织。</p>
<div align="center"><a href="./README.md">English</a> | 简体中文</div>

<br />

## 项目简介

`velox-pro` 是 `velox` 项目的后端，负责认证、权限、用户、菜单、文件、邮件与基础能力装配。

## 技术栈

- Java 25
- Spring Boot 3.4.5
- Maven 3.9+
- MyBatis-Plus 3.5.12
- MySQL 8.4.0
- PostgreSQL 42.7.7
- Redis 7
- Sa-Token 1.40.0
- SpringDoc OpenAPI 2.8.6
- Spring Validation
- Spring AOP
- Hutool 5.8.34
- Guava 33.4.8-jre
- EasyCaptcha 1.6.2
- JavaMail
- AWS SDK S3 2.25.0
- Tika 2.9.2
- JSch 0.2.19
- Commons Net 3.12.0

## 模块结构

```text
velox-dependencies  # 依赖版本治理
velox-common        # 业务公共模块
velox-framework     # 可插拔、可复制、配置驱动的能力 starter
velox-infra         # 基建装配层
velox-system        # 系统业务模块
velox-server        # 启动模块
```

## 当前实现功能

- 登录、退出、注册、验证码、找回密码
- 当前用户信息、资料、密码、头像管理
- 用户、角色、菜单管理
- 角色菜单权限配置
- 文件上传、创建、查询、删除、批量删除
- 文件预签名上传与下载
- 文件配置管理、启停、主配置切换、连通性测试
- 文件存储支持 local、FTP、SFTP、S3-compatible
- 邮件发送能力
- 统一返回、异常处理、请求日志、链路追踪、操作日志
- ID 生成能力

## 本地开发

环境要求：

- JDK 25+
- Maven 3.9+
- MySQL 8 / PostgreSQL 14+
- Redis 7

```bash
cd script/docker
docker compose up -d
```

```bash
mvn clean compile
```

```bash
cd velox-server
mvn spring-boot:run
```

## License

MIT
