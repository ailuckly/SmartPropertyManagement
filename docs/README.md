# 智慧物业管理平台 - 文档中心

本目录包含项目的所有技术文档和开发指南。

## 📚 文档目录

### 🗄️ 数据库文档 (`database/`)
- **[db_init.sql](database/db_init.sql)** - 数据库初始化脚本
  - 表结构定义
  - 初始数据
  - 索引和约束

### 🎨 前端文档 (`frontend/`)
- **[frontend-development-guide.md](frontend/frontend-development-guide.md)** - 前端开发规范
  - 技术栈说明
  - 项目结构
  - 组件使用规范
  - 页面实现说明
  - 开发最佳实践

### 🏗️ 系统文档 (`system/`)
- **[module_overview.md](system/module_overview.md)** - 系统模块概览
  - 功能模块说明
  - 架构设计
  - 接口文档

---

## 📝 文档分类说明

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

### 系统文档 (system/)
包含系统整体相关的文档：
- 系统架构设计
- 模块功能说明
- 接口文档
- 部署文档

---

## 🔍 快速导航

### 新手入门
1. 阅读 [系统模块概览](system/module_overview.md) 了解系统整体架构
2. 查看 [数据库初始化脚本](database/db_init.sql) 了解数据结构
3. 参考 [前端开发规范](frontend/frontend-development-guide.md) 开始前端开发

### 开发指南
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
1. **数据库相关** → `database/`
2. **前端相关** → `frontend/`
3. **系统整体** → `system/`

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

### 2025-01-14
- ✅ 重组文档目录结构
- ✅ 创建分类目录（database/frontend/system）
- ✅ 移动文档到对应目录
- ✅ 创建主 README 索引
- ✅ 清理非必要文档的 Git 跟踪

---

**文档维护**: 开发团队  
**最后更新**: 2025-01-14
