CREATE DATABASE IF NOT EXISTS campus_oa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_oa;
SET NAMES utf8mb4;
ALTER DATABASE campus_oa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_org (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT NULL,
    org_code VARCHAR(64) NOT NULL,
    org_name VARCHAR(128) NOT NULL,
    org_type VARCHAR(32) NOT NULL,
    sort_no INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_org_code (org_code)
);
ALTER TABLE sys_org CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_role_code (role_code)
);
ALTER TABLE sys_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_role MODIFY COLUMN role_code VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE sys_role MODIFY COLUMN role_name VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_id BIGINT DEFAULT NULL,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(128) NOT NULL,
    user_type VARCHAR(32) NOT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    email VARCHAR(128) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_org_id (org_id)
);
ALTER TABLE sys_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_user MODIFY COLUMN username VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE sys_user MODIFY COLUMN real_name VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE sys_user MODIFY COLUMN user_type VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_role (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT NULL,
    menu_name VARCHAR(128) NOT NULL,
    menu_type VARCHAR(32) NOT NULL,
    route_path VARCHAR(255) DEFAULT NULL,
    permission_code VARCHAR(128) DEFAULT NULL,
    sort_no INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE sys_menu CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_type VARCHAR(64) NOT NULL,
    dict_code VARCHAR(64) NOT NULL,
    dict_label VARCHAR(128) NOT NULL,
    sort_no INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_dict_type_code (dict_type, dict_code)
);
ALTER TABLE sys_dict CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wf_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_type VARCHAR(64) NOT NULL,
    definition_code VARCHAR(64) NOT NULL,
    definition_name VARCHAR(128) NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wf_definition_code_version (definition_code, version_no)
);
ALTER TABLE wf_definition CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wf_node_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    definition_id BIGINT NOT NULL,
    node_code VARCHAR(64) NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    node_type VARCHAR(32) NOT NULL,
    approver_type VARCHAR(32) DEFAULT NULL,
    approver_role_code VARCHAR(64) DEFAULT NULL,
    sort_no INT DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_wf_node_definition_def_id (definition_id)
);
ALTER TABLE wf_node_definition CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wf_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    definition_id BIGINT NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    business_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    current_node_code VARCHAR(64) DEFAULT NULL,
    flow_status VARCHAR(32) NOT NULL,
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at DATETIME DEFAULT NULL,
    KEY idx_wf_instance_business (business_type, business_id),
    KEY idx_wf_instance_applicant (applicant_id)
);
ALTER TABLE wf_instance CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wf_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    instance_id BIGINT NOT NULL,
    node_code VARCHAR(64) NOT NULL,
    assignee_id BIGINT NOT NULL,
    task_status VARCHAR(32) NOT NULL,
    arrived_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_at DATETIME DEFAULT NULL,
    KEY idx_wf_task_instance_id (instance_id),
    KEY idx_wf_task_assignee_id (assignee_id)
);
ALTER TABLE wf_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wf_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    instance_id BIGINT NOT NULL,
    task_id BIGINT DEFAULT NULL,
    action_type VARCHAR(32) NOT NULL,
    action_by BIGINT NOT NULL,
    action_comment VARCHAR(500) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_wf_action_log_instance_id (instance_id)
);
ALTER TABLE wf_action_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wf_application_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_code VARCHAR(64) NOT NULL,
    type_name VARCHAR(128) NOT NULL,
    description_text VARCHAR(500) DEFAULT NULL,
    approver_role_code VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wf_application_type_code (type_code)
);
ALTER TABLE wf_application_type CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE wf_application_type MODIFY COLUMN type_code VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE wf_application_type MODIFY COLUMN type_name VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE wf_application_type MODIFY COLUMN description_text VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE wf_application_type MODIFY COLUMN approver_role_code VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

CREATE TABLE IF NOT EXISTS wf_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_no VARCHAR(64) NOT NULL,
    type_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content_text TEXT NOT NULL,
    applicant_id BIGINT NOT NULL,
    current_approver_id BIGINT DEFAULT NULL,
    status VARCHAR(32) NOT NULL,
    submitted_at DATETIME DEFAULT NULL,
    finished_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wf_application_no (application_no),
    KEY idx_wf_application_type_id (type_id),
    KEY idx_wf_application_applicant_id (applicant_id),
    KEY idx_wf_application_current_approver_id (current_approver_id),
    KEY idx_wf_application_status (status)
);
ALTER TABLE wf_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE wf_application MODIFY COLUMN application_no VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE wf_application MODIFY COLUMN title VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE wf_application MODIFY COLUMN content_text TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE wf_application MODIFY COLUMN status VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

