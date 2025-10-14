-- ============================================================
-- 智慧物业管理平台：数据库初始化脚本（MySQL 8+）
-- 使用说明：
--   1. 在执行脚本前，请确认已创建目标数据库（例如 property_db）。
--   2. 可根据需要调整注释中的占位信息（如默认管理员账号）。
--   3. 如搭配 Spring Boot 使用，建议执行完成后再启动应用。
-- ============================================================

-- 若存在旧表，可根据需要取消注释执行清理
-- DROP TABLE IF EXISTS maintenance_requests;
-- DROP TABLE IF EXISTS payments;
-- DROP TABLE IF EXISTS leases;
-- DROP TABLE IF EXISTS properties;
-- DROP TABLE IF EXISTS refresh_tokens;
-- DROP TABLE IF EXISTS user_roles;
-- DROP TABLE IF EXISTS roles;
-- DROP TABLE IF EXISTS users;

-- ======================
-- 核心数据表结构定义
-- ======================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    first_name VARCHAR(50) COMMENT '名',
    last_name VARCHAR(50) COMMENT '姓',
    phone_number VARCHAR(20) COMMENT '手机号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE COMMENT '角色名，如 ROLE_ADMIN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS properties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL COMMENT '业主 ID，关联 users',
    address VARCHAR(255) NOT NULL COMMENT '详细地址',
    city VARCHAR(100) COMMENT '城市',
    state VARCHAR(100) COMMENT '省/州',
    zip_code VARCHAR(20) COMMENT '邮编',
    property_type ENUM('APARTMENT', 'HOUSE', 'COMMERCIAL') NOT NULL COMMENT '物业类型',
    bedrooms INT COMMENT '卧室数',
    bathrooms DECIMAL(3,1) COMMENT '卫生间数',
    square_footage INT COMMENT '面积（㎡）',
    status ENUM('AVAILABLE', 'LEASED', 'UNDER_MAINTENANCE') DEFAULT 'AVAILABLE' NOT NULL COMMENT '物业状态',
    rent_amount DECIMAL(10,2) COMMENT '参考租金',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT fk_properties_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS leases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT NOT NULL COMMENT '物业 ID',
    tenant_id BIGINT NOT NULL COMMENT '租客 ID',
    start_date DATE NOT NULL COMMENT '租期开始',
    end_date DATE NOT NULL COMMENT '租期结束',
    rent_amount DECIMAL(10,2) NOT NULL COMMENT '月租金额',
    status ENUM('ACTIVE', 'EXPIRED', 'TERMINATED') DEFAULT 'ACTIVE' NOT NULL COMMENT '租约状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_leases_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    CONSTRAINT fk_leases_tenant FOREIGN KEY (tenant_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lease_id BIGINT NOT NULL COMMENT '租约 ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    payment_date DATE NOT NULL COMMENT '支付日期',
    payment_method VARCHAR(50) COMMENT '支付方式',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_payments_lease FOREIGN KEY (lease_id) REFERENCES leases(id) ON DELETE CASCADE,
    INDEX idx_payments_lease (lease_id, payment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS maintenance_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT NOT NULL COMMENT '关联物业 ID',
    tenant_id BIGINT NOT NULL COMMENT '提交租客 ID',
    description TEXT NOT NULL COMMENT '问题描述',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING' NOT NULL COMMENT '维修状态',
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '提交时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    CONSTRAINT fk_maintenance_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    CONSTRAINT fk_maintenance_tenant FOREIGN KEY (tenant_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户 ID',
    token VARCHAR(255) NOT NULL UNIQUE COMMENT '刷新令牌值',
    expiry_date DATETIME NOT NULL COMMENT '过期时间',
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ======================
-- 初始数据（可按需修改）
-- ======================

INSERT INTO roles (name) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_OWNER'),
    ('ROLE_TENANT')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 创建一个默认管理员账户（密码需替换为 BCrypt 加密后的值）
-- 例如可使用 Spring Boot/BCrypt 工具生成密文后粘贴
INSERT INTO users (username, email, password, first_name, last_name, phone_number)
VALUES ('admin', 'admin@example.com', '{bcrypt_password_here}', 'System', 'Admin', '000-0000')
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- 绑定管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 可选：添加演示用业主、租客与物业
-- INSERT INTO users (username, email, password, first_name, last_name)
-- VALUES ('owner01', 'owner01@example.com', '{bcrypt_password_here}', 'Alice', 'Owner'),
--        ('tenant01', 'tenant01@example.com', '{bcrypt_password_here}', 'Bob', 'Tenant');
--
-- INSERT INTO user_roles (user_id, role_id)
-- SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_OWNER' WHERE u.username = 'owner01'
-- UNION ALL
-- SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_TENANT' WHERE u.username = 'tenant01';
--
-- INSERT INTO properties (owner_id, address, city, state, zip_code, property_type, bedrooms, bathrooms, square_footage, status, rent_amount)
-- SELECT u.id, 'XX路123号', '郑州', '河南', '450000', 'APARTMENT', 3, 1.0, 120, 'AVAILABLE', 3200.00
-- FROM users u WHERE u.username = 'owner01';

-- ======================
-- 脚本结束
-- ======================
