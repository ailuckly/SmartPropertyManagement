USE smart_property_system;

-- Update admin password with correctly hashed BCrypt value for "admin123"
UPDATE `user` 
SET `password` = '$2a$10$qo06sIjgT3TtGKjop/KVOefbQKDxaxl7d7RhfB70UEAWuYKS9BBn.' 
WHERE `username` = 'admin';

-- Verify the update
SELECT id, username, email, password FROM `user` WHERE username = 'admin';
