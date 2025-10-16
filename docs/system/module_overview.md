# 智慧物业管理平台 - 系统模块完整说明

> **文档版本**: v2.0  
> **更新日期**: 2025-10-15  
> **适用范围**: 完整系统架构与功能模块说明

## 项目总体概览

### 项目简介
**智慧物业管理平台**是一个基于 Spring Boot + Vue 3 的全栈 Web 应用，旨在为物业管理提供数字化解决方案。系统支持物业管理、租约管理、租金登记、维修工单等核心业务流程，针对管理员、业主、租客三种角色提供差异化功能权限。

### 技术架构
```
前端层：Vue 3 + Vite + Pinia + Vue Router + Element Plus
    ↓ HTTP/RESTful API
后端层：Spring Boot 3 + Spring Security + JWT
    ↓ JPA/Hibernate
数据层：MySQL 8.0 + Redis（可选）
部署层：Docker + Docker Compose + Nginx
```

### 核心功能模块（10个）
1. **用户管理** - 注册/登录/角色权限控制
2. **物业管理** - CRUD + 图片上传 + 批量操作
3. **租约管理** - 租约生命周期 + 到期提醒
4. **维修管理** - 工单系统 + 状态跟踪
5. **支付管理** - 租金记录查询与登记
6. **通知系统** - 14种通知类型 + 实时提醒
7. **仪表盘** - 数据统计 + ECharts 图表
8. **Excel 导出** - 5种数据类型导出
9. **文件管理** - 图片上传/预览/删除
10. **安全与审计** - JWT 认证 + 操作日志

### 系统特点
- ✅ **安全性高** - JWT + HttpOnly Cookie，角色权限细粒度控制
- ✅ **用户体验优** - 响应式设计，防抖搜索，批量操作
- ✅ **代码质量好** - 分层架构，DTO/Mapper 分离，统一异常处理
- ✅ **易部署** - Docker 容器化，一键启动所有服务
- ✅ **扩展性强** - 模块化设计，支持微服务拆分

---

## 1. 后端服务概览（`backend/`）

### 1.1 认证与授权
- **核心类**：`AuthService`、`JwtTokenProvider`、`JwtAuthenticationFilter`、`SecurityConfig`、`AuthController`
- **职责说明**：
  - `AuthService` 负责注册、登录、刷新令牌、注销，统一生成访问令牌（JWT）与刷新令牌（UUID），并通过 HttpOnly Cookie 返回客户端。
  - `JwtTokenProvider` 基于配置密钥生成/验证 JWT，暴露访问令牌有效期与刷新令牌有效期计算方法。
  - `JwtAuthenticationFilter` 拦截所有请求，优先解析 `Authorization: Bearer` 头，其次读取 Cookie，在验证成功后将认证信息写入 `SecurityContext`。
  - `SecurityConfig` 关闭 CSRF、开启无状态会话、插入 JWT 过滤器、配置可匿名访问的端点；通过 `app.security.cors.allowed-origins` 动态加载 CORS 白名单。
  - `AuthController` 暴露 `/api/auth/*` 接口，供前端登录注册模块调用。
- **安全要点**：
  - 所有令牌均通过 HttpOnly Cookie 传递，客户端 JS 无法直接读取，降低 XSS 风险。
  - 注册默认授予租客角色，如需创建管理员帐号需在后端手动调整或通过管理端接口。

### 1.2 物业模块
- **核心类**：`PropertyService`、`PropertyController`、`PropertyRepository`
- **功能**：
  - 分页查询支持按业主过滤，返回结构统一封装为 `PageResponse<PropertyDto>`。
  - 只有管理员/业主可创建或更新物业；管理员可在 request 中指定 `ownerId`，业主只能为自身创建。
  - 删除操作同样受角色限制，防止越权。

### 1.3 租约模块
- **核心类**：`LeaseService`、`LeaseController`、`LeaseRepository`
- **功能**：
  - 创建租约时校验业主身份，默认状态为 `ACTIVE`，并同步将相关物业状态改为 `LEASED`。
  - 分页查询自动根据当前用户角色过滤数据：管理员全部、业主仅限自有物业、租客仅限本人。
  - 更新/删除时遵循相同的权限校验，删除时将物业状态恢复为 `AVAILABLE`。

