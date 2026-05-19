USE campus_oa;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS biz_vehicle_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    use_start_time DATETIME NOT NULL,
    use_end_time DATETIME NOT NULL,
    destination VARCHAR(200) NOT NULL,
    passenger_count INT NOT NULL,
    use_reason VARCHAR(1000) NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(50) NOT NULL,
    dispatch_requirement VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_vehicle_workflow (workflow_application_id),
    KEY idx_biz_vehicle_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_class_notice_receipt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    publisher_id BIGINT NOT NULL,
    target_class VARCHAR(200) NOT NULL,
    notice_title VARCHAR(200) NOT NULL,
    notice_content VARCHAR(2000) NOT NULL,
    deadline_time DATETIME NOT NULL,
    receipt_required TINYINT NOT NULL DEFAULT 1,
    expected_receipt_count INT NOT NULL,
    received_receipt_count INT NOT NULL DEFAULT 0,
    attachment_note VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_class_notice_workflow (workflow_application_id),
    KEY idx_biz_class_notice_publisher (publisher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_student_warning_process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    handler_id BIGINT NOT NULL,
    student_no VARCHAR(64) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    warning_type VARCHAR(100) NOT NULL,
    warning_level VARCHAR(30) NOT NULL,
    trigger_reason VARCHAR(1000) NOT NULL,
    process_record VARCHAR(1000) NOT NULL,
    follow_up_plan VARCHAR(1000) NOT NULL,
    attachment_note VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_warning_workflow (workflow_application_id),
    KEY idx_biz_warning_handler (handler_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_material_supplement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    related_business_no VARCHAR(100) NOT NULL,
    return_reason VARCHAR(1000) NOT NULL,
    supplement_description VARCHAR(1000) NOT NULL,
    supplement_material_note VARCHAR(1000) NOT NULL,
    original_reviewer VARCHAR(100) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_material_supplement_workflow (workflow_application_id),
    KEY idx_biz_material_supplement_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_announcement_publish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT NOT NULL,
    publisher_id BIGINT NOT NULL,
    announcement_title VARCHAR(200) NOT NULL,
    announcement_content VARCHAR(3000) NOT NULL,
    publish_scope VARCHAR(200) NOT NULL,
    planned_publish_time DATETIME NOT NULL,
    top_flag TINYINT NOT NULL DEFAULT 0,
    attachment_note VARCHAR(500) NOT NULL,
    business_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    flow_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_announcement_workflow (workflow_application_id),
    KEY idx_biz_announcement_publisher (publisher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO wf_application_type (id, type_code, type_name, description_text, approver_role_code, status) VALUES
    (46, 'VEHICLE_REQUEST', '车辆申请', '教师提交公务用车申请。', 'ADMIN', 1),
    (47, 'CLASS_NOTICE_RECEIPT', '班级通知回执', '班主任发布班级通知并统计回执。', 'ADVISER', 1),
    (48, 'STUDENT_WARNING_PROCESS', '学生预警处理', '班主任登记并处理学生预警。', 'STUDENT_AFFAIRS', 1),
    (49, 'MATERIAL_SUPPLEMENT', '证明材料补交', '学生补交被退回或缺失的证明材料。', 'ADVISER', 1),
    (50, 'ANNOUNCEMENT_PUBLISH', '公告发布', '管理员或部门人员提交公告发布申请。', 'ADMIN', 1);

INSERT IGNORE INTO wf_definition (id, business_type, definition_code, definition_name, version_no, status) VALUES
    (36, 'VEHICLE_REQUEST', 'WF_VEHICLE_REQUEST', '车辆申请流程', 1, 1),
    (37, 'CLASS_NOTICE_RECEIPT', 'WF_CLASS_NOTICE_RECEIPT', '班级通知回执流程', 1, 1),
    (38, 'STUDENT_WARNING_PROCESS', 'WF_STUDENT_WARNING_PROCESS', '学生预警处理流程', 1, 1),
    (39, 'MATERIAL_SUPPLEMENT', 'WF_MATERIAL_SUPPLEMENT', '证明材料补交流程', 1, 1),
    (40, 'ANNOUNCEMENT_PUBLISH', 'WF_ANNOUNCEMENT_PUBLISH', '公告发布流程', 1, 1);

INSERT IGNORE INTO wf_node_definition (
    id, definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
) VALUES
    (1036, 36, 'APPROVE_1', '管理员排车', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1037, 37, 'APPROVE_1', '班主任确认发布', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1038, 38, 'APPROVE_1', '学工处复核', 'APPROVAL', 'ROLE', 'STUDENT_AFFAIRS', 1, 1),
    (1039, 39, 'APPROVE_1', '班主任复核', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1040, 40, 'APPROVE_1', '管理员审核发布', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1);