CREATE TABLE IF NOT EXISTS wf_approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    actor_id BIGINT NOT NULL,
    comment_text VARCHAR(500) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_wf_approval_record_application_id (application_id)
);
ALTER TABLE wf_approval_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_type VARCHAR(64) NOT NULL,
    business_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    content_type VARCHAR(128) DEFAULT NULL,
    uploaded_by BIGINT NOT NULL,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_file_attachment_business (business_type, business_id)
);
ALTER TABLE file_attachment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS msg_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notice_type VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(1000) DEFAULT NULL,
    read_flag TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_msg_notice_user_id (user_id)
);
ALTER TABLE msg_notice CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_leave_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    class_org_id BIGINT DEFAULT NULL,
    leave_type VARCHAR(32) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    reason VARCHAR(500) NOT NULL,
    emergency_contact VARCHAR(128) DEFAULT NULL,
    emergency_phone VARCHAR(32) DEFAULT NULL,
    destination VARCHAR(255) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE biz_leave_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_leave_request MODIFY COLUMN leave_type VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_leave_request MODIFY COLUMN reason VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_leave_request MODIFY COLUMN emergency_contact VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_leave_request MODIFY COLUMN emergency_phone VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_leave_request MODIFY COLUMN destination VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;

CREATE TABLE IF NOT EXISTS biz_internship_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    semester_code VARCHAR(32) NOT NULL,
    internship_company VARCHAR(200) DEFAULT NULL,
    internship_position VARCHAR(128) DEFAULT NULL,
    tutor_name VARCHAR(128) DEFAULT NULL,
    tutor_phone VARCHAR(32) DEFAULT NULL,
    start_date DATE DEFAULT NULL,
    end_date DATE DEFAULT NULL,
    material_type VARCHAR(64) NOT NULL,
    material_title VARCHAR(200) NOT NULL,
    material_summary VARCHAR(500) DEFAULT NULL,
    submit_status VARCHAR(32) NOT NULL,
    review_status VARCHAR(32) NOT NULL,
    business_status VARCHAR(32) DEFAULT 'DRAFT',
    flow_status VARCHAR(32) DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE biz_internship_material CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_internship_material MODIFY COLUMN internship_company VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_internship_material MODIFY COLUMN internship_position VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_internship_material MODIFY COLUMN tutor_name VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_internship_material MODIFY COLUMN material_type VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_internship_material MODIFY COLUMN material_title VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_internship_material MODIFY COLUMN material_summary VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;

CREATE TABLE IF NOT EXISTS biz_student_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    student_no VARCHAR(64) DEFAULT NULL,
    student_name VARCHAR(128) DEFAULT NULL,
    alert_type VARCHAR(64) NOT NULL,
    alert_level VARCHAR(32) NOT NULL,
    alert_reason VARCHAR(500) DEFAULT NULL,
    problem_description VARCHAR(500) DEFAULT NULL,
    intervention_plan VARCHAR(500) DEFAULT NULL,
    follow_up_status VARCHAR(32) NOT NULL,
    business_status VARCHAR(32) DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE biz_student_alert CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_student_alert MODIFY COLUMN student_name VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_student_alert MODIFY COLUMN alert_type VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_student_alert MODIFY COLUMN alert_level VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_student_alert MODIFY COLUMN alert_reason VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_student_alert MODIFY COLUMN problem_description VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_student_alert MODIFY COLUMN intervention_plan VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_student_alert MODIFY COLUMN follow_up_status VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

CREATE TABLE IF NOT EXISTS biz_course_standard_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    course_code VARCHAR(64) NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    academic_year VARCHAR(32) NOT NULL,
    target_major VARCHAR(128) DEFAULT NULL,
    total_hours INT DEFAULT NULL,
    standard_version VARCHAR(64) DEFAULT NULL,
    revision_note VARCHAR(500) DEFAULT NULL,
    review_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE biz_course_standard_review CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_course_standard_review MODIFY COLUMN course_name VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_course_standard_review MODIFY COLUMN target_major VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_course_standard_review MODIFY COLUMN standard_version VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_course_standard_review MODIFY COLUMN revision_note VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;

CREATE TABLE IF NOT EXISTS biz_project_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    applicant_id BIGINT NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    project_category VARCHAR(64) NOT NULL,
    application_year VARCHAR(32) NOT NULL,
    project_level VARCHAR(64) DEFAULT NULL,
    budget_amount DECIMAL(12, 2) DEFAULT NULL,
    team_members VARCHAR(255) DEFAULT NULL,
    project_summary VARCHAR(1000) DEFAULT NULL,
    review_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE biz_project_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_project_application MODIFY COLUMN project_name VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_project_application MODIFY COLUMN project_category VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE biz_project_application MODIFY COLUMN project_level VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_project_application MODIFY COLUMN team_members VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;
ALTER TABLE biz_project_application MODIFY COLUMN project_summary VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL;

