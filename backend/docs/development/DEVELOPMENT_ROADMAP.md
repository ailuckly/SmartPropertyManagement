# 智慧物业管理平台 - 开发路线图

## 项目概述

**项目名称**: SmartPropertyManagement（智慧物业管理平台）  
**技术栈**: Spring Boot 3.3.4 + Vue 3 + Element Plus + MySQL  
**架构**: 统一后端 + 角色分离前端UI  
**仓库地址**: https://github.com/ailuckly/SmartPropertyManagement

---

## ✅ 已完成模块

### 1. 基础架构 (已完成)
- [x] Spring Boot 后端框架搭建
- [x] Vue 3 前端项目初始化
- [x] MySQL 数据库设计
- [x] JWT 认证与授权
- [x] 统一异常处理
- [x] CORS 跨域配置

### 2. 用户管理模块 (已完成)
**后端功能:**
- [x] 用户注册/登录
- [x] 角色管理（管理员/业主/租户）
- [x] 用户信息查询
- [x] 个人资料修改
- [x] 密码修改
- [x] 用户列表导出（Excel）

**前端页面:**
- [x] 登录页面
- [x] 注册页面
- [x] 个人资料页面
- [x] 用户管理页面（管理员）

**文件位置:**
- 后端: `src/main/java/com/example/propertymanagement/controller/AuthController.java`
- 后端: `src/main/java/com/example/propertymanagement/controller/UserController.java`
- 前端: `frontend/src/views/LoginView.vue`
- 前端: `frontend/src/views/RegisterView.vue`
- 前端: `frontend/src/views/ProfileView.vue`

### 3. 物业管理模块 (已完成)
**后端功能:**
- [x] 物业CRUD操作
- [x] 物业状态管理（可出租/已出租/维护中）
- [x] 物业类型分类（公寓/独栋/商用）
- [x] 关键词搜索（地址、城市、州、邮编）
- [x] 业主权限控制
- [x] 物业列表导出（Excel）

**前端页面:**
- [x] 物业列表页面
- [x] 物业详情页面
- [x] 物业创建/编辑表单
- [x] 搜索功能（防抖）
- [x] Excel 导出按钮

**文件位置:**
- 后端: `src/main/java/com/example/propertymanagement/controller/PropertyController.java`
- 后端: `src/main/java/com/example/propertymanagement/service/PropertyService.java`
- 后端: `src/main/java/com/example/propertymanagement/repository/PropertyRepository.java`
- 前端: `frontend/src/views/PropertiesView.vue`

### 4. 租约管理模块 (已完成)
**后端功能:**
- [x] 租约CRUD操作
- [x] 租约状态管理（活跃/已终止/已过期）
- [x] 关键词搜索（租户姓名、物业地址）
- [x] 租约到期自动提醒（定时任务）
- [x] 角色权限控制
- [x] 租约列表导出（Excel）

**前端页面:**
- [x] 租约列表页面
- [x] 租约详情显示
- [x] 租约创建/编辑表单
- [x] 搜索功能
- [x] Excel 导出按钮

**文件位置:**
- 后端: `src/main/java/com/example/propertymanagement/controller/LeaseController.java`
- 后端: `src/main/java/com/example/propertymanagement/service/LeaseService.java`
- 后端: `src/main/java/com/example/propertymanagement/service/LeaseExpirationNotificationService.java`
- 前端: `frontend/src/views/LeasesView.vue`

### 5. 维修管理模块 (已完成)
**后端功能:**
- [x] 维修请求提交（租户）
- [x] 维修工单管理（业主/管理员）
- [x] 工单状态更新（待处理/处理中/已完成/已取消）
- [x] 关键词搜索（维修描述）
- [x] 状态和物业ID筛选
- [x] 角色权限控制
- [x] 维修记录导出（Excel）

**前端页面:**
- [x] 维修工单列表页面
- [x] 维修请求提交表单（租户）
- [x] 工单状态更新（业主/管理员）
- [x] 搜索和筛选功能
- [x] Excel 导出按钮

**文件位置:**
- 后端: `src/main/java/com/example/propertymanagement/controller/MaintenanceRequestController.java`
- 后端: `src/main/java/com/example/propertymanagement/service/MaintenanceRequestService.java`
- 前端: `frontend/src/views/MaintenanceView.vue`

### 6. 支付管理模块 (已完成)
**后端功能:**
- [x] 支付记录查询
- [x] 支付信息录入（业主/管理员）
- [x] 按租约查询支付记录
- [x] 角色权限控制
- [x] 支付记录导出（Excel）

**前端页面:**
- [x] 支付记录列表页面
- [x] 支付录入表单
- [x] Excel 导出按钮

