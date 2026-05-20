USE campus_oa;
SET NAMES utf8mb4;

-- Admin system-access repair script.
-- Idempotently keeps the ADMIN role, admin account, role binding, menus and permissions usable.

INSERT INTO sys_role (role_code, role_name, status)
SELECT 'ADMIN', '系统管理员', 1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE role_code = 'ADMIN'
);

UPDATE sys_role
SET role_name = '系统管理员',
    status = 1,
    updated_at = NOW()
WHERE role_code = 'ADMIN';

INSERT INTO sys_user (username, password_hash, real_name, user_type, status)
SELECT 'admin', '123456', '系统管理员', 'ADMIN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'admin'
);

UPDATE sys_user
SET user_type = 'ADMIN',
    status = 1,
    updated_at = NOW()
WHERE username = 'admin';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = 'ADMIN' AND r.status = 1
WHERE u.user_type = 'ADMIN';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.status = 1
WHERE r.role_code = 'ADMIN'
  AND r.status = 1;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.status = 1
WHERE r.role_code = 'ADMIN'
  AND r.status = 1;
