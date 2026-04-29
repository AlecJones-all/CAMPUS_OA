USE campus_oa;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS biz_student_leave_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    leave_type VARCHAR(64) NOT NULL,
    leave_start_time DATETIME NOT NULL,
    leave_end_time DATETIME NOT NULL,
    leave_destination VARCHAR(255) NOT NULL,
    leave_reason VARCHAR(500) NOT NULL,
    emergency_contact VARCHAR(128) NOT NULL,
    emergency_phone VARCHAR(32) NOT NULL,
    return_plan VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_student_leave_workflow (workflow_application_id),
    KEY idx_biz_student_leave_student (student_id)
);
ALTER TABLE biz_student_leave_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_student_return_confirmation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    related_leave_no VARCHAR(64) NOT NULL,
    return_time DATETIME NOT NULL,
    return_note VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_student_return_workflow (workflow_application_id),
    KEY idx_biz_student_return_student (student_id)
);
ALTER TABLE biz_student_return_confirmation CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_graduation_project_opening (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    topic_direction VARCHAR(200) NOT NULL,
    advisor_name VARCHAR(128) NOT NULL,
    opening_date DATE NOT NULL,
    opening_summary VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_graduation_opening_workflow (workflow_application_id),
    KEY idx_biz_graduation_opening_student (student_id)
);
ALTER TABLE biz_graduation_project_opening CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_graduation_project_midterm (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    progress_rate INT NOT NULL,
    midterm_date DATE NOT NULL,
    problems_found VARCHAR(500) NOT NULL,
    rectification_plan VARCHAR(500) NOT NULL,
    midterm_summary VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_graduation_midterm_workflow (workflow_application_id),
    KEY idx_biz_graduation_midterm_student (student_id)
);
ALTER TABLE biz_graduation_project_midterm CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_research_achievement_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    achievement_name VARCHAR(200) NOT NULL,
    achievement_type VARCHAR(64) NOT NULL,
    publish_time DATE NOT NULL,
    issue_unit VARCHAR(255) NOT NULL,
    achievement_level VARCHAR(64) NOT NULL,
    achievement_summary VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_research_achievement_workflow (workflow_application_id),
    KEY idx_biz_research_achievement_teacher (teacher_id)
);
ALTER TABLE biz_research_achievement_registration CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO wf_application_type (id, type_code, type_name, description_text, approver_role_code, status)
SELECT 36, 'STUDENT_LEAVE_APPLICATION', '学生离校申请', '学生提交离校申请。', 'ADVISER', 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_application_type WHERE type_code = 'STUDENT_LEAVE_APPLICATION'
);

INSERT INTO wf_application_type (id, type_code, type_name, description_text, approver_role_code, status)
SELECT 37, 'STUDENT_RETURN_CONFIRMATION', '学生返校确认', '学生提交返校确认。', 'ADVISER', 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_application_type WHERE type_code = 'STUDENT_RETURN_CONFIRMATION'
);

INSERT INTO wf_application_type (id, type_code, type_name, description_text, approver_role_code, status)
SELECT 38, 'GRADUATION_PROJECT_OPENING', '毕业设计开题申请', '学生提交毕业设计开题申请。', 'TEACHER', 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_application_type WHERE type_code = 'GRADUATION_PROJECT_OPENING'
);

INSERT INTO wf_application_type (id, type_code, type_name, description_text, approver_role_code, status)
SELECT 39, 'GRADUATION_PROJECT_MIDTERM', '毕业设计中期检查', '学生提交毕业设计中期检查。', 'TEACHER', 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_application_type WHERE type_code = 'GRADUATION_PROJECT_MIDTERM'
);

INSERT INTO wf_application_type (id, type_code, type_name, description_text, approver_role_code, status)
SELECT 40, 'RESEARCH_ACHIEVEMENT_REGISTRATION', '科研成果登记', '教师提交科研成果登记。', 'RESEARCH', 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_application_type WHERE type_code = 'RESEARCH_ACHIEVEMENT_REGISTRATION'
);

INSERT INTO wf_definition (id, business_type, definition_code, definition_name, version_no, status)
SELECT 26, 'STUDENT_LEAVE_APPLICATION', 'WF_STUDENT_LEAVE_APPLICATION', '学生离校申请流程', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_definition WHERE business_type = 'STUDENT_LEAVE_APPLICATION' AND version_no = 1
);

INSERT INTO wf_definition (id, business_type, definition_code, definition_name, version_no, status)
SELECT 27, 'STUDENT_RETURN_CONFIRMATION', 'WF_STUDENT_RETURN_CONFIRMATION', '学生返校确认流程', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_definition WHERE business_type = 'STUDENT_RETURN_CONFIRMATION' AND version_no = 1
);

INSERT INTO wf_definition (id, business_type, definition_code, definition_name, version_no, status)
SELECT 28, 'GRADUATION_PROJECT_OPENING', 'WF_GRADUATION_PROJECT_OPENING', '毕业设计开题流程', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_definition WHERE business_type = 'GRADUATION_PROJECT_OPENING' AND version_no = 1
);

INSERT INTO wf_definition (id, business_type, definition_code, definition_name, version_no, status)
SELECT 29, 'GRADUATION_PROJECT_MIDTERM', 'WF_GRADUATION_PROJECT_MIDTERM', '毕业设计中期检查流程', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_definition WHERE business_type = 'GRADUATION_PROJECT_MIDTERM' AND version_no = 1
);

INSERT INTO wf_definition (id, business_type, definition_code, definition_name, version_no, status)
SELECT 30, 'RESEARCH_ACHIEVEMENT_REGISTRATION', 'WF_RESEARCH_ACHIEVEMENT_REGISTRATION', '科研成果登记流程', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_definition WHERE business_type = 'RESEARCH_ACHIEVEMENT_REGISTRATION' AND version_no = 1
);

INSERT INTO wf_node_definition (
    id, definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
)
SELECT 1026, 26, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_node_definition WHERE definition_id = 26 AND node_code = 'APPROVE_1'
);

INSERT INTO wf_node_definition (
    id, definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
)
SELECT 1027, 27, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_node_definition WHERE definition_id = 27 AND node_code = 'APPROVE_1'
);

INSERT INTO wf_node_definition (
    id, definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
)
SELECT 1028, 28, 'APPROVE_1', '指导教师审批', 'APPROVAL', 'ROLE', 'TEACHER', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_node_definition WHERE definition_id = 28 AND node_code = 'APPROVE_1'
);

INSERT INTO wf_node_definition (
    id, definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
)
SELECT 1029, 29, 'APPROVE_1', '指导教师审批', 'APPROVAL', 'ROLE', 'TEACHER', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_node_definition WHERE definition_id = 29 AND node_code = 'APPROVE_1'
);

INSERT INTO wf_node_definition (
    id, definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
)
SELECT 1030, 30, 'APPROVE_1', '科技处审批', 'APPROVAL', 'ROLE', 'RESEARCH', 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM wf_node_definition WHERE definition_id = 30 AND node_code = 'APPROVE_1'
);
