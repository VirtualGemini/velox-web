<h2 align="center" id="top">Arc Pro Admin Frontend</h2>
<p align="center">An admin console rebuilt from the Art Design Pro prototype, integrated with the architecture-pro backend APIs and extended with production-oriented management features.</p>
<div align="center">English | <a href="./README.zh-CN.md">简体中文</a></div>

<br />

## Overview

This project is the frontend of `arc-pro`. It is based on the original `art-design-pro` prototype, but no longer serves as a pure visual demo.

It has been turned into a real admin application by:

- integrating the `architecture-pro` backend API
- replacing mock-oriented flows with actual authentication and business requests
- adding system management, file management, and profile management pages
- optimizing route permissions, token handling, and request error behavior

## Tech Stack

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

## What Was Added

- Real authentication flow: login, register, logout, forgot password, reset password
- Current-user capabilities: profile update, password change, avatar update
- System management modules: users, roles, menus
- File center: file list, upload, delete, batch delete
- File configuration management: create, update, enable/disable, set master, test configuration
- Backend-driven menu loading and permission binding

## What Was Optimized

- Unified API client based on Axios with:
  - token injection via `Authorization`
  - `X-Time-Zone` request header
  - unified `Result` response parsing
  - centralized error messaging
  - debounced 401 auto logout handling
- Dynamic route initialization aligned with backend menu data
- Persistent login state and token storage via Pinia
- Vite proxy setup for local frontend-backend integration
- Frontend permission marks aligned with backend `Sa-Token` permission codes

## Key API Integration

The frontend is already wired to the backend modules below:

- Auth: `/api/auth/login`, `/api/auth/register`, `/api/auth/logout`
- Password recovery: `/api/auth/forgot-password/code`, `/api/auth/forgot-password/reset`
- Current user: `/api/user/info`, `/api/user/profile`, `/api/user/password`, `/api/user/avatar`
- User management: `/api/user/list`, `/api/user`, `/api/user/{userId}`
- Role management: `/api/role/list`, `/api/role/{roleId}/menu-permissions`
- Menu management: `/api/v3/system/menus/simple`, `/api/v3/system/menus`
- File management: `/api/file/*`
- File config management: `/api/file-config/*`

## Project Structure

```text
art-design-pro/
├── src/api                  # Backend API wrappers
├── src/router               # Static and dynamic routing
├── src/store                # Pinia stores
├── src/utils/http           # Axios client and error handling
├── src/views/auth           # Login / register / forgot password
├── src/views/system         # User / role / menu / file modules
├── src/views/config         # File config pages
└── src/views/dashboard      # Dashboard pages
```

## Environment

Runtime requirements:

- Node.js >= 20.19.0
- pnpm >= 8.8.0

Important environment variables:

```bash
VITE_ACCESS_MODE=architecture-pro
VITE_API_URL=/
VITE_API_PROXY_URL=http://localhost:8080
VITE_WITH_CREDENTIALS=false
```

Local development uses the Vite proxy to forward `/api` requests to the backend.

## Quick Start

```bash
pnpm install
pnpm dev
```

Build for production:

```bash
pnpm build
```

Preview production build:

```bash
pnpm serve
```

## Backend Dependency

This frontend is designed to work with the `architecture-pro` backend in the same repository.

Recommended local startup order:

1. Start MySQL and Redis
2. Start `architecture-pro`
3. Start this frontend project

Default local addresses:

- Frontend: `http://localhost:3006`
- Backend: `http://localhost:8080`

## Notes

- The project uses backend permission codes such as `system:user:query` and `system:role:update`.
- File upload supports both backend-upload and presigned-url flows.
- The current implementation targets the unified backend response structure used by `architecture-pro`.

<br>
<div align="center"><a href="#top">Back to Top</a></div>
<br>
