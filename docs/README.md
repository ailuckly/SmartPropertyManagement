# 智慧物业管理平台 - 文档中心

本目录包含项目的所有技术文档和开发指南。

## 📚 文档目录

### 🤖 AI功能文档
- **[AI_FEATURES.md](AI_FEATURES.md)** - AI智能功能完整配置指南
  - 全局AI聊天助手配置
  - 维修工单智能分析配置
  - 七牛云AI集成说明
  - 故障排查和优化建议

### 🗄️ 数据库文档 (`database/`)
- **[README.md](database/README.md)** - 数据库文档索引
- **[00_db_drop_and_create.sql](database/00_db_drop_and_create.sql)** - 数据库初始化
- **[01_db_schema.sql](database/01_db_schema.sql)** - 表结构定义
- **[02_db_init_admin.sql](database/02_db_init_admin.sql)** - 管理员账号
- **[03_db_mock_data.sql](database/03_db_mock_data.sql)** - 测试数据

### 🎨 前端文档 (`frontend/`)
- **[frontend-development-guide.md](frontend/frontend-development-guide.md)** - 前端开发规范
  - 技术栈说明
  - 项目结构
  - 组件使用规范
  - 页面实现说明
  - 开发最佳实践
- **[../frontend/README_FILE_UPLOAD.md](../frontend/README_FILE_UPLOAD.md)** - 文件上传功能文档

### 🏗️ 系统文档 (`system/`)
- **[module_overview.md](system/module_overview.md)** - 系统模块概览
  - 功能模块说明
  - 架构设计
  - 接口文档
- **[CHANGES_V2.0.md](CHANGES_V2.0.md)** - 版本2.0更新日志

---

## 📝 文档分类说明

### AI功能文档
包含所有AI智能功能相关的文档：
- AI聊天助手配置
- 维修工单智能分析
- 七牛云AI服务集成
- 故障排查指南

### 数据库文档 (database/)
包含所有与数据库相关的文档：
- 数据库设计文档
- SQL 脚本文件
- 数据迁移脚本
- 数据字典

### 前端文档 (frontend/)
包含前端开发相关的文档：
- 开发规范和指南
- 组件库使用说明
- UI/UX 设计规范
- 前端架构文档
- 文件上传功能说明

### 系统文档 (system/)
包含系统整体相关的文档：
- 系统架构设计
- 模块功能说明
- 接口文档
- 版本更新日志

---

## 🔍 快速导航

### 新手入门
1. 阅读 [项目主文档](../README.md) 快速了解项目
2. 查看 [系统模块概览](system/module_overview.md) 了解系统架构
3. 查看 [数据库文档](database/README.md) 了解数据结构
4. 参考 [前端开发规范](frontend/frontend-development-guide.md) 开始开发
5. 如需AI功能，查看 [AI配置指南](AI_FEATURES.md)

### 开发指南
- **项目概览**: [../README.md](../README.md) - 快速开始
- **完整开发指南**: [../WARP.md](../WARP.md) - 详细技术文档
- **AI功能配置**: [AI_FEATURES.md](AI_FEATURES.md)
- **前端开发**: 参考 `frontend/` 目录下的文档
- **数据库操作**: 参考 `database/` 目录下的脚本
- **系统设计**: 参考 `system/` 目录下的文档

---

## 📋 文档维护规范

### 文档命名规则
- 使用小写字母和连字符
- 使用描述性的名称
- 例如: `frontend-development-guide.md`

### 文档分类规则
1. **AI功能相关** → 根目录 `AI_FEATURES.md`
2. **数据库相关** → `database/`
3. **前端相关** → `frontend/`
4. **系统整体** → `system/`

### 文档更新流程
1. 修改相应文档
2. 更新文档中的"最后更新"日期
3. 提交 Git 并说明修改内容

---

## ✅ 版本管理说明

### Git 跟踪规则
- ✅ **跟踪**: 所有 `.sql` 文件
- ✅ **跟踪**: 所有 `.md` 文档
- ❌ **不跟踪**: `.pptx` 演示文件
- ❌ **不跟踪**: 临时学习笔记

### 已从 Git 移除的文件
以下文件已从版本控制中移除，但保留在本地：
- `learnWord.md` - 个人学习笔记
- `presentation.md` / `presentation.pptx` - 演示文稿

---

## 🔄 更新日志

### 2025-10-27
- ✅ 整合所有AI相关文档到 `AI_FEATURES.md`
- ✅ 删除根目录冗余文档（AI_MODULE_README.md, GLOBAL_AI_CHAT_GUIDE.md, QINIU_AI_SETUP.md）
- ✅ 清理根目录无用npm文件和node_modules
- ✅ 更新主README添加AI功能说明
- ✅ 更新文档索引和导航

### 2025-01-14
- ✅ 重组文档目录结构
- ✅ 创建分类目录（database/frontend/system）
- ✅ 移动文档到对应目录
- ✅ 创建主 README 索引
- ✅ 清理非必要文档的 Git 跟踪

---

**文档维护**: 开发团队  
**最后更新**: 2025-10-27
