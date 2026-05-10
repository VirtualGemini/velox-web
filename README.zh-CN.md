<h2 align="center" id="top">Arc Pro 管理后台前端</h2>
<p align="center">基于 Art Design Pro 原型重构的后台管理前端，已接入 architecture-pro 后端 API，并扩展为可实际落地的管理系统。</p>
<div align="center"><a href="./README.md">English</a> | 简体中文</div>

<br />

## 项目概述

本项目是 `arc-pro` 的前端部分。它基于原始 `art-design-pro` 原型继续开发，但已经不再是单纯的 UI 演示模板。

当前版本重点完成了以下工作：

- 接入 `architecture-pro` 后端 API
- 将原型中的演示流程替换为真实认证与业务请求
- 新增系统管理、文件管理、个人资料等后台能力
- 优化动态路由、权限控制、Token 管理与异常处理

## 技术栈

- Vue 3
- TypeScript
- Vite 7
- Element Plus
- Tailwind CSS 4
- Pinia
- Vue Router 4
- Axios
- Vue I18n
- ECharts
- WangEditor

## 新增内容

- 真实认证流程：登录、注册、退出登录、忘记密码、重置密码
- 当前用户能力：个人资料修改、密码修改、头像更新
- 系统管理模块：用户管理、角色管理、菜单管理
- 文件中心：文件列表、上传、删除、批量删除
- 文件配置管理：新增、编辑、启停、设为主配置、配置测试
- 基于后端菜单数据的动态菜单与权限绑定

## 优化内容

- 基于 Axios 的统一请求层：
  - 自动注入 `Authorization` Token
  - 自动携带 `X-Time-Zone` 时区请求头
  - 统一解析后端 `Result` 返回结构
  - 统一错误提示与异常分发
  - 401 自动退出登录，带防抖处理
- 动态路由初始化流程与后端菜单结构对齐
- 基于 Pinia 的登录态与 Token 持久化
- 使用 Vite Proxy 完成本地前后端联调
- 前端权限标识与后端 `Sa-Token` 权限码保持一致

## 已接入的后端 API

前端已经完成以下后端模块对接：

- 认证：`/api/auth/login`、`/api/auth/register`、`/api/auth/logout`
- 找回密码：`/api/auth/forgot-password/code`、`/api/auth/forgot-password/reset`
- 当前用户：`/api/user/info`、`/api/user/profile`、`/api/user/password`、`/api/user/avatar`
- 用户管理：`/api/user/list`、`/api/user`、`/api/user/{userId}`
- 角色管理：`/api/role/list`、`/api/role/{roleId}/menu-permissions`
- 菜单管理：`/api/v3/system/menus/simple`、`/api/v3/system/menus`
- 文件管理：`/api/file/*`
- 文件配置管理：`/api/file-config/*`

## 项目结构

```text
art-design-pro/
├── src/api                  # 后端接口封装
├── src/router               # 静态路由与动态路由
├── src/store                # Pinia 状态管理
├── src/utils/http           # Axios 请求层与错误处理
├── src/views/auth           # 登录 / 注册 / 忘记密码
├── src/views/system         # 用户 / 角色 / 菜单 / 文件模块
├── src/views/config         # 文件配置页面
└── src/views/dashboard      # 控制台页面
```

## 环境要求

- Node.js >= 20.19.0
- pnpm >= 8.8.0

关键环境变量如下：

```bash
VITE_ACCESS_MODE=architecture-pro
VITE_API_URL=/
VITE_API_PROXY_URL=http://localhost:8080
VITE_WITH_CREDENTIALS=false
```

本地开发时，前端会通过 Vite 代理将 `/api` 请求转发到后端服务。

## 快速开始

```bash
pnpm install
pnpm dev
```

生产构建：

```bash
pnpm build
```

预览构建产物：

```bash
pnpm serve
```

## 后端依赖

该前端默认与同仓库下的 `architecture-pro` 后端配套使用。

推荐本地启动顺序：

1. 启动 MySQL 和 Redis
2. 启动 `architecture-pro`
3. 启动当前前端项目

默认本地地址：

- 前端：`http://localhost:3006`
- 后端：`http://localhost:8080`

## 说明

- 当前项目使用后端权限码，例如 `system:user:query`、`system:role:update`
- 文件上传同时支持后端直传与预签名地址上传两种模式
- 当前请求层已适配 `architecture-pro` 的统一返回结构

<br>
<div align="center"><a href="#top">回到顶部</a></div>
<br>