CREATE TABLE IF NOT EXISTS biz_maintenance_ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    applicant_id BIGINT NOT NULL,
    ticket_type VARCHAR(32) NOT NULL,
    location_text VARCHAR(255) NOT NULL,
    problem_desc VARCHAR(500) NOT NULL,
    ticket_status VARCHAR(32) NOT NULL,
    assigned_to BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE biz_maintenance_ticket CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_leave_cancellation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    related_leave_no VARCHAR(64) NOT NULL,
    return_time DATETIME NOT NULL,
    cancel_reason VARCHAR(500) NOT NULL,
    actual_return_note VARCHAR(500) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_leave_cancellation_workflow (workflow_application_id),
    KEY idx_biz_leave_cancellation_student (student_id)
);
ALTER TABLE biz_leave_cancellation CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_schedule_adjustment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    original_time DATETIME NOT NULL,
    adjusted_time DATETIME NOT NULL,
    original_classroom VARCHAR(128) NOT NULL,
    adjusted_classroom VARCHAR(128) NOT NULL,
    adjustment_reason VARCHAR(500) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_schedule_adjustment_workflow (workflow_application_id),
    KEY idx_biz_schedule_adjustment_teacher (teacher_id)
);
ALTER TABLE biz_schedule_adjustment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_meeting_room_booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    applicant_id BIGINT NOT NULL,
    meeting_subject VARCHAR(200) NOT NULL,
    room_name VARCHAR(128) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    attendee_count INT NOT NULL,
    equipment_needs VARCHAR(500) NOT NULL,
    contact_name VARCHAR(128) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_meeting_room_workflow (workflow_application_id),
    KEY idx_biz_meeting_room_applicant (applicant_id)
);
ALTER TABLE biz_meeting_room_booking CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_dorm_repair (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    dorm_building VARCHAR(128) NOT NULL,
    room_no VARCHAR(64) NOT NULL,
    repair_type VARCHAR(64) NOT NULL,
    problem_description VARCHAR(500) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    urgency_level VARCHAR(32) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_dorm_repair_workflow (workflow_application_id),
    KEY idx_biz_dorm_repair_student (student_id)
);
ALTER TABLE biz_dorm_repair CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_asset_repair (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    applicant_id BIGINT NOT NULL,
    asset_no VARCHAR(64) NOT NULL,
    asset_name VARCHAR(200) NOT NULL,
    location_text VARCHAR(255) NOT NULL,
    fault_description VARCHAR(500) NOT NULL,
    urgency_level VARCHAR(32) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_asset_repair_workflow (workflow_application_id),
    KEY idx_biz_asset_repair_applicant (applicant_id)
);
ALTER TABLE biz_asset_repair CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_scholarship_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    scholarship_type VARCHAR(64) NOT NULL,
    grade_rank VARCHAR(64) NOT NULL,
    comprehensive_score VARCHAR(64) NOT NULL,
    award_records VARCHAR(500) DEFAULT NULL,
    family_situation VARCHAR(500) DEFAULT NULL,
    application_reason VARCHAR(500) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_scholarship_workflow (workflow_application_id),
    KEY idx_biz_scholarship_student (student_id)
);
ALTER TABLE biz_scholarship_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_grant_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    family_income VARCHAR(64) NOT NULL,
    household_size INT NOT NULL,
    difficulty_level VARCHAR(32) NOT NULL,
    application_reason VARCHAR(500) NOT NULL,
    special_notes VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_grant_workflow (workflow_application_id),
    KEY idx_biz_grant_student (student_id)
);
ALTER TABLE biz_grant_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_difficulty_recognition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    family_members VARCHAR(255) NOT NULL,
    annual_income VARCHAR(64) NOT NULL,
    special_condition VARCHAR(500) DEFAULT NULL,
    recognition_level VARCHAR(32) NOT NULL,
    application_reason VARCHAR(500) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_difficulty_workflow (workflow_application_id),
    KEY idx_biz_difficulty_student (student_id)
);
ALTER TABLE biz_difficulty_recognition CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_enrollment_certificate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    student_id BIGINT NOT NULL,
    certificate_purpose VARCHAR(255) NOT NULL,
    receiver_org VARCHAR(255) NOT NULL,
    language_type VARCHAR(32) NOT NULL,
    delivery_method VARCHAR(32) NOT NULL,
    remark_text VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_certificate_workflow (workflow_application_id),
    KEY idx_biz_certificate_student (student_id)
);
ALTER TABLE biz_enrollment_certificate CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_textbook_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    textbook_name VARCHAR(200) NOT NULL,
    isbn VARCHAR(64) NOT NULL,
    publisher VARCHAR(128) NOT NULL,
    author_name VARCHAR(128) NOT NULL,
    class_names VARCHAR(255) NOT NULL,
    order_quantity INT NOT NULL,
    selection_reason VARCHAR(500) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_textbook_workflow (workflow_application_id),
    KEY idx_biz_textbook_teacher (teacher_id)
);
ALTER TABLE biz_textbook_order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_goods_borrow_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    applicant_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    item_spec VARCHAR(200) NOT NULL,
    borrow_quantity INT NOT NULL,
    borrow_start_time DATETIME NOT NULL,
    borrow_end_time DATETIME NOT NULL,
    borrow_purpose VARCHAR(500) NOT NULL,
    return_plan VARCHAR(500) NOT NULL,
    contact_name VARCHAR(128) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    remarks VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_goods_borrow_workflow (workflow_application_id),
    KEY idx_biz_goods_borrow_applicant (applicant_id)
);
ALTER TABLE biz_goods_borrow_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_course_suspension_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    course_code VARCHAR(64) NOT NULL,
    suspension_date DATE NOT NULL,
    suspension_start_time DATETIME NOT NULL,
    suspension_end_time DATETIME NOT NULL,
    suspension_reason VARCHAR(500) NOT NULL,
    makeup_suggestion VARCHAR(500) NOT NULL,
    affected_class VARCHAR(255) NOT NULL,
    remarks VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_course_suspension_workflow (workflow_application_id),
    KEY idx_biz_course_suspension_teacher (teacher_id)
);
ALTER TABLE biz_course_suspension_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_makeup_class_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    course_code VARCHAR(64) NOT NULL,
    makeup_date DATE NOT NULL,
    makeup_start_time DATETIME NOT NULL,
    makeup_end_time DATETIME NOT NULL,
    makeup_location VARCHAR(128) NOT NULL,
    related_suspension_no VARCHAR(64) NOT NULL,
    makeup_reason VARCHAR(500) NOT NULL,
    affected_class VARCHAR(255) NOT NULL,
    notice_plan VARCHAR(500) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_makeup_class_workflow (workflow_application_id),
    KEY idx_biz_makeup_class_teacher (teacher_id)
);
ALTER TABLE biz_makeup_class_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_research_midterm_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    project_no VARCHAR(64) NOT NULL,
    progress_rate VARCHAR(32) NOT NULL,
    stage_outcome VARCHAR(500) NOT NULL,
    existing_problems VARCHAR(500) NOT NULL,
    correction_plan VARCHAR(500) NOT NULL,
    budget_usage VARCHAR(500) NOT NULL,
    remarks VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_research_midterm_workflow (workflow_application_id),
    KEY idx_biz_research_midterm_teacher (teacher_id)
);
ALTER TABLE biz_research_midterm_check CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_research_completion_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    project_no VARCHAR(64) NOT NULL,
    achievements VARCHAR(1000) NOT NULL,
    funding_usage VARCHAR(500) NOT NULL,
    completion_report VARCHAR(1000) NOT NULL,
    expert_list VARCHAR(500) DEFAULT NULL,
    conclusion_note VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_research_completion_workflow (workflow_application_id),
    KEY idx_biz_research_completion_teacher (teacher_id)
);
ALTER TABLE biz_research_completion_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_lesson_plan_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    semester_code VARCHAR(32) NOT NULL,
    chapter_range VARCHAR(255) NOT NULL,
    version_no VARCHAR(64) NOT NULL,
    lesson_plan_title VARCHAR(200) NOT NULL,
    lesson_plan_summary VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_lesson_plan_workflow (workflow_application_id),
    KEY idx_biz_lesson_plan_teacher (teacher_id)
);
ALTER TABLE biz_lesson_plan_submission CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_teaching_outline_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    academic_year VARCHAR(32) NOT NULL,
    target_major VARCHAR(128) NOT NULL,
    version_no VARCHAR(64) NOT NULL,
    revision_note VARCHAR(500) DEFAULT NULL,
    outline_summary VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_outline_workflow (workflow_application_id),
    KEY idx_biz_outline_teacher (teacher_id)
);
ALTER TABLE biz_teaching_outline_submission CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_grade_correction_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    teacher_id BIGINT NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    course_code VARCHAR(64) NOT NULL,
    student_no VARCHAR(64) NOT NULL,
    student_name VARCHAR(128) NOT NULL,
    original_grade VARCHAR(32) NOT NULL,
    new_grade VARCHAR(32) NOT NULL,
    correction_reason VARCHAR(500) NOT NULL,
    proof_materials VARCHAR(500) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_grade_correction_workflow (workflow_application_id),
    KEY idx_biz_grade_correction_teacher (teacher_id)
);
ALTER TABLE biz_grade_correction_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_exam_schedule_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    applicant_id BIGINT NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    exam_count INT NOT NULL,
    exam_time_suggestion VARCHAR(128) NOT NULL,
    classroom_need VARCHAR(255) NOT NULL,
    invigilator_need VARCHAR(255) DEFAULT NULL,
    remarks VARCHAR(500) DEFAULT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_exam_schedule_workflow (workflow_application_id),
    KEY idx_biz_exam_schedule_applicant (applicant_id)
);
ALTER TABLE biz_exam_schedule_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS biz_classroom_borrow_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_application_id BIGINT DEFAULT NULL,
    applicant_id BIGINT NOT NULL,
    classroom_name VARCHAR(128) NOT NULL,
    borrow_date DATE NOT NULL,
    borrow_start_time DATETIME NOT NULL,
    borrow_end_time DATETIME NOT NULL,
    borrow_purpose VARCHAR(500) NOT NULL,
    attendee_count INT NOT NULL,
    equipment_needs VARCHAR(500) DEFAULT NULL,
    contact_name VARCHAR(128) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    business_status VARCHAR(32) NOT NULL,
    flow_status VARCHAR(32) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_classroom_borrow_workflow (workflow_application_id),
    KEY idx_biz_classroom_borrow_applicant (applicant_id)
);
ALTER TABLE biz_classroom_borrow_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_academic_lecture_application CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_office_supply_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_lab_safety_hazard_report CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_dorm_adjustment_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_stamp_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_vehicle_request CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_class_notice_receipt CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_student_warning_process CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_material_supplement CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
ALTER TABLE biz_announcement_publish CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT IGNORE INTO sys_role (id, role_code, role_name, status) VALUES
    (1, 'ADMIN', '系统管理员', 1),
    (2, 'STUDENT', '学生', 1),
    (3, 'TEACHER', '教师', 1),
    (4, 'ADVISER', '班主任', 1),
    (5, 'RESEARCH', '科技处', 1),
    (6, 'OFFICE', '教务处', 1),
    (7, 'REVIEWER', '评审专家', 1),
    (8, 'STUDENT_AFFAIRS', '学工处', 1);

