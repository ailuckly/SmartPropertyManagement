# 数据库外键约束修复指南

## 🚨 问题描述

启动应用时遇到以下错误：
```
Referencing column 'recipient_id' and referenced column 'id' in foreign key constraint 'FKqnduwq6ix2pxx1add03905i1i' are incompatible.
```

这是因为 `notification` 表的 `recipient_id` 字段与 `user` 表的 `id` 字段类型不兼容导致的。

## 🔧 修复方案

### 方案一：快速修复（推荐）

1. **停止应用**（如果正在运行）

2. **连接到 MySQL 数据库**
```bash
mysql -u root -p
```

3. **执行修复SQL**
```sql
-- 切换到项目数据库
USE property_db;  -- 或者 USE smart_property_system;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除现有的外键约束（如果存在）
ALTER TABLE notification DROP FOREIGN KEY IF EXISTS FKqnduwq6ix2pxx1add03905i1i;

-- 确保字段类型一致
ALTER TABLE user MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE notification MODIFY COLUMN recipient_id BIGINT;

-- 删除可能的无效数据
DELETE n FROM notification n 
LEFT JOIN user u ON n.recipient_id = u.id 
WHERE u.id IS NULL;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
```

4. **重启应用**

### 方案二：使用提供的SQL脚本

直接运行项目中的 `backend/fix_database.sql` 脚本：

```bash
cd backend
mysql -u root -p < fix_database.sql
```

### 方案三：重建数据库（如果数据不重要）

如果这是开发环境且没有重要数据：

```sql
-- 删除现有数据库
DROP DATABASE IF EXISTS property_db;
DROP DATABASE IF EXISTS smart_property_system;

-- 重新创建数据库
CREATE DATABASE property_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 或者
CREATE DATABASE smart_property_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后重启应用，Hibernate会自动创建表结构。

## 🔍 根本原因

1. **数据库配置不一致**：
   - 配置文件中数据库名：`smart_property_system`
   - 实际使用的数据库名：`property_db`

2. **字段类型不匹配**：
   - 可能是由于之前的迁移或手动修改造成的类型不一致

3. **外键约束创建失败**：
   - Hibernate 在启动时尝试创建外键约束失败

## ⚙️ 预防措施

1. **统一数据库命名**：
   建议将 `application.properties` 中的数据库名统一：
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/property_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   ```

2. **使用数据库迁移工具**：
   考虑使用 Flyway 或 Liquibase 来管理数据库版本

3. **添加数据库初始化脚本**：
   在 `src/main/resources` 下添加 `schema.sql` 和 `data.sql`

## 🔧 如果修复后仍有问题

1. **检查MySQL版本兼容性**
2. **确认所有相关表的字符集一致**
3. **检查是否有循环外键约束**
4. **考虑临时禁用 Hibernate 的 DDL 自动生成**：
   ```properties
   spring.jpa.hibernate.ddl-auto=none
   ```

## 📞 支持

如果以上方案都无法解决问题，请：

1. 提供完整的数据库表结构：`SHOW CREATE TABLE user; SHOW CREATE TABLE notification;`
2. 提供MySQL版本信息：`SELECT VERSION();`
3. 检查是否有其他应用同时连接此数据库

---

**注意**：修复前请备份重要数据！