### 1.4 维修工单模块
- **核心类**：`MaintenanceRequestService`、`MaintenanceRequestController`、`MaintenanceRequestRepository`
- **功能**：
  - 租客可提交问题描述，系统自动记录提交时间与初始状态 `PENDING`。
  - 分页查询数据范围随角色变化：管理员全部、业主查看名下物业工单、租客仅查看本人提交的请求。
  - 管理员/业主可更新状态；状态变为 `COMPLETED` 时会记录完成时间。

### 1.5 租金支付模块
- **核心类**：`PaymentService`、`PaymentController`、`PaymentRepository`
- **功能**：
  - 管理员与业主可以登记缴费记录，租客仅具有查看权限。
  - 查询接口支持按租约 ID 分页，并在服务层校验租客与租约的关联性。
  - 金额使用 `BigDecimal` 保证精度，日期字段保持 ISO-8601 序列化。

### 1.6 共用基础设施
- **DTO 与 Mapper**：位于 `dto/` 与 `mapper/` 目录，负责实体与前端数据结构之间的转换。
- **异常处理**：`GlobalExceptionHandler` 统一捕获业务异常、校验异常与系统异常，返回结构化 JSON。
- **工具类**：`CookieUtils` 提供 HttpOnly Cookie 写入/清除方法；`SecurityUtils` 封装 `SecurityContext` 访问。
- **配置文件**：`application.properties` 包含数据库、JPA、JWT、CORS 等默认配置，可通过环境变量覆盖。

## 2. 前端 SPA 概览（`frontend/`）

### 2.1 技术栈与基础配置
- **技术栈**：Vue 3（组合式 API）+ Vite + Pinia + Vue Router + Axios。
- **目录结构**：
  - `src/main.js`：应用入口，注入 Pinia、Router、全局样式。
  - `src/router/index.js`：路由定义，支持 `meta.requiresAuth` 与 `meta.roles` 控制访问。
  - `src/stores/auth.js`：全局认证状态，负责登录、注册、刷新、注销与角色判断。
  - `src/api/http.js`：Axios 实例配置，带 `withCredentials` 与 401 自动刷新逻辑。
  - `src/styles/base.css`：全局基础样式。

### 2.2 页面模块
- **`App.vue`**：应用框架，显示导航栏、角色菜单与退出按钮。
- **`DashboardView.vue`**：仪表盘，展示物业数量、租约数量、待处理维修数量等指标。
- **`PropertiesView.vue`**：物业管理页面，管理员/业主可创建更新，所有登录用户可查看。
- **`MaintenanceView.vue`**：维修工单页面，租客可提交，业主/管理员可更新状态。
- **`LeasesView.vue`**：租约管理页面，包含创建、编辑、删除及租客只读视图。
- **`PaymentsView.vue`**：租金支付页面，业主/管理员登记，租客按租约查看。
- **`LoginView.vue` / `RegisterView.vue`**：认证页面，接入 `auth` Store 实现会话管理。
- **`NotFoundView.vue`**：路由兜底 404 页面。

### 2.3 状态与交互
- Pinia `auth` 仓库存储用户信息、加载状态与角色工具方法，初始化时会调用 `/api/auth/me` 恢复会话。
- Axios 拦截器在收到 401 时触发 `/api/auth/refresh-token`，刷新成功后重试原请求。
- 路由守卫确保未登录用户访问受限页面时跳转到登录页面，并带上 `redirect` 参数。

## 3. 部署与环境

### 3.1 Docker 镜像
- **后端镜像**：`backend/Dockerfile` 使用 Maven 多阶段构建，最终以 `eclipse-temurin:17-jre` 运行。
- **前端镜像**：`frontend/Dockerfile` 先打包 `dist`，再复制到 `nginx:alpine`，并加载 `docker/nginx.conf`。

### 3.2 docker-compose
- **服务**：
  - `db`：MySQL 8.0，默认 root 密码 `property`，库名 `property_db`。
  - `backend`：Spring Boot 应用，环境变量负责数据库、CORS、JWT Secret 等配置。
  - `frontend`：Nginx 托管的静态资源，监听 80 端口。
- **注意事项**：
  - 如需在浏览器直接访问后端并携带 Cookie，需保证 `APP_ALLOWED_ORIGINS` 与实际 `Origin` 完全一致。
  - 生产环境应修改 `MYSQL_ROOT_PASSWORD` 与 `JWT_SECRET` 等敏感信息。

