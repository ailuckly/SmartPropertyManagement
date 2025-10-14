# 数据库快速设置指南

## 执行步骤

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 执行脚本（按顺序）
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/00_db_drop_and_create.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/01_db_schema.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/02_db_init_admin.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/03_db_mock_data.sql
```

## 默认账户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| owner_wang/li/zhang | admin123 | 业主 |
| tenant_* | admin123 | 租户 |

## 数据概览

- 用户：11个
- 物业：20个
- 租约：15个
- 支付记录：80+条
- 维修请求：20条

---

**数据库名**: `smart_property_system`  
**完成日期**: 2025-01-14
