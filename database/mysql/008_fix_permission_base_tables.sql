USE campus_oa;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, menu_id),
    KEY idx_sys_role_menu_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    permission_group VARCHAR(128) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    KEY idx_sys_role_permission_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(128) NOT NULL,
    config_name VARCHAR(128) NOT NULL,
    config_value VARCHAR(500) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 101), (1, 102), (1, 103), (1, 104), (1, 105),
    (2, 1), (2, 2), (2, 3), (2, 6),
    (3, 1), (3, 2), (3, 4), (3, 5), (3, 6),
    (4, 1), (4, 2), (4, 3), (4, 6),
    (5, 1), (5, 2), (5, 5), (5, 6),
    (6, 1), (6, 2), (6, 4), (6, 6),
    (7, 1), (7, 2), (7, 5),
    (8, 1), (8, 2), (8, 3);

INSERT IGNORE INTO sys_permission (id, permission_code, permission_name, permission_group, status) VALUES
    (1, 'system:user:view', '查看用户', 'SYSTEM_USER', 1),
    (2, 'system:user:create', '新增用户', 'SYSTEM_USER', 1),
    (3, 'system:user:update', '编辑用户', 'SYSTEM_USER', 1),
    (4, 'system:user:assign-role', '分配用户角色', 'SYSTEM_USER', 1),
    (5, 'system:org:view', '查看组织', 'SYSTEM_ORG', 1),
    (6, 'system:org:create', '新增组织', 'SYSTEM_ORG', 1),
    (7, 'system:org:update', '编辑组织', 'SYSTEM_ORG', 1),
    (8, 'system:role:view', '查看角色', 'SYSTEM_ROLE', 1),
    (9, 'system:role:create', '新增角色', 'SYSTEM_ROLE', 1),
    (10, 'system:role:update', '编辑角色', 'SYSTEM_ROLE', 1),
    (11, 'system:role:assign-menu', '分配角色菜单', 'SYSTEM_ROLE', 1),
    (12, 'system:role:assign-permission', '分配角色权限', 'SYSTEM_ROLE', 1),
    (13, 'system:file:view', '查看附件', 'SYSTEM_FILE', 1),
    (14, 'system:file:upload', '上传附件', 'SYSTEM_FILE', 1),
    (15, 'system:file:delete', '删除附件', 'SYSTEM_FILE', 1),
    (16, 'system:workflow:view', '查看流程模板', 'SYSTEM_WORKFLOW', 1),
    (17, 'system:workflow:create', '新增流程模板', 'SYSTEM_WORKFLOW', 1),
    (18, 'system:workflow:update', '编辑流程模板', 'SYSTEM_WORKFLOW', 1);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4),
    (1, 5), (1, 6), (1, 7),
    (1, 8), (1, 9), (1, 10), (1, 11), (1, 12),
    (1, 13), (1, 14), (1, 15),
    (1, 16), (1, 17), (1, 18);

INSERT IGNORE INTO sys_config (id, config_key, config_name, config_value, status) VALUES
    (1, 'file.upload.max-size-mb', '附件上传大小限制', '20', 1),
    (2, 'workflow.definition.mode', '流程模板启用方式', 'ROLE_NODE', 1);
