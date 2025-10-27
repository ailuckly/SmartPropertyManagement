
# 智慧物业管理平台

本项目实现了课程文档中描述的“智慧物业管理平台”，包含 Spring Boot 后端、Vue 3 前端和基于 Docker 的部署方案。平台覆盖用户登录与注册、物业管理、租约管理、费用记录、维修工单等核心业务流程。

## 目录结构

```
CourseDesign
├── backend              # Spring Boot 3 (Java 17) 服务端
├── frontend             # Vue 3 + Vite 单页前端
├── docker-compose.yml   # 一键启动全部服务
├── docker               # 共享的部署配置（nginx 等）
└── word.md              # 课程原始需求文档
```

## 后端简介（`backend/`）

- 使用 Spring Boot 3、Spring Data JPA、Spring Security、JWT。
- 提供标准 REST 接口，支持分页查询、业务校验和统一异常处理。
- 引入角色权限模型（管理员/业主/租户），通过 JWT + HttpOnly Cookie 控制访问。
- 主要模块：
  - `AuthController`：注册、登录、刷新、退出、`/me`。
  - `PropertyController`：物业增删改查。
  - `LeaseController`：租约管理。
  - `PaymentController`：租金收支记录。
  - `MaintenanceRequestController`：维修工单提交及进度更新。

### 本地开发

```bash
cd backend
mvn -DskipTests spring-boot:run
```

默认连接本机 MySQL（可通过环境变量调整），JWT/CORS 等配置位于 `application.properties`。

## 前端简介（`frontend/`）

- Vue 3 组合式 API + Pinia + Vue Router。
- Axios 封装 `/api` 请求，自动处理 401 并尝试刷新令牌。
- 根据角色动态显示模块、按钮和操作能力。
- 主要页面：登录、注册、仪表盘、物业管理、维修工单、租约、缴费记录。

### 本地开发

```bash
cd frontend
npm install
npm run dev
```

开发服务器默认监听 `http://localhost:5173` 并通过 Vite 代理转发 `/api`。

## Docker 一键启动

```bash
docker compose up --build
```

服务启动后：

- 前端：<http://localhost/>
- 后端：<http://localhost:8080/>
- MySQL：`localhost:3306`（库名 `property_db`，用户名/密码见 `docker-compose.yml`）

## 测试账号建议

首次启动后，可在 `/api/auth/register` 创建管理员或业主账号，也可直接在数据库插入初始化数据。

## AI智能功能

本平台集成了AI智能客服助手和维修工单智能分析功能,支持:

- 💬 全局AI聊天助手(右下角悬浮球)
- 🔧 维修工单自动分类、紧急度评估、维修建议和费用预估

详细配置请查看: [docs/AI_FEATURES.md](./docs/AI_FEATURES.md)

## 注意事项

- 由于环境默认提供 JDK 17,后端以 Java 17 构建。若部署在支持 Java 21 的服务器,只需将 `pom.xml` 中 `java.version` 调整即可。
- JWT 默认密钥为 `change-me-in-prod`,生产环境请修改环境变量 `JWT_SECRET`。
- Docker 默认暴露 MySQL 3306,若本机已有数据库服务,请修改 `docker-compose.yml` 中端口映射。
- AI功能需要配置API Key,默认关闭。启用方法见 [docs/AI_FEATURES.md](./docs/AI_FEATURES.md)。