INSERT IGNORE INTO sys_user (id, org_id, username, password_hash, real_name, user_type, status) VALUES
    (1, NULL, 'admin', '123456', '系统管理员', 'ADMIN', 1),
    (2, NULL, 'student', '123456', '学生用户', 'STUDENT', 1),
    (3, NULL, 'teacher', '123456', '教师用户', 'TEACHER', 1),
    (4, NULL, 'adviser', '123456', '班主任用户', 'ADVISER', 1),
    (5, NULL, 'research', '123456', '科技处用户', 'RESEARCH', 1),
    (6, NULL, 'office', '123456', '教务处用户', 'OFFICE', 1),
    (7, NULL, 'reviewer', '123456', '评审专家用户', 'REVIEWER', 1),
    (8, NULL, 'affairs', '123456', '学工处用户', 'STUDENT_AFFAIRS', 1);

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5),
    (6, 6),
    (7, 7),
    (8, 8);

INSERT IGNORE INTO sys_org (id, parent_id, org_code, org_name, org_type, sort_no, status) VALUES
    (1, NULL, 'SCHOOL', '智慧校园示范学校', 'SCHOOL', 1, 1),
    (2, 1, 'COLLEGE-INFO', '信息工程学院', 'COLLEGE', 1, 1),
    (3, 2, 'DEPT-SOFTWARE', '软件工程系', 'DEPARTMENT', 1, 1),
    (4, 3, 'CLASS-2024-SE-1', '2024级软件工程1班', 'CLASS', 1, 1),
    (5, 1, 'ADMIN-OFFICE', '教务与职能部门', 'DEPARTMENT', 2, 1);

INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, permission_code, sort_no, status) VALUES
    (1, NULL, '工作台', 'MENU', '/', 'dashboard', 1, 1),
    (2, NULL, '通用审批', 'MENU', '/workflow/applications', 'workflow', 2, 1),
    (3, NULL, '学生事务', 'MENU', '/modules/student-affairs', 'student-affairs', 3, 1),
    (4, NULL, '教学事务', 'MENU', '/modules/academic', 'academic', 4, 1),
    (5, NULL, '科研管理', 'MENU', '/modules/research', 'research', 5, 1),
    (6, NULL, '后勤管理', 'MENU', '/modules/logistics', 'logistics', 6, 1),
    (7, NULL, '系统管理', 'MENU', '/modules/system', 'system', 7, 1),
    (101, 7, '用户管理', 'MENU', '/system/users', 'system:user:view', 1, 1),
    (102, 7, '组织管理', 'MENU', '/system/orgs', 'system:org:view', 2, 1),
    (103, 7, '角色权限管理', 'MENU', '/system/roles', 'system:role:view', 3, 1),
    (104, 7, '附件中心', 'MENU', '/system/files', 'system:file:view', 4, 1),
    (105, 7, '流程模板管理', 'MENU', '/system/workflows', 'system:workflow:view', 5, 1);

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

