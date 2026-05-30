<h2 align="center" id="top">Velox Web Pro</h2>
<p align="center">A full-stack admin system with a Vue 3 frontend and a Spring Boot backend.</p>
<div align="center">English | <a href="./README.zh-CN.md">中文</a></div>

<br />

## Overview

`velox-web-pro` is a full-stack admin project. It provides the web console, backend services, authentication, permission management, system management, file management, and supporting infrastructure needed by the Velox admin product.

## Repository Architecture

```text
velox-web-pro/
├── frontend/              # Vue 3, TypeScript, Vite frontend application
│   ├── public/            # Static public assets
│   └── src/               # Frontend source code
│       ├── api/           # Backend API wrappers
│       ├── assets/        # Static source assets
│       ├── components/    # Reusable Vue components
│       ├── config/        # Frontend configuration
│       ├── directives/    # Vue directives
│       ├── enums/         # Shared frontend enums
│       ├── hooks/         # Composition hooks
│       ├── locales/       # Internationalization resources
│       ├── plugins/       # Frontend plugins
│       ├── router/        # Static and dynamic routing
│       ├── store/         # Pinia stores
│       ├── types/         # TypeScript type definitions
│       ├── utils/         # Frontend utilities
│       └── views/         # Page views
└── backend/               # Java 25, Spring Boot, Maven backend application
    ├── script/            # Local scripts and Docker dependency setup
    ├── velox-dependencies # Dependency version management
    ├── velox-common       # Shared business language and common models
    ├── velox-framework    # Pluggable capability starters
    ├── velox-infra        # Product-level infrastructure wiring
    ├── velox-system       # System business modules
    └── velox-server       # Application startup module
```

## Current Capabilities

- Authentication: login, logout, registration, captcha, password recovery, and password reset.
- Current user: profile, password, avatar, and account information management.
- System management: users, roles, menus, and role-menu permission assignment.
- File center: upload, query, delete, batch delete, presigned upload, and presigned download.
- File configuration: create, update, enable or disable, primary config switching, and connectivity testing.
- Foundation: unified response model, exception handling, request logging, trace propagation, operation logging, ID generation, and mail sending.
- Frontend integration: dynamic routing, permission-code binding, token persistence, unified API client, and centralized error handling.

## Requirements

- JDK 25+
- Maven 3.9+
- Node.js >= 20.19.0
- pnpm >= 8.8.0
- MySQL 8 or PostgreSQL 14+
- Redis 7
- Docker and Docker Compose, recommended for local dependencies

## Local Development

Start backend dependencies:

```bash
cd backend/script/docker
docker compose up -d
```

Start the backend:

```bash
cd backend
mvn clean compile
cd velox-server
mvn spring-boot:run
```

Start the frontend:

```bash
cd frontend
pnpm install
pnpm dev
```

Default local addresses:

- Frontend: `http://localhost:3006`
- Backend: `http://localhost:8080`

The frontend uses the Vite proxy to forward `/api` requests to the backend during local development. Common environment variables:

```bash
VITE_ACCESS_MODE=backend
VITE_API_URL=/
VITE_API_PROXY_URL=http://localhost:8080
VITE_WITH_CREDENTIALS=false
```

## Common Commands

Backend:

```bash
cd backend
mvn clean compile
```

Frontend:

```bash
cd frontend
pnpm lint
pnpm build
pnpm serve
```

## Documentation

- [Contribution Guide](./CONTRIBUTING.md) / [贡献指南](./CONTRIBUTING.zh-CN.md)
- [Development Spec](./docs/DEVELOPMENT_SPEC.md) / [开发规范](./docs/DEVELOPMENT_SPEC.zh-CN.md)
- [Commit Spec](./docs/COMMIT_SPEC.md) / [提交规范](./docs/COMMIT_SPEC.zh-CN.md)
- [Changelog](./CHANGELOG.md) / [更新日志](./CHANGELOG.zh-CN.md)
- [Disclaimer](./DISCLAIMER.md) / [免责声明](./DISCLAIMER.zh-CN.md)
- [Terms of Use](./TERMS_OF_USE.md) / [使用条款](./TERMS_OF_USE.zh-CN.md)

## License

This repository is released under the MIT License (see [LICENSE](./LICENSE)), which also describes the licensing of bundled components. The backend (`backend/`) and the frontend (`frontend/`) each carry their own LICENSE file; the frontend is derived from a third-party open-source project and retains its original MIT license and copyright.
Additional risk allocation, warranty disclaimers, and use terms are described in [DISCLAIMER.md](./DISCLAIMER.md) and [TERMS_OF_USE.md](./TERMS_OF_USE.md).

<br>
<div align="center"><a href="#top">Back to Top</a></div>
<br>
