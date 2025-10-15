-- 修复数据库外键约束问题
-- 问题：notification.recipient_id 与 user.id 的外键约束不兼容

USE property_db;

-- 1. 首先检查现有的表结构
DESCRIBE user;
DESCRIBE notification;

-- 2. 删除可能存在的外键约束
SET FOREIGN_KEY_CHECKS = 0;

-- 查找并删除现有的外键约束
SELECT CONSTRAINT_NAME 
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'notification' 
  AND COLUMN_NAME = 'recipient_id' 
  AND TABLE_SCHEMA = 'property_db';

-- 删除可能存在的外键约束（如果存在的话）
ALTER TABLE notification DROP FOREIGN KEY IF EXISTS FKqnduwq6ix2pxx1add03905i1i;

-- 3. 确保字段类型一致
-- 将 notification.recipient_id 修改为与 user.id 相同的类型
ALTER TABLE notification MODIFY COLUMN recipient_id BIGINT NOT NULL;

-- 4. 确保 user.id 是正确的类型
ALTER TABLE user MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- 5. 清理可能的无效数据
-- 删除 notification 表中 recipient_id 不存在于 user 表中的记录
DELETE n FROM notification n 
LEFT JOIN user u ON n.recipient_id = u.id 
WHERE u.id IS NULL;

-- 6. 重新创建外键约束
ALTER TABLE notification 
ADD CONSTRAINT FK_notification_recipient 
FOREIGN KEY (recipient_id) REFERENCES user(id) ON DELETE CASCADE;

-- 7. 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 8. 验证修复结果
SELECT 'Foreign key constraint created successfully' AS status;

-- 查看最终的表结构
DESCRIBE user;
DESCRIBE notification;

-- 检查外键约束
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'notification' 
  AND COLUMN_NAME = 'recipient_id' 
  AND TABLE_SCHEMA = 'property_db';