INSERT IGNORE INTO wf_application_type (id, type_code, type_name, description_text, approver_role_code, status) VALUES
    (1, 'GENERAL_STUDENT', '通用学生申请', '面向学生日常事务的通用申请单。', 'ADVISER', 1),
    (2, 'GENERAL_ACADEMIC', '通用教学申请', '面向教学教务事项的通用申请单。', 'OFFICE', 1),
    (3, 'GENERAL_RESEARCH', '通用科研申请', '面向科研管理事项的通用申请单。', 'RESEARCH', 1),
    (4, 'GENERAL_LOGISTICS', '通用后勤申请', '面向后勤保障事项的通用申请单。', 'ADMIN', 1),
    (11, 'INTERNSHIP_MATERIAL', '实习协议材料', '学生提交实习协议材料。', 'ADVISER', 1),
    (12, 'ABNORMAL_STUDENT_CASE', '异常学生', '班主任登记异常学生信息。', 'ADMIN', 1),
    (13, 'RESEARCH_PROJECT_REVIEW', '课题申报', '教师提交课题申报。', 'RESEARCH', 1),
    (14, 'COURSE_STANDARD_REVIEW', '课程标准', '教师提交课程标准。', 'OFFICE', 1),
    (15, 'LEAVE_APPLICATION', '请假申请', '学生提交请假申请。', 'ADVISER', 1),
    (16, 'LEAVE_CANCELLATION', '销假申请', '学生返校后提交销假。', 'ADVISER', 1),
    (17, 'SCHEDULE_ADJUSTMENT', '调课申请', '教师提交调课申请。', 'OFFICE', 1),
    (18, 'MEETING_ROOM_BOOKING', '会议室预约', '校内人员预约会议室。', 'ADMIN', 1),
    (19, 'DORM_REPAIR', '宿舍维修', '学生提交宿舍维修申请。', 'ADMIN', 1),
    (20, 'ASSET_REPAIR', '资产报修', '教师提交资产报修申请。', 'ADMIN', 1),
    (21, 'SCHOLARSHIP_APPLICATION', '奖学金申请', '学生提交奖学金申请。', 'STUDENT_AFFAIRS', 1),
    (22, 'GRANT_APPLICATION', '助学金申请', '学生提交助学金申请。', 'STUDENT_AFFAIRS', 1),
    (23, 'DIFFICULTY_RECOGNITION', '困难认定', '学生提交困难认定申请。', 'STUDENT_AFFAIRS', 1),
    (24, 'ENROLLMENT_CERTIFICATE', '在读证明', '学生申请在读证明。', 'ADVISER', 1),
    (25, 'TEXTBOOK_ORDER', '教材征订', '教师提交教材征订申请。', 'OFFICE', 1),
    (26, 'GOODS_BORROW_APPLICATION', '物资借用', '校内人员借用物资。', 'ADMIN', 1),
    (27, 'COURSE_SUSPENSION_APPLICATION', '停课申请', '教师提交停课申请。', 'OFFICE', 1),
    (28, 'MAKEUP_CLASS_APPLICATION', '补课申请', '教师提交补课申请。', 'OFFICE', 1),
    (29, 'RESEARCH_MIDTERM_CHECK', '课题中期检查', '教师提交课题中期检查。', 'RESEARCH', 1),
    (30, 'RESEARCH_COMPLETION_APPLICATION', '课题结题申请', '教师提交课题结题申请。', 'RESEARCH', 1),
    (31, 'LESSON_PLAN_SUBMISSION', '教案提交', '教师提交教案。', 'OFFICE', 1),
    (32, 'TEACHING_OUTLINE_SUBMISSION', '教学大纲提交', '教师提交教学大纲。', 'OFFICE', 1),
    (33, 'GRADE_CORRECTION_REQUEST', '成绩更正申请', '教师提交成绩更正申请。', 'OFFICE', 1),
    (34, 'EXAM_SCHEDULE_APPLICATION', '考试安排申请', '教师提交考试安排申请。', 'OFFICE', 1),
    (35, 'CLASSROOM_BORROW_APPLICATION', '教室借用申请', '教师提交教室借用申请。', 'OFFICE', 1),
    (36, 'STUDENT_LEAVE_APPLICATION', '学生离校申请', '学生提交离校申请。', 'ADVISER', 1),
    (37, 'STUDENT_RETURN_CONFIRMATION', '学生返校确认', '学生提交返校确认。', 'ADVISER', 1),
    (38, 'GRADUATION_PROJECT_OPENING', '毕业设计开题申请', '学生提交毕业设计开题申请。', 'TEACHER', 1),
    (39, 'GRADUATION_PROJECT_MIDTERM', '毕业设计中期检查', '学生提交毕业设计中期检查。', 'TEACHER', 1),
    (40, 'RESEARCH_ACHIEVEMENT_REGISTRATION', '科研成果登记', '教师提交科研成果登记。', 'RESEARCH', 1),
    (41, 'ACADEMIC_LECTURE_APPLICATION', '学术讲座申请', '教师提交学术讲座申请。', 'RESEARCH', 1),
    (42, 'OFFICE_SUPPLY_REQUEST', '办公用品申领', '教师提交办公用品申领。', 'ADMIN', 1),
    (43, 'LAB_SAFETY_HAZARD_REPORT', '实验室安全隐患上报', '教师提交实验室安全隐患上报。', 'ADMIN', 1),
    (44, 'DORM_ADJUSTMENT_REQUEST', '宿舍调宿申请', '学生提交宿舍调宿申请。', 'ADVISER', 1),
    (45, 'STAMP_REQUEST', '用章申请', '教师提交用章申请。', 'ADMIN', 1),
    (46, 'VEHICLE_REQUEST', '车辆申请', '教师提交公务用车申请。', 'ADMIN', 1),
    (47, 'CLASS_NOTICE_RECEIPT', '班级通知回执', '班主任发布班级通知并统计回执。', 'ADVISER', 1),
    (48, 'STUDENT_WARNING_PROCESS', '学生预警处理', '班主任登记并处理学生预警。', 'STUDENT_AFFAIRS', 1),
    (49, 'MATERIAL_SUPPLEMENT', '证明材料补交', '学生补交被退回或缺失的证明材料。', 'ADVISER', 1),
    (50, 'ANNOUNCEMENT_PUBLISH', '公告发布', '管理员或部门人员提交公告发布申请。', 'ADMIN', 1);

