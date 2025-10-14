-- ============================================================
-- 智慧物业管理平台：管理员初始化脚本
-- 版本：2.0
-- 创建日期：2025-01-14
-- ============================================================
-- 说明：
--   1. 初始化系统角色（管理员、业主、租户）
--   2. 创建默认管理员账户
--   3. 管理员密码：admin123（已BCrypt加密）
-- ============================================================

USE `smart_property_system`;

-- ============================================================
-- 1. 初始化角色数据
-- ============================================================
INSERT INTO `role` (`name`) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_OWNER'),
    ('ROLE_TENANT')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ============================================================
-- 2. 创建默认管理员账户
-- ============================================================
-- 密码：admin123（BCrypt加密后）
-- 加密值：$2a$10$qo06sIjgT3TtGKjop/KVOefbQKDxaxl7d7RhfB70UEAWuYKS9BBn.
INSERT INTO `user` (`username`, `email`, `password`, `first_name`, `last_name`, `phone_number`)
VALUES ('admin', 'admin@smartproperty.com', '$2a$10$qo06sIjgT3TtGKjop/KVOefbQKDxaxl7d7RhfB70UEAWuYKS9BBn.', 'System', 'Admin', '400-8888-8888')
ON DUPLICATE KEY UPDATE `email` = VALUES(`email`);

-- ============================================================
-- 3. 为管理员分配角色
-- ============================================================
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u
CROSS JOIN `role` r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);

-- ============================================================
-- 验证初始化结果
-- ============================================================
-- 查看角色列表
SELECT * FROM `role` ORDER BY `id`;

-- 查看管理员信息
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.phone_number,
    GROUP_CONCAT(r.name) as roles
FROM `user` u
LEFT JOIN `user_role` ur ON u.id = ur.user_id
LEFT JOIN `role` r ON ur.role_id = r.id
WHERE u.username = 'admin'
GROUP BY u.id;

-- ============================================================
-- 执行完成提示
-- ============================================================
SELECT CONCAT(
    '管理员初始化成功！',
    '\n用户名: admin',
    '\n密码: admin123',
    '\n\n请继续执行 03_db_mock_data.sql'
) as message;
