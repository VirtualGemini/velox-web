<h2 align="center" id="top">Velox Web Pro</h2>
<p align="center">基于 Vue 3 前端与 Spring Boot 后端的全栈后台管理系统。</p>
<div align="center"><a href="./README.md">English</a> | 简体中文</div>

<br />

## 项目简介

`velox-web-pro` 是一个全栈管理后台项目，提供 Web 控制台、后端服务、认证鉴权、权限管理、系统管理、文件管理和基础设施能力。

## 仓库架构

```text
velox-web-pro/
├── frontend/              # Vue 3、TypeScript、Vite 前端应用
│   ├── public/            # 前端静态资源
│   └── src/               # 前端源码
│       ├── api/           # 后端接口封装
│       ├── assets/        # 源码静态资源
│       ├── components/    # 可复用 Vue 组件
│       ├── config/        # 前端配置
│       ├── directives/    # Vue 指令
│       ├── enums/         # 前端共享枚举
│       ├── hooks/         # 组合式 hooks
│       ├── locales/       # 国际化资源
│       ├── plugins/       # 前端插件
│       ├── router/        # 静态路由与动态路由
│       ├── store/         # Pinia 状态管理
│       ├── types/         # TypeScript 类型定义
│       ├── utils/         # 前端工具函数
│       └── views/         # 页面视图
└── backend/               # Java 25、Spring Boot、Maven 后端应用
    ├── script/            # 本地脚本与 Docker 依赖环境
    ├── velox-dependencies # 依赖版本治理
    ├── velox-common       # 业务公共语言和公共模型
    ├── velox-framework    # 可插拔能力 starter
    ├── velox-infra        # 产品级基建装配
    ├── velox-system       # 系统业务模块
    └── velox-server       # 应用启动模块
```

## 当前能力

- 认证：登录、退出、注册、验证码、找回密码与重置密码。
- 当前用户：资料、密码、头像和账号信息管理。
- 系统管理：用户、角色、菜单和角色菜单权限配置。
- 文件中心：上传、查询、删除、批量删除、预签名上传与下载。
- 文件配置：新增、编辑、启停、主配置切换、连通性测试。
- 基础能力：统一返回、异常处理、请求日志、链路追踪、操作日志、ID 生成、邮件发送。
- 前端接入：动态路由、权限码绑定、Token 持久化、统一请求层和错误处理。

## 环境要求

- JDK 25+
- Maven 3.9+
- Node.js >= 20.19.0
- pnpm >= 8.8.0
- MySQL 8 或 PostgreSQL 14+
- Redis 7
- Docker 与 Docker Compose，推荐用于启动本地依赖

## 本地开发

启动后端依赖：

```bash
cd backend/script/docker
docker compose up -d
```

启动后端：

```bash
cd backend
mvn clean compile
cd velox-server
mvn spring-boot:run
```

启动前端：

```bash
cd frontend
pnpm install
pnpm dev
```

默认本地地址：

- 前端：`http://localhost:3006`
- 后端：`http://localhost:8080`

前端本地开发通过 Vite Proxy 将 `/api` 请求转发到后端服务。常用环境变量：

```bash
VITE_ACCESS_MODE=backend
VITE_API_URL=/
VITE_API_PROXY_URL=http://localhost:8080
VITE_WITH_CREDENTIALS=false
```

## 常用命令

后端：

```bash
cd backend
mvn clean compile
```

前端：

```bash
cd frontend
pnpm lint
pnpm build
pnpm serve
```

## 文档

- [Contribution Guide](./CONTRIBUTING.md) / [贡献指南](./CONTRIBUTING.zh-CN.md)
- [Development Spec](./docs/DEVELOPMENT_SPEC.md) / [开发规范](./docs/DEVELOPMENT_SPEC.zh-CN.md)
- [Commit Spec](./docs/COMMIT_SPEC.md) / [提交规范](./docs/COMMIT_SPEC.zh-CN.md)
- [Changelog](./CHANGELOG.md) / [更新日志](./CHANGELOG.zh-CN.md)
- [免责声明](./DISCLAIMER.zh-CN.md) / [English](./DISCLAIMER.md)
- [使用条款](./TERMS_OF_USE.zh-CN.md) / [English](./TERMS_OF_USE.md)

## 许可证

本仓库整体采用 MIT License（详见 [LICENSE](./LICENSE)），根许可证中亦说明了各组成部分的许可情况。后端（`backend/`）与前端（`frontend/`）各自附带独立的 LICENSE 文件；其中前端源自某第三方开源项目，保留了其原始的 MIT 许可证与版权声明。
关于额外的风险分配、保证免责声明和使用条件，详见 [DISCLAIMER.zh-CN.md](./DISCLAIMER.zh-CN.md) 与 [TERMS_OF_USE.zh-CN.md](./TERMS_OF_USE.zh-CN.md)。

<br>
<div align="center"><a href="#top">回到顶部</a></div>
<br>
