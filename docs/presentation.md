# 智慧物业管理平台项目汇报（结构化讲稿）

## 封面
- **标题**：智慧物业管理平台开发流程与架构思路
- **副标题**：课程设计项目总结
- **团队/作者**：XXX
- **日期**：YYYY-MM-DD

---

## 目录
1. 项目背景与目标
2. 数据库设计与模型构建
3. 后端开发流程
4. 前端开发流程
5. 配置、部署与版本管理
6. 展望与优化方向

---

## 1. 项目背景与目标
- **背景需求**：
  - 针对课程设计案例，构建一套智慧物业管理平台，覆盖物业管理、租约管理、租金登记与维修工单等场景。
  - 满足管理员、业主、租客不同角色的权限控制，实现全流程数字化。
- **核心目标**：
  - 提供 Web API + SPA 前端的全栈应用。
  - 保障安全性（JWT + HttpOnly Cookie）、数据一致性与良好的用户体验。
  - 提供可复制的部署方案，支持本地及容器化运行。

---

## 2. 数据库设计与模型构建
- **建模思路**：
  - 从业务实体出发：`users`、`roles`、`properties`、`leases`、`payments`、`maintenance_requests` 等。
  - 确定实体关系：用户与角色多对多、物业与租约一对多、租约与支付一对多、物业与维修请求一对多。
- **字段设计原则**：
  - 所有主键使用自增 ID，关键外键添加索引。
  - 使用枚举字段表示状态：如物业状态（AVAILABLE、LEASED、UNDER_MAINTENANCE），租约状态，维修状态等。
  - 时间字段统一使用 `TIMESTAMP`，记录创建、完成时间。
- **ER 图简述**：（可在 PPT 中加入 ER 图展示表关系）
  - `users` — `user_roles` — `roles`
  - `users` — `properties`（业主关系）
  - `properties` — `leases` — `users`（租客）
  - `leases` — `payments`; `properties` — `maintenance_requests`
- **数据库实现**：
  - 使用 Spring Data JPA 实体与 Repository 映射。
  - `application.properties` 中配置 MySQL 方言、时区、驼峰命名规则。
  - 提供初始角色数据的 CommandLineRunner，确保系统启动后即有 ROLE_ADMIN/ROLE_OWNER/ROLE_TENANT。

---

## 3. 后端开发流程
- **技术栈与架构**：
  - Spring Boot 3 + Spring Security + Spring Data JPA + Validation + Lombok。
  - 分层设计：Controller -> Service -> Repository -> Entity/DTO。
- **认证与授权实现**：
  - `AuthService` 实现注册、登录、刷新、注销；使用 BCrypt 处理密码。
  - `JwtTokenProvider` 生成访问令牌，刷新令牌存表；`JwtAuthenticationFilter` 解析请求令牌。
  - `SecurityConfig` 设置无状态会话、REST 风格权限控制、CORS 白名单。
- **业务模块实现**：
  - `PropertyService`、`LeaseService`、`MaintenanceRequestService`、`PaymentService` 分别处理 CRUD、权限校验、状态更新。
  - DTO/Mapper 层负责实体序列化，统一通过 `PageResponse<T>` 返回分页结构。
  - 全局异常处理 `GlobalExceptionHandler` 输出统一错误格式。
- **开发节奏**：
  - 优先搭建实体与仓库，再补充服务层逻辑，最后编写控制器。
  - 每个模块完成后编写 MockMvc / 单元测试验证边界场景。
- **关键里程碑**：
  - 完成认证模块。
  - 物业与租约业务联调。
  - 维修与支付模块上线。
  - 提炼公共工具与统一异常处理。

---

## 4. 前端开发流程
- **技术栈与架构**：
  - Vue 3 + Vite + Pinia + Vue Router + Axios；样式使用基础 CSS。
  - 单页面应用（SPA），组件按模块划分。
- **状态管理**：
  - `auth` Store 维护用户信息、角色、加载状态，封装登录/注册/刷新/注销逻辑。
  - axios 拦截器处理 401 -> refresh token -> retry。
- **页面结构**：
  - 登录、注册视图负责认证流程。
  - 仪表盘展示核心数据统计。
  - 物业/租约/维修/支付视图分别对接后台接口，实现表单 + 表格 + 分页。
  - 路由守卫依据 `meta.requiresAuth` 和 `meta.roles` 控制访问。
- **开发节奏**：
  - 先搭建脚手架、样式与路由，再实现认证流程。
  - 逐个实现业务视图，与后端联调，完善校验与提示。
  - 优化响应式布局与交互细节。
- **UI 亮点与挑战**：
  - 使用同一套卡片/表格样式提升一致性。
  - 处理权限差异，如租客视图为只读、管理员/业主拥有表单。
  - 添加加载态与空态，提高操作反馈。

---

## 5. 配置、部署与版本管理
- **配置管理**：
  - `application.properties` 支持 `SPRING_DATASOURCE_*`、`JWT_SECRET` 等环境变量覆盖。
  - 前端 `vite.config.js` 配置 API 代理与路径别名，方便本地调试。
- **Docker 部署**：
  - 后端 Dockerfile：多阶段构建（Maven -> JRE），导出 JAR。
  - 前端 Dockerfile：Node 构建 `dist`，复制到 Nginx。
  - `docker-compose.yml` 组合 MySQL、后端、前端；使用 `frontend/docker/nginx.conf` 执行反向代理和静态托管。
- **Git 工作流**：
  - 初始化仓库，按照功能模块逐步提交。
  - 提交信息规范如 `feat:`、`docs:` 等，便于回溯历史。
  - 使用 `origin/main` 作为主分支，多次 push 更新，保留主要里程碑。
- **文档管理**：
  - `README.md` 总结运行方式与注意事项。
  - `docs/module_overview.md` 记录模块说明。
  - `docs/learnWord.md` 收录 20 天开发日志，便于回顾与汇报。

---

## 6. 展望与优化方向
- **安全强化**：
  - 注册接口限制角色选项，避免直接创建高权限账号。
  - 维修工单校验租客与物业关联，防止越权提交。
  - 完善 CORS 默认配置，适配多种前端访问形式。
- **功能扩展**：
  - 引入消息通知或看板视图，提示业主/管理员的待办事项。
  - 增加报表导出、图表统计，支持财务分析与占用率分析。
  - 提供角色管理界面，让管理员在线分配权限。
- **技术优化**：
  - 补充更多端到端测试与性能监控。
  - 尝试引入 CI/CD 流程，自动执行构建和部署。
  - 对接对象存储，支持上传物业图片、维修附件等。

---

## 封底
- **感谢聆听**
- **联系信息**
- **问答时间**
