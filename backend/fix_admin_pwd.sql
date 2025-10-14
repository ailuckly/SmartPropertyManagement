USE smart_property_system;

-- Delete and recreate admin user with correct password
DELETE FROM refresh_token WHERE user_id = 1;
DELETE FROM user_role WHERE user_id = 1;
DELETE FROM user WHERE username = 'admin';

-- Insert admin with correct BCrypt password for "admin123"
INSERT INTO `user` (id, username, email, password, first_name, last_name, phone_number, gmt_create, gmt_modified, is_deleted)
VALUES (
    1,
    'admin',
    'admin@smartproperty.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka',
    'System',
    'Admin',
    '400-8888-8888',
    NOW(),
    NOW(),
    0
);

-- Assign ROLE_ADMIN to admin user
INSERT INTO user_role (user_id, role_id)
SELECT 1, id FROM role WHERE name = 'ROLE_ADMIN';

-- Verify
SELECT id, username, email, password, LENGTH(password) as pwd_len FROM user WHERE username = 'admin';
