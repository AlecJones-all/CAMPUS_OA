USE campus_oa;
SET NAMES utf8mb4;

-- 管理员固定拥有全部有效菜单入口，避免角色映射缺失导致被锁出功能。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.status = 1
WHERE r.role_code = 'ADMIN'
  AND r.status = 1;

-- 管理员固定拥有全部有效按钮/接口权限。
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.status = 1
WHERE r.role_code = 'ADMIN'
  AND r.status = 1;

-- 评审专家保留工作台、通用审批和科研事务入口，用于处理课题申报待办。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.permission_code IN ('dashboard', 'workflow', 'research')
WHERE r.role_code = 'REVIEWER'
  AND r.status = 1
  AND m.status = 1;

-- 教师需要进入学生事务域处理指定给自己的毕业设计开题/中期审批入口。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.permission_code = 'student-affairs'
WHERE r.role_code = 'TEACHER'
  AND r.status = 1
  AND m.status = 1;

-- 学工处可进入后勤域查看公告发布等允许公开/可见记录。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.permission_code = 'logistics'
WHERE r.role_code = 'STUDENT_AFFAIRS'
  AND r.status = 1
  AND m.status = 1;

-- 课题申报由评审专家审批，科技处不再作为课题申报审批角色。
UPDATE wf_application_type
SET approver_role_code = 'REVIEWER',
    updated_at = NOW()
WHERE type_code = 'RESEARCH_PROJECT_REVIEW';

UPDATE wf_node_definition n
JOIN wf_definition d ON d.id = n.definition_id
SET n.node_name = '评审专家审批',
    n.approver_role_code = 'REVIEWER',
    n.updated_at = NOW()
WHERE d.business_type = 'RESEARCH_PROJECT_REVIEW'
  AND n.node_code = 'APPROVE_1';