INSERT IGNORE INTO wf_definition (id, business_type, definition_code, definition_name, version_no, status) VALUES
    (1, 'INTERNSHIP_MATERIAL', 'WF_INTERNSHIP_MATERIAL', '学生实习材料流程', 1, 1),
    (2, 'ABNORMAL_STUDENT_CASE', 'WF_ABNORMAL_STUDENT_CASE', '异常学生材料流程', 1, 1),
    (3, 'RESEARCH_PROJECT_REVIEW', 'WF_RESEARCH_PROJECT_REVIEW', '课题申报流程', 1, 1),
    (4, 'COURSE_STANDARD_REVIEW', 'WF_COURSE_STANDARD_REVIEW', '课程标准评审流程', 1, 1),
    (5, 'LEAVE_APPLICATION', 'WF_LEAVE_APPLICATION', '学生请假流程', 1, 1),
    (6, 'LEAVE_CANCELLATION', 'WF_LEAVE_CANCELLATION', '学生销假流程', 1, 1),
    (7, 'SCHEDULE_ADJUSTMENT', 'WF_SCHEDULE_ADJUSTMENT', '教师调课流程', 1, 1),
    (8, 'MEETING_ROOM_BOOKING', 'WF_MEETING_ROOM_BOOKING', '会议室申请流程', 1, 1),
    (9, 'DORM_REPAIR', 'WF_DORM_REPAIR', '宿舍维修流程', 1, 1),
    (10, 'ASSET_REPAIR', 'WF_ASSET_REPAIR', '资产报修流程', 1, 1),
    (11, 'SCHOLARSHIP_APPLICATION', 'WF_SCHOLARSHIP_APPLICATION', '奖学金申请流程', 1, 1),
    (12, 'GRANT_APPLICATION', 'WF_GRANT_APPLICATION', '助学金申请流程', 1, 1),
    (13, 'DIFFICULTY_RECOGNITION', 'WF_DIFFICULTY_RECOGNITION', '困难认定流程', 1, 1),
    (14, 'ENROLLMENT_CERTIFICATE', 'WF_ENROLLMENT_CERTIFICATE', '在读证明流程', 1, 1),
    (15, 'TEXTBOOK_ORDER', 'WF_TEXTBOOK_ORDER', '教材征订流程', 1, 1),
    (16, 'GOODS_BORROW_APPLICATION', 'WF_GOODS_BORROW_APPLICATION', '物资借用申请流程', 1, 1),
    (17, 'COURSE_SUSPENSION_APPLICATION', 'WF_COURSE_SUSPENSION_APPLICATION', '停课申请流程', 1, 1),
    (18, 'MAKEUP_CLASS_APPLICATION', 'WF_MAKEUP_CLASS_APPLICATION', '补课申请流程', 1, 1),
    (19, 'RESEARCH_MIDTERM_CHECK', 'WF_RESEARCH_MIDTERM_CHECK', '课题中期检查流程', 1, 1),
    (20, 'RESEARCH_COMPLETION_APPLICATION', 'WF_RESEARCH_COMPLETION_APPLICATION', '课题结题申请流程', 1, 1),
    (21, 'LESSON_PLAN_SUBMISSION', 'WF_LESSON_PLAN_SUBMISSION', '教案提交流程', 1, 1),
    (22, 'TEACHING_OUTLINE_SUBMISSION', 'WF_TEACHING_OUTLINE_SUBMISSION', '教学大纲提交流程', 1, 1),
    (23, 'GRADE_CORRECTION_REQUEST', 'WF_GRADE_CORRECTION_REQUEST', '成绩更正申请流程', 1, 1),
    (24, 'EXAM_SCHEDULE_APPLICATION', 'WF_EXAM_SCHEDULE_APPLICATION', '考试安排申请流程', 1, 1),
    (25, 'CLASSROOM_BORROW_APPLICATION', 'WF_CLASSROOM_BORROW_APPLICATION', '教室借用申请流程', 1, 1),
    (26, 'STUDENT_LEAVE_APPLICATION', 'WF_STUDENT_LEAVE_APPLICATION', '学生离校申请流程', 1, 1),
    (27, 'STUDENT_RETURN_CONFIRMATION', 'WF_STUDENT_RETURN_CONFIRMATION', '学生返校确认流程', 1, 1),
    (28, 'GRADUATION_PROJECT_OPENING', 'WF_GRADUATION_PROJECT_OPENING', '毕业设计开题流程', 1, 1),
    (29, 'GRADUATION_PROJECT_MIDTERM', 'WF_GRADUATION_PROJECT_MIDTERM', '毕业设计中期检查流程', 1, 1),
    (30, 'RESEARCH_ACHIEVEMENT_REGISTRATION', 'WF_RESEARCH_ACHIEVEMENT_REGISTRATION', '科研成果登记流程', 1, 1),
    (31, 'ACADEMIC_LECTURE_APPLICATION', 'WF_ACADEMIC_LECTURE_APPLICATION', '学术讲座申请流程', 1, 1),
    (32, 'OFFICE_SUPPLY_REQUEST', 'WF_OFFICE_SUPPLY_REQUEST', '办公用品申领流程', 1, 1),
    (33, 'LAB_SAFETY_HAZARD_REPORT', 'WF_LAB_SAFETY_HAZARD_REPORT', '实验室安全隐患上报流程', 1, 1),
    (34, 'DORM_ADJUSTMENT_REQUEST', 'WF_DORM_ADJUSTMENT_REQUEST', '宿舍调宿申请流程', 1, 1),
    (35, 'STAMP_REQUEST', 'WF_STAMP_REQUEST', '用章申请流程', 1, 1),
    (36, 'VEHICLE_REQUEST', 'WF_VEHICLE_REQUEST', '车辆申请流程', 1, 1),
    (37, 'CLASS_NOTICE_RECEIPT', 'WF_CLASS_NOTICE_RECEIPT', '班级通知回执流程', 1, 1),
    (38, 'STUDENT_WARNING_PROCESS', 'WF_STUDENT_WARNING_PROCESS', '学生预警处理流程', 1, 1),
    (39, 'MATERIAL_SUPPLEMENT', 'WF_MATERIAL_SUPPLEMENT', '证明材料补交流程', 1, 1),
    (40, 'ANNOUNCEMENT_PUBLISH', 'WF_ANNOUNCEMENT_PUBLISH', '公告发布流程', 1, 1);

