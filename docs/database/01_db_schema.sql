-- ============================================================
-- 智慧物业管理平台：表结构定义脚本（阿里巴巴规范）
-- 版本：2.0
-- 创建日期：2025-01-14
-- ============================================================
-- 阿里巴巴MySQL规范要点：
--   1. 表名、字段名使用小写字母、数字、下划线，禁止使用复数
--   2. 表必备字段：id, gmt_create, gmt_modified, is_deleted
--   3. 主键：id BIGINT UNSIGNED，单表自增
--   4. 索引命名：pk(主键)、uk(唯一)、idx(普通)
--   5. 不使用物理外键（使用软外键）
--   6. 所有字段必须添加COMMENT
--   7. 字符集：utf8mb4
-- ============================================================

USE `smart_property_system`;

-- ============================================================
-- 1. 角色表（role）
-- ============================================================
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(32) NOT NULL COMMENT '角色名称，如ROLE_ADMIN、ROLE_OWNER、ROLE_TENANT',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================
-- 2. 用户表（user）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `first_name` VARCHAR(50) DEFAULT NULL COMMENT '名',
    `last_name` VARCHAR(50) DEFAULT NULL COMMENT '姓',
    `phone_number` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_phone` (`phone_number`),
    KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 3. 用户角色关联表（user_role）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================================
-- 4. 物业表（property）
-- ============================================================
CREATE TABLE IF NOT EXISTS `property` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '业主用户ID',
    `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `city` VARCHAR(100) DEFAULT NULL COMMENT '城市',
    `state` VARCHAR(100) DEFAULT NULL COMMENT '省份/州',
    `zip_code` VARCHAR(20) DEFAULT NULL COMMENT '邮政编码',
    `property_type` VARCHAR(20) NOT NULL COMMENT '物业类型：APARTMENT-公寓, HOUSE-别墅, COMMERCIAL-商业',
    `bedrooms` INT DEFAULT NULL COMMENT '卧室数量',
    `bathrooms` DECIMAL(3,1) DEFAULT NULL COMMENT '卫生间数量',
    `square_footage` INT DEFAULT NULL COMMENT '建筑面积（平方米）',
    `status` VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE' COMMENT '物业状态：AVAILABLE-可租, LEASED-已租, UNDER_MAINTENANCE-维护中',
    `rent_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '参考租金（元/月）',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_city_status` (`city`, `status`),
    KEY `idx_property_type` (`property_type`),
    KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物业表';

-- ============================================================
-- 5. 租约表（lease）
-- ============================================================
CREATE TABLE IF NOT EXISTS `lease` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `property_id` BIGINT UNSIGNED NOT NULL COMMENT '物业ID',
    `tenant_id` BIGINT UNSIGNED NOT NULL COMMENT '租户用户ID',
    `start_date` DATE NOT NULL COMMENT '租期开始日期',
    `end_date` DATE NOT NULL COMMENT '租期结束日期',
    `rent_amount` DECIMAL(10,2) NOT NULL COMMENT '月租金额（元）',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '租约状态：ACTIVE-活跃, EXPIRED-已过期, TERMINATED-已终止',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_property_id` (`property_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_date_range` (`start_date`, `end_date`),
    KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租约表';

-- ============================================================
-- 6. 支付记录表（payment）
-- ============================================================
CREATE TABLE IF NOT EXISTS `payment` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `lease_id` BIGINT UNSIGNED NOT NULL COMMENT '租约ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额（元）',
    `payment_date` DATE NOT NULL COMMENT '支付日期',
    `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '支付方式：银行转账、支付宝、微信支付等',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_lease_id` (`lease_id`),
    KEY `idx_payment_date` (`payment_date`),
    KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ============================================================
-- 7. 维修请求表（maintenance_request）
-- ============================================================
CREATE TABLE IF NOT EXISTS `maintenance_request` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `property_id` BIGINT UNSIGNED NOT NULL COMMENT '物业ID',
    `tenant_id` BIGINT UNSIGNED NOT NULL COMMENT '提交租户ID',
    `description` TEXT NOT NULL COMMENT '问题描述',
    `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '维修状态：PENDING-待处理, IN_PROGRESS-处理中, COMPLETED-已完成, CANCELLED-已取消',
    `reported_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_property_id` (`property_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_reported_at` (`reported_at`),
    KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维修请求表';

-- ============================================================
-- 8. 刷新令牌表（refresh_token）
-- ============================================================
CREATE TABLE IF NOT EXISTS `refresh_token` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `token` VARCHAR(255) NOT NULL COMMENT '刷新令牌值',
    `expiry_date` DATETIME NOT NULL COMMENT '过期时间',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_expiry_date` (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷新令牌表';

-- ============================================================
-- 验证表创建结果
-- ============================================================
SELECT 
    TABLE_NAME as '表名',
    TABLE_COMMENT as '说明',
    ENGINE as '引擎',
    TABLE_COLLATION as '字符集'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'smart_property_system'
ORDER BY TABLE_NAME;

-- ============================================================
-- 执行完成提示
-- ============================================================
SELECT '表结构创建成功！请继续执行 02_db_init_admin.sql' as message;