**文件位置:**
- 后端: `src/main/java/com/example/propertymanagement/controller/PaymentController.java`
- 后端: `src/main/java/com/example/propertymanagement/service/PaymentService.java`
- 前端: `frontend/src/views/PaymentsView.vue`

### 7. 通知系统模块 (已完成)
**后端功能:**
- [x] 通知实体设计（14种通知类型）
- [x] 通知CRUD操作
- [x] 通知分页查询
- [x] 标记已读/未读
- [x] 批量操作（全部已读、删除已读）
- [x] 租约到期自动提醒（定时任务）
- [x] 维修状态变更通知
- [x] 租约创建通知

**前端页面:**
- [x] 通知铃铛组件（显示未读数量）
- [x] 通知中心页面
- [x] 通知列表展示
- [x] 筛选功能（全部/未读）
- [x] 标记已读/删除操作

**文件位置:**
- 后端: `src/main/java/com/example/propertymanagement/controller/NotificationController.java`
- 后端: `src/main/java/com/example/propertymanagement/service/NotificationService.java`
- 后端: `src/main/java/com/example/propertymanagement/model/Notification.java`
- 前端: `frontend/src/components/NotificationBell.vue`
- 前端: `frontend/src/views/NotificationsView.vue`

### 8. 仪表盘模块 (已完成)
**后端功能:**
- [x] 概览数据统计
- [x] 物业状态分布
- [x] 物业类型分布
- [x] 维修请求统计
- [x] 月度收支趋势（近6个月）
- [x] 租约到期提醒（未来3个月）
- [x] 近期活动记录

**前端页面:**
- [x] 仪表盘首页
- [x] 角色定制欢迎卡片
- [x] 统计数据卡片
- [x] ECharts 图表展示
- [x] 快捷操作按钮

**文件位置:**
- 后端: `src/main/java/com/example/propertymanagement/controller/DashboardController.java`
- 前端: `frontend/src/views/DashboardView.vue`

### 9. Excel 导出功能 (已完成)
**功能:**
- [x] 统一的 ExcelExportService
- [x] 物业列表导出
- [x] 租约列表导出
- [x] 维修记录导出
- [x] 支付记录导出
- [x] 用户列表导出
- [x] 自动生成带时间戳的文件名

**文件位置:**
- 后端: `src/main/java/com/example/propertymanagement/service/ExcelExportService.java`
- 前端: `frontend/src/utils/download.js`

### 10. 角色分离UI (已完成)
**功能:**
- [x] 侧边栏菜单角色定制
  - 管理员：系统管理视角（5个菜单）
  - 业主：物业运营视角（4个菜单）
  - 租户：租房服务视角（3个菜单）
- [x] 仪表盘个性化欢迎
- [x] 角色徽章显示
- [x] 快捷操作按钮
- [x] 简洁大方的设计风格

**文件位置:**
- 前端: `frontend/src/App.vue`
- 前端: `frontend/src/views/DashboardView.vue`

---

## 🚀 待开发模块（按优先级）

### 优先级 1: 通知系统增强
**目标**: 添加通知优先级和更丰富的通知管理功能

**待开发功能:**
- [ ] 通知优先级字段（LOW/MEDIUM/HIGH/URGENT）
- [ ] 前端根据优先级显示不同颜色和图标
- [ ] 通知分类标签
- [ ] 通知偏好设置

**预计工时**: 4-6小时

**技术要点:**
- 扩展 Notification 实体，添加 priority 枚举字段
- 更新前端 NotificationsView 组件样式
- 添加用户通知偏好设置表

---

### 优先级 2: 报表与数据分析
**目标**: 提供数据统计和报表生成功能

**待开发功能:**
- [ ] 月度/季度/年度财务报表
- [ ] 物业出租率统计
- [ ] 租金收缴率统计
- [ ] 维修完成率统计
- [ ] PDF 报表导出
- [ ] 数据趋势分析

**预计工时**: 8-10小时

**技术要点:**
- 创建 ReportController 和 ReportService
- 集成 iText 或 Apache PDFBox 生成 PDF
- 前端新增报表页面，使用 ECharts 可视化
- 支持日期范围选择

**新文件:**
- 后端: `controller/ReportController.java`
- 后端: `service/ReportService.java`
- 前端: `views/ReportsView.vue`

---

### 优先级 3: 高级搜索功能
**目标**: 提供更强大的搜索和筛选能力

**待开发功能:**
- [ ] 多条件组合搜索
- [ ] 高级筛选器（日期范围、价格区间、多状态）
- [ ] 搜索历史记录
- [ ] 保存常用搜索条件
- [ ] 搜索结果排序

**预计工时**: 6-8小时

