USE smart_property_system;

UPDATE user 
SET password = '$2a$10$qo06sIjgT3TtGKjop/KVOefbQKDxaxl7d7RhfB70UEAWuYKS9BBn.'
WHERE username = 'admin';

SELECT username, password, LENGTH(password) as pwd_len, HEX(password) as pwd_hex 
FROM user 
WHERE username = 'admin';