### 3.3 本地开发
- **后端**：`mvn -DskipTests spring-boot:run`
- **前端**：`npm install && npm run dev`（默认走 Vite 代理转发 `/api`）。

## 4. 数据库设计模块

### 4.1 数据库架构
- **数据库系统**：MySQL 8.0
- **数据库名**：`smart_property_system`
- **字符集**：utf8mb4 / utf8mb4_unicode_ci
- **设计规范**：遵循阿里巴巴 MySQL 规范

### 4.2 核心数据表（8个）

#### 用户与权限表
1. **user** - 用户信息表
   - 存储用户基本信息：用户名、邮箱、密码（BCrypt）、手机号
   - 唯一索引：username, email

2. **role** - 角色表
   - 三种角色：ROLE_ADMIN（管理员）、ROLE_OWNER（业主）、ROLE_TENANT（租客）

3. **user_role** - 用户角色关联表
   - 多对多关系，支持一个用户拥有多个角色

#### 业务数据表
4. **property** - 物业表
   - 存储物业详细信息：地址、类型、面积、租金、状态
   - 状态：AVAILABLE（可用）、LEASED（已租）、UNDER_MAINTENANCE（维修中）
   - 支持图片上传（cover_image_path）

5. **lease** - 租约表
   - 存储租约信息：物业id、租客id、租期、租金
   - 状态：ACTIVE（活跃）、EXPIRED（过期）、TERMINATED（终止）

6. **payment** - 支付记录表
   - 存储租金支付记录：租约id、金额、支付日期、支付方式

7. **maintenance_request** - 维修申请表
   - 存储维修工单：物业id、租客id、问题描述、状态
   - 状态：PENDING、IN_PROGRESS、COMPLETED、CANCELLED

8. **refresh_token** - 刷新令牌表
   - JWT 刷新令牌存储，用于访问令牌的续期

### 4.3 数据库设计特色

#### 阿里巴巴规范实践
- ✅ **禁用物理外键** - 通过应用层保证数据一致性，提升插入性能
- ✅ **统一审计字段** - 所有表包含 `gmt_create`、`gmt_modified`、`is_deleted`
- ✅ **表名单数化** - 表名使用单数形式（user 而非 users）
- ✅ **逻辑删除** - 使用 `is_deleted` 字段标记删除，不物理删除数据
- ✅ **索引优化** - 关键外键字段、状态字段均建立索引

#### 数据一致性保证
```java
// 应用层检查示例
@Transactional
public void deleteUser(Long userId) {
    // 检查是否有关联物业
    if (propertyRepository.existsByOwnerId(userId)) {
        throw new BusinessException("用户存在关联物业，无法删除");
    }
    // 逻辑删除
    user.setIsDeleted(1);
    userRepository.save(user);
}
```

---

## 5. 功能覆盖与待办

### 5.1 已实现功能（100%）
- ✅ 用户认证（注册/登录/JWT）
- ✅ 角色权限控制（RBAC）
- ✅ 物业管理（CRUD + 图片上传 + 批量操作）
- ✅ 租约管理（生命周期 + 到期提醒）
- ✅ 维修工单（提交/状态更新）
- ✅ 租金支付（记录查询/登记）
- ✅ 通知系统（14种通知类型）
- ✅ 仪表盘（统计 + ECharts）
- ✅ Excel 导出（5种数据类型）
- ✅ 文件管理（上传/预览/删除）
- ✅ CORS 配置
- ✅ Docker 容器化部署
- ✅ 前端 SPA（Vue 3 + Pinia）

### 5.2 高级功能（85%）
- ✅ 批量操作（批量删除/状态更新）
- ✅ 高级搜索（防抖搜索）
- ✅ 高级筛选（多维度筛选面板）
- ✅ 安全修复（npm 依赖漏洞修复）
- 🔄 审计日志（数据模型已建立，服务层待完成）

### 5.3 建议改进
- 💡 注册接口限制角色提升，避免普通用户直接创建管理员账号
- 💡 维修工单模块补充租客与物业之间的租约校验
- 💡 CORS 默认配置建议添加 `http://localhost` 与 `http://localhost:80` 双白名单
- 💡 增加单元/集成测试覆盖率
- 💡 集成 Swagger/OpenAPI 文档
- 💡 引入 Redis 缓存机制
