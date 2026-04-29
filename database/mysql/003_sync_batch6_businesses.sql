CREATE TABLE IF NOT EXISTS biz_academic_lecture_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    lecture_topic VARCHAR(200) NOT NULL,
    speaker_name VARCHAR(100) NOT NULL,
    lecture_time DATETIME NOT NULL,
    lecture_location VARCHAR(200) NOT NULL,
    audience_scope VARCHAR(200) NOT NULL,
    budget_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    attachment_note VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_academic_lecture_workflow (workflow_application_id),
    KEY idx_biz_academic_lecture_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_office_supply_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    request_quantity INT NOT NULL,
    usage_purpose VARCHAR(500) NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    remarks VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_office_supply_workflow (workflow_application_id),
    KEY idx_biz_office_supply_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_lab_safety_hazard_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    lab_name VARCHAR(200) NOT NULL,
    hazard_type VARCHAR(100) NOT NULL,
    hazard_description VARCHAR(1000) NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    rectification_requirement VARCHAR(1000) NOT NULL,
    attachment_note VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_lab_hazard_workflow (workflow_application_id),
    KEY idx_biz_lab_hazard_reporter (reporter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_dorm_adjustment_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    student_info VARCHAR(200) NOT NULL,
    current_dormitory VARCHAR(100) NOT NULL,
    target_dormitory VARCHAR(100) NOT NULL,
    adjustment_reason VARCHAR(1000) NOT NULL,
    remarks VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_dorm_adjustment_workflow (workflow_application_id),
    KEY idx_biz_dorm_adjustment_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_stamp_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    request_subject VARCHAR(200) NOT NULL,
    seal_type VARCHAR(100) NOT NULL,
    usage_time DATETIME NOT NULL,
    document_name VARCHAR(200) NOT NULL,
    document_count INT NOT NULL,
    attachment_note VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_stamp_workflow (workflow_application_id),
    KEY idx_biz_stamp_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO wf_application_type (id, type_code, type_name, description_text, approver_role_code, status) VALUES
    (41, 'ACADEMIC_LECTURE_APPLICATION', '学术讲座申请', '教师提交学术讲座申请。', 'RESEARCH', 1),
    (42, 'OFFICE_SUPPLY_REQUEST', '办公用品申领', '教师提交办公用品申领。', 'ADMIN', 1),
    (43, 'LAB_SAFETY_HAZARD_REPORT', '实验室安全隐患上报', '教师提交实验室安全隐患上报。', 'ADMIN', 1),
    (44, 'DORM_ADJUSTMENT_REQUEST', '宿舍调宿申请', '学生提交宿舍调宿申请。', 'ADVISER', 1),
    (45, 'STAMP_REQUEST', '用章申请', '教师提交用章申请。', 'ADMIN', 1);

INSERT IGNORE INTO wf_definition (id, business_type, definition_code, definition_name, version_no, status) VALUES
    (31, 'ACADEMIC_LECTURE_APPLICATION', 'WF_ACADEMIC_LECTURE_APPLICATION', '学术讲座申请流程', 1, 1),
    (32, 'OFFICE_SUPPLY_REQUEST', 'WF_OFFICE_SUPPLY_REQUEST', '办公用品申领流程', 1, 1),
    (33, 'LAB_SAFETY_HAZARD_REPORT', 'WF_LAB_SAFETY_HAZARD_REPORT', '实验室安全隐患上报流程', 1, 1),
    (34, 'DORM_ADJUSTMENT_REQUEST', 'WF_DORM_ADJUSTMENT_REQUEST', '宿舍调宿申请流程', 1, 1),
    (35, 'STAMP_REQUEST', 'WF_STAMP_REQUEST', '用章申请流程', 1, 1);

INSERT IGNORE INTO wf_node_definition (
    id, definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
) VALUES
    (1031, 31, 'APPROVE_1', '科技处审批', 'APPROVAL', 'ROLE', 'RESEARCH', 1, 1),
    (1032, 32, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1033, 33, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1034, 34, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1035, 35, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1);