INSERT IGNORE INTO wf_node_definition (
    id, definition_id, node_code, node_name, node_type, approver_type, approver_role_code, sort_no, status
) VALUES
    (1001, 1, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1002, 2, 'APPROVE_1', '管理员复核', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1003, 3, 'APPROVE_1', '科技处审批', 'APPROVAL', 'ROLE', 'RESEARCH', 1, 1),
    (1004, 4, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1005, 5, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1006, 6, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1007, 7, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1008, 8, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1009, 9, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1010, 10, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1011, 11, 'APPROVE_1', '学工处审批', 'APPROVAL', 'ROLE', 'STUDENT_AFFAIRS', 1, 1),
    (1012, 12, 'APPROVE_1', '学工处审批', 'APPROVAL', 'ROLE', 'STUDENT_AFFAIRS', 1, 1),
    (1013, 13, 'APPROVE_1', '学工处审批', 'APPROVAL', 'ROLE', 'STUDENT_AFFAIRS', 1, 1),
    (1014, 14, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1015, 15, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1016, 16, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1017, 17, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1018, 18, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1019, 19, 'APPROVE_1', '科技处审批', 'APPROVAL', 'ROLE', 'RESEARCH', 1, 1),
    (1020, 20, 'APPROVE_1', '科技处审批', 'APPROVAL', 'ROLE', 'RESEARCH', 1, 1),
    (1021, 21, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1022, 22, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1023, 23, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1024, 24, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1025, 25, 'APPROVE_1', '教务处审批', 'APPROVAL', 'ROLE', 'OFFICE', 1, 1),
    (1026, 26, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1027, 27, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1028, 28, 'APPROVE_1', '指导教师审批', 'APPROVAL', 'ROLE', 'TEACHER', 1, 1),
    (1029, 29, 'APPROVE_1', '指导教师审批', 'APPROVAL', 'ROLE', 'TEACHER', 1, 1),
    (1030, 30, 'APPROVE_1', '科技处审批', 'APPROVAL', 'ROLE', 'RESEARCH', 1, 1),
    (1031, 31, 'APPROVE_1', '科技处审批', 'APPROVAL', 'ROLE', 'RESEARCH', 1, 1),
    (1032, 32, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1033, 33, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1034, 34, 'APPROVE_1', '班主任审批', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1035, 35, 'APPROVE_1', '管理员审批', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1036, 36, 'APPROVE_1', '管理员排车', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1),
    (1037, 37, 'APPROVE_1', '班主任确认发布', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1038, 38, 'APPROVE_1', '学工处复核', 'APPROVAL', 'ROLE', 'STUDENT_AFFAIRS', 1, 1),
    (1039, 39, 'APPROVE_1', '班主任复核', 'APPROVAL', 'ROLE', 'ADVISER', 1, 1),
    (1040, 40, 'APPROVE_1', '管理员审核发布', 'APPROVAL', 'ROLE', 'ADMIN', 1, 1);
