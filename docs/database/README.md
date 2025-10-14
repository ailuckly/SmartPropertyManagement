# 智慧物业管理平台 - 数据库文档

## 📋 概述

本目录包含智慧物业管理平台的数据库脚本，遵循**阿里巴巴MySQL数据库规范**。

### ✅ 特性

- 表名使用单数形式
- 包含阿里规范必备字段：`gmt_create`, `gmt_modified`, `is_deleted`
- 无物理外键约束（使用软外键）

## 📁 文件说明

| 文件名 | 说明 | 执行顺序 |
|--------|------|----------|
| `00_db_drop_and_create.sql` | 删除旧数据库，创建新数据库 | 1 |
| `01_db_schema.sql` | 创建表结构（8个表） | 2 |
| `02_db_init_admin.sql` | 初始化角色和管理员账户 | 3 |
| `03_db_mock_data.sql` | 插入模拟数据 | 4 |
| `04_db_verify.sql` | 数据验证和统计查询 | 5 |

## 🗄️ 数据库信息

- **数据库名**: `smart_property_system`
- **字符集**: `utf8mb4`
- **表数量**: 8个（role, user, user_role, property, lease, payment, maintenance_request, refresh_token）

## 🚀 快速开始

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 按顺序执行脚本
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/00_db_drop_and_create.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/01_db_schema.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/02_db_init_admin.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/03_db_mock_data.sql
```

## 👤 默认账户

执行初始化脚本后，系统会创建以下账户：

| 用户类型 | 用户名 | 密码 | 角色 |
|---------|--------|------|------|
| 管理员 | admin | admin123 | ROLE_ADMIN |
| 业主 | owner_wang | admin123 | ROLE_OWNER |
| 业主 | owner_li | admin123 | ROLE_OWNER |
| 业主 | owner_zhang | admin123 | ROLE_OWNER |
| 租户 | tenant_zhao | admin123 | ROLE_TENANT |
| 租户 | tenant_chen | admin123 | ROLE_TENANT |
| 租户 | tenant_liu | admin123 | ROLE_TENANT |
| 租户 | tenant_huang | admin123 | ROLE_TENANT |
| 租户 | tenant_xu | admin123 | ROLE_TENANT |
| 租户 | tenant_sun | admin123 | ROLE_TENANT |
| 租户 | tenant_ma | admin123 | ROLE_TENANT |

> **注意**: 所有用户的密码都是 `admin123`（已BCrypt加密存储）

## 📈 模拟数据说明

### 数据量统计

- **用户**: 11个（1管理员 + 3业主 + 7租户）
- **物业**: 20个（分布在郑州、洛阳、开封）
- **租约**: 15个（11活跃 + 2过期 + 2终止）
- **支付记录**: 80+条（2024年6月-2025年1月）
- **维修请求**: 20条（4待处理 + 4处理中 + 10已完成 + 2已取消）

### 物业类型分布

- **APARTMENT（公寓）**: 14个
- **HOUSE（别墅）**: 2个
- **COMMERCIAL（商业）**: 3个

### 城市分布

- **郑州**: 14个物业
- **洛阳**: 3个物业
- **开封**: 2个物业


## ⚠️ 注意事项

1. **数据库删除**：`00_db_drop_and_create.sql` 会删除旧数据库，执行前请备份
2. **字符集**：使用 utf8mb4 支持中文
3. **外键**：数据库层面不使用物理外键，JPA层面的关系映射仍有效



## 🆘 常见问题

**Q: 如何重建数据库？**  
A: 按顺序重新执行 SQL 脚本即可

**Q: 后端启动失败？**  
A: 检查 `application.properties` 中数据库名是否为 `smart_property_system`

---

**最后更新**: 2025-01-14 | **版本**: 2.0