**技术要点:**
- 使用 JPA Specification 实现动态查询
- 前端实现高级搜索表单组件
- LocalStorage 保存搜索历史
- 支持搜索条件的保存和快速应用

**新文件:**
- 后端: `specification/PropertySpecification.java`
- 前端: `components/AdvancedSearchForm.vue`

---

### 优先级 4: 文件管理系统
**目标**: 支持文件上传、存储和管理

**待开发功能:**
- [ ] 租赁合同文件上传
- [ ] 物业图片上传（支持多图）
- [ ] 文档分类管理
- [ ] 在线预览（PDF、图片）
- [ ] 文件下载
- [ ] 文件删除和更新

**预计工时**: 10-12小时

**技术要点:**
- 集成阿里云 OSS 或本地文件存储
- 创建 File 实体和 FileRepository
- 前端使用 el-upload 组件
- 支持图片预览、PDF 在线查看
- 添加文件大小和格式限制

**新文件:**
- 后端: `controller/FileController.java`
- 后端: `service/FileService.java`
- 后端: `model/File.java`
- 前端: `components/FileUpload.vue`
- 前端: `views/FilesView.vue`

---

### 优先级 5: 评价与反馈系统
**目标**: 收集租户对物业和服务的评价

**待开发功能:**
- [ ] 物业评价（星级评分）
- [ ] 服务评价
- [ ] 投诉建议提交
- [ ] 评价列表展示
- [ ] 满意度统计

**预计工时**: 6-8小时

**技术要点:**
- 创建 Review 和 Complaint 实体
- 评分系统实现（1-5星）
- 前端评分组件
- 统计平均评分

**新文件:**
- 后端: `controller/ReviewController.java`
- 后端: `model/Review.java`
- 前端: `views/ReviewsView.vue`

---

### 优先级 6: 消息中心
**目标**: 用户间沟通系统

**待开发功能:**
- [ ] 站内消息发送
- [ ] 消息列表
- [ ] 消息已读/未读
- [ ] 业主-租户沟通
- [ ] 消息删除

**预计工时**: 8-10小时

**技术要点:**
- 创建 Message 实体
- 实现用户间消息发送
- WebSocket 实时消息推送（可选）
- 前端消息列表和对话框

**新文件:**
- 后端: `controller/MessageController.java`
- 后端: `model/Message.java`
- 前端: `views/MessagesView.vue`

---

### 优先级 7: 批量操作与数据导入
**目标**: 提高数据管理效率

**待开发功能:**
- [ ] Excel 批量导入物业
- [ ] Excel 批量导入租约
- [ ] 批量修改物业状态
- [ ] 批量删除
- [ ] 数据验证和错误提示

**预计工时**: 6-8小时

**技术要点:**
- Apache POI 读取 Excel
- 数据验证和清洗
- 批量操作事务管理
- 前端文件上传和进度显示

---

## 📋 开发规范

### Git 提交规范
```
feat: 新功能
fix: 修复bug
refactor: 重构
docs: 文档更新
style: 代码格式调整
test: 测试相关
chore: 构建过程或辅助工具的变动
```

### 代码规范
- **后端**: 遵循 Spring Boot 最佳实践
- **前端**: 使用 Vue 3 Composition API
- **命名**: 驼峰命名，见名知意
- **注释**: 关键逻辑必须注释

### 文档更新
- 每完成一个模块，立即更新本文档
- 标记完成的功能为 `[x]`
- 记录新增的文件路径
- 更新预计工时为实际工时

---

## 🎯 项目里程碑

### 第一阶段 ✅ (已完成)
- 基础功能模块开发
- 用户、物业、租约、维修、支付管理
- 通知系统
- 角色分离UI

### 第二阶段 🔄 (进行中)
- 通知系统增强
- 报表功能
- 高级搜索

### 第三阶段 ⏳ (计划中)
- 文件管理
- 评价系统
- 消息中心
- 批量操作

### 第四阶段 ⏳ (计划中)
- 系统优化
- 性能调优
- 安全加固
- 文档完善

---

## 📝 技术债务

### 需要优化的地方
- [ ] 统一异常处理优化
- [ ] API 接口文档（Swagger）
- [ ] 单元测试覆盖率提升
- [ ] 前端状态管理优化
- [ ] 图片和文件的CDN加速
- [ ] 数据库索引优化
- [ ] 缓存机制引入（Redis）

---

## 📚 相关文档

- [项目 README](./README.md)
- [API 文档](./API_DOCUMENTATION.md) - 待创建
- [数据库设计](./DATABASE_DESIGN.md) - 待创建
- [部署指南](./DEPLOYMENT_GUIDE.md) - 待创建

---

**最后更新时间**: 2025-10-14  
**文档维护者**: Development Team
