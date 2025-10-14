# 文档目录说明

本目录包含智慧物业管理平台的所有项目文档，按功能模块分类组织。

## 📁 目录结构

```
docs/
├── api/                    # API接口文档
│   ├── PAYMENT_API_GUIDE.md           # 支付API使用指南
│   └── PAYMENT_VERIFICATION.md        # 支付功能验证文档
│
├── features/               # 功能说明文档
│   ├── 收支记录功能说明.md             # 收支记录功能详细说明
│   └── 租约记录功能说明.md             # 租约记录功能详细说明
│
├── development/            # 开发指南与计划
│   ├── DEVELOPMENT_ROADMAP.md         # 项目开发路线图（总体规划）
│   ├── PUSH_GUIDE.md                  # Git推送指南
│   └── 功能完善开发计划.md            # 功能完善开发计划
│
├── project/                # 项目管理文档
│   └── COMMITS_SUMMARY.md             # 提交记录汇总
│
├── database/               # 数据库相关脚本
│   ├── fix_admin_pwd.sql              # 修复管理员密码脚本
│   ├── update_admin_password.sql      # 更新管理员密码脚本
│   └── update_pwd_final.sql           # 最终密码更新脚本
│
└── test-scripts/           # 测试脚本（参考用）
    ├── test_leases.ps1                # 租约功能测试脚本
    ├── test_payment_features.ps1      # 支付功能测试脚本
    └── test_payments.ps1              # 支付测试脚本
```

## 📖 文档说明

### API 文档 (api/)
包含所有后端API接口的使用说明和验证文档，方便前端开发和接口测试。

### 功能说明 (features/)
详细描述各个业务功能模块的需求、实现方案和使用说明。

### 开发指南 (development/)
- **DEVELOPMENT_ROADMAP.md**: 项目整体开发路线图，包含已完成和待开发模块
- **PUSH_GUIDE.md**: Git操作指南
- **功能完善开发计划.md**: 具体功能的开发计划

### 项目管理 (project/)
包含项目提交历史、变更记录等项目管理相关文档。

### 数据库脚本 (database/)
存放数据库初始化、更新、修复等SQL脚本文件。

### 测试脚本 (test-scripts/)
PowerShell测试脚本，用于快速验证后端功能，仅作为参考保留。

## 🔄 文档更新规范

1. **新增文档**: 根据文档类型放入对应目录
2. **文档命名**: 使用清晰的文件名，英文文档使用大写+下划线，中文文档使用描述性名称
3. **及时更新**: 功能开发完成后立即更新相关文档
4. **版本控制**: 所有文档变更都需要通过Git提交

## 📝 推荐阅读顺序

1. **新手入门**: 
   - `development/DEVELOPMENT_ROADMAP.md` - 了解项目整体
   - `features/` - 熟悉各功能模块

2. **开发人员**:
   - `api/` - API接口使用
   - `development/` - 开发规范和计划
   - `database/` - 数据库操作

3. **测试人员**:
   - `test-scripts/` - 测试脚本参考
   - `api/` - API验证文档

---

**最后更新**: 2025-10-14  
**维护者**: Development Team
