package com.campusoa.workflow.service;

import com.campusoa.security.AuthenticatedUser;
import com.campusoa.workflow.dto.ApplicationTypeDto;
import com.campusoa.workflow.dto.ApprovalRecordDto;
import com.campusoa.workflow.dto.CreateApplicationRequest;
import com.campusoa.workflow.dto.WorkflowApplicationDetail;
import com.campusoa.workflow.dto.WorkflowApplicationSummary;
import com.campusoa.workflow.exception.WorkflowException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class WorkflowService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";

    private final JdbcTemplate jdbcTemplate;

    public WorkflowService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ApplicationTypeDto> listTypes() {
        return jdbcTemplate.query("""
                        SELECT id, type_code, type_name, description_text, approver_role_code
                        FROM wf_application_type
                        WHERE status = 1
                        ORDER BY id
                        """,
                (rs, rowNum) -> new ApplicationTypeDto(
                        rs.getLong("id"),
                        rs.getString("type_code"),
                        rs.getString("type_name"),
                        rs.getString("description_text"),
                        rs.getString("approver_role_code")
                )
        );
    }

    @Transactional
    public Long createDraft(AuthenticatedUser currentUser, CreateApplicationRequest request) {
        ApplicationType type = findType(request.typeId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO wf_application (
                                application_no, type_id, title, content_text, applicant_id, status
                            ) VALUES (?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, generateApplicationNo());
            statement.setLong(2, type.id());
            statement.setString(3, request.title().trim());
            statement.setString(4, request.content().trim());
            statement.setLong(5, currentUser.userId());
            statement.setString(6, STATUS_DRAFT);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new WorkflowException("创建申请失败");
        }
        return key.longValue();
    }

    @Transactional
    public void submit(AuthenticatedUser currentUser, Long applicationId) {
        ApplicationRecord application = findApplication(applicationId);
        ensureApplicant(currentUser, application);
        ensureStatus(application.status(), STATUS_DRAFT);

        ApplicationType type = findType(application.typeId());
        Approver approver = resolveApprover(application, type);
        if (approver == null) {
            throw new WorkflowException("当前申请类型未配置可用审批人");
        }

        jdbcTemplate.update("""
                        UPDATE wf_application
                        SET current_approver_id = ?, status = ?, submitted_at = NOW(), updated_at = NOW()
                        WHERE id = ?
                        """,
                approver.userId(),
                STATUS_PENDING,
                applicationId
        );
        syncBusinessStatus(applicationId, STATUS_PENDING);
        insertRecord(applicationId, "SUBMIT", currentUser.userId(), "提交申请");
    }

    public List<WorkflowApplicationSummary> listMyApplications(AuthenticatedUser currentUser) {
        return jdbcTemplate.query("""
                        SELECT a.id,
                               a.application_no,
                               a.type_id,
                               t.type_name,
                               a.title,
                               a.status,
                               applicant.real_name AS applicant_name,
                               approver.real_name AS current_approver_name,
                               a.submitted_at,
                               a.updated_at
                        FROM wf_application a
                        JOIN wf_application_type t ON t.id = a.type_id
                        JOIN sys_user applicant ON applicant.id = a.applicant_id
                        LEFT JOIN sys_user approver ON approver.id = a.current_approver_id
                        WHERE a.applicant_id = ?
                        ORDER BY a.updated_at DESC, a.id DESC
                        """,
                (rs, rowNum) -> new WorkflowApplicationSummary(
                        rs.getLong("id"),
                        rs.getString("application_no"),
                        rs.getLong("type_id"),
                        rs.getString("type_name"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("applicant_name"),
                        rs.getString("current_approver_name"),
                        getDateTime(rs, "submitted_at"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                currentUser.userId()
        );
    }

    public List<WorkflowApplicationSummary> listTodos(AuthenticatedUser currentUser) {
        return jdbcTemplate.query("""
                        SELECT a.id,
                               a.application_no,
                               a.type_id,
                               t.type_name,
                               a.title,
                               a.status,
                               applicant.real_name AS applicant_name,
                               approver.real_name AS current_approver_name,
                               a.submitted_at,
                               a.updated_at
                        FROM wf_application a
                        JOIN wf_application_type t ON t.id = a.type_id
                        JOIN sys_user applicant ON applicant.id = a.applicant_id
                        LEFT JOIN sys_user approver ON approver.id = a.current_approver_id
                        WHERE a.current_approver_id = ?
                          AND a.status IN (?, ?)
                        ORDER BY a.updated_at DESC, a.id DESC
                        """,
                (rs, rowNum) -> new WorkflowApplicationSummary(
                        rs.getLong("id"),
                        rs.getString("application_no"),
                        rs.getLong("type_id"),
                        rs.getString("type_name"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("applicant_name"),
                        rs.getString("current_approver_name"),
                        getDateTime(rs, "submitted_at"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                currentUser.userId(),
                STATUS_PENDING,
                STATUS_IN_PROGRESS
        );
    }

    public WorkflowApplicationDetail getDetail(AuthenticatedUser currentUser, Long applicationId) {
        ApplicationRecord application = findApplication(applicationId);
        ensureReadable(currentUser, application);

        List<ApprovalRecordDto> records = jdbcTemplate.query("""
                        SELECT r.id,
                               r.action_type,
                               u.real_name AS actor_name,
                               r.comment_text,
                               r.created_at
                        FROM wf_approval_record r
                        JOIN sys_user u ON u.id = r.actor_id
                        WHERE r.application_id = ?
                        ORDER BY r.id ASC
                        """,
                (rs, rowNum) -> new ApprovalRecordDto(
                        rs.getLong("id"),
                        rs.getString("action_type"),
                        rs.getString("actor_name"),
                        rs.getString("comment_text"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ),
                applicationId
        );

        return new WorkflowApplicationDetail(
                application.id(),
                application.applicationNo(),
                application.typeId(),
                application.typeName(),
                application.title(),
                application.content(),
                application.status(),
                application.applicantId(),
                application.applicantName(),
                application.currentApproverId(),
                application.currentApproverName(),
                application.submittedAt(),
                application.finishedAt(),
                application.createdAt(),
                application.updatedAt(),
                canSubmit(currentUser, application),
                canWithdraw(currentUser, application),
                canApprove(currentUser, application),
                canReject(currentUser, application),
                records
        );
    }

    public void ensureReadable(AuthenticatedUser currentUser, Long applicationId) {
        ensureReadable(currentUser, findApplication(applicationId));
    }

    public boolean canManageAttachments(AuthenticatedUser currentUser, Long applicationId) {
        ApplicationRecord application = findApplication(applicationId);
        ensureReadable(currentUser, application);
        return isAdmin(currentUser)
                || Objects.equals(currentUser.userId(), application.applicantId())
                || Objects.equals(currentUser.userId(), application.currentApproverId());
    }

    @Transactional
    public void approve(AuthenticatedUser currentUser, Long applicationId, String comment) {
        ApplicationRecord application = findApplication(applicationId);
        ensureApprover(currentUser, application);
        ensureStatus(application.status(), STATUS_PENDING, STATUS_IN_PROGRESS);

        jdbcTemplate.update("""
                        UPDATE wf_application
                        SET status = ?, finished_at = NOW(), current_approver_id = NULL, updated_at = NOW()
                        WHERE id = ?
                        """,
                STATUS_APPROVED,
                applicationId
        );
        syncBusinessStatus(applicationId, STATUS_APPROVED);
        insertRecord(applicationId, "APPROVE", currentUser.userId(), normalizeComment(comment, "审批通过"));
    }

    @Transactional
    public void reject(AuthenticatedUser currentUser, Long applicationId, String comment) {
        ApplicationRecord application = findApplication(applicationId);
        ensureApprover(currentUser, application);
        ensureStatus(application.status(), STATUS_PENDING, STATUS_IN_PROGRESS);

        jdbcTemplate.update("""
                        UPDATE wf_application
                        SET status = ?, finished_at = NOW(), current_approver_id = NULL, updated_at = NOW()
                        WHERE id = ?
                        """,
                STATUS_REJECTED,
                applicationId
        );
        syncBusinessStatus(applicationId, STATUS_REJECTED);
        insertRecord(applicationId, "REJECT", currentUser.userId(), normalizeComment(comment, "审批驳回"));
    }

    @Transactional
    public void withdraw(AuthenticatedUser currentUser, Long applicationId) {
        ApplicationRecord application = findApplication(applicationId);
        ensureApplicant(currentUser, application);
        ensureStatus(application.status(), STATUS_PENDING, STATUS_IN_PROGRESS);

        jdbcTemplate.update("""
                        UPDATE wf_application
                        SET status = ?, finished_at = NOW(), current_approver_id = NULL, updated_at = NOW()
                        WHERE id = ?
                        """,
                STATUS_WITHDRAWN,
                applicationId
        );
        syncBusinessStatus(applicationId, STATUS_WITHDRAWN);
        insertRecord(applicationId, "WITHDRAW", currentUser.userId(), "申请撤回");
    }

    private ApplicationType findType(Long typeId) {
        List<ApplicationType> types = jdbcTemplate.query("""
                        SELECT id, type_code, type_name, description_text, approver_role_code, status
                        FROM wf_application_type
                        WHERE id = ?
                        """,
                (rs, rowNum) -> new ApplicationType(
                        rs.getLong("id"),
                        rs.getString("type_code"),
                        rs.getString("type_name"),
                        rs.getString("description_text"),
                        rs.getString("approver_role_code"),
                        rs.getInt("status")
                ),
                typeId
        );
        ApplicationType type = types.isEmpty() ? null : types.get(0);
        if (type == null || type.status() != 1) {
            throw new WorkflowException("申请类型不存在或已停用");
        }
        return type;
    }

    private ApplicationRecord findApplication(Long applicationId) {
        List<ApplicationRecord> applications = jdbcTemplate.query("""
                        SELECT a.id,
                               a.application_no,
                               a.type_id,
                               t.type_code,
                               t.type_name,
                               a.title,
                               a.content_text,
                               a.applicant_id,
                               applicant.real_name AS applicant_name,
                               a.current_approver_id,
                               approver.real_name AS current_approver_name,
                               a.status,
                               a.submitted_at,
                               a.finished_at,
                               a.created_at,
                               a.updated_at
                        FROM wf_application a
                        JOIN wf_application_type t ON t.id = a.type_id
                        JOIN sys_user applicant ON applicant.id = a.applicant_id
                        LEFT JOIN sys_user approver ON approver.id = a.current_approver_id
                        WHERE a.id = ?
                        """,
                (rs, rowNum) -> new ApplicationRecord(
                        rs.getLong("id"),
                        rs.getString("application_no"),
                        rs.getLong("type_id"),
                        rs.getString("type_code"),
                        rs.getString("type_name"),
                        rs.getString("title"),
                        rs.getString("content_text"),
                        rs.getLong("applicant_id"),
                        rs.getString("applicant_name"),
                        getLong(rs, "current_approver_id"),
                        rs.getString("current_approver_name"),
                        rs.getString("status"),
                        getDateTime(rs, "submitted_at"),
                        getDateTime(rs, "finished_at"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                applicationId
        );
        if (applications.isEmpty()) {
            throw new WorkflowException("申请单不存在");
        }
        return applications.get(0);
    }

    private Approver findApprover(String approverRoleCode) {
        List<Approver> approvers = jdbcTemplate.query("""
                        SELECT u.id, u.real_name
                        FROM sys_user u
                        JOIN sys_user_role ur ON ur.user_id = u.id
                        JOIN sys_role r ON r.id = ur.role_id
                        WHERE r.role_code = ?
                          AND r.status = 1
                          AND u.status = 1
                        ORDER BY u.id
                        LIMIT 1
                        """,
                (rs, rowNum) -> new Approver(
                        rs.getLong("id"),
                        rs.getString("real_name")
                ),
                approverRoleCode
        );
        return approvers.isEmpty() ? null : approvers.get(0);
    }

    private Approver resolveApprover(ApplicationRecord application, ApplicationType type) {
        return switch (type.typeCode()) {
            case "STUDENT_RETURN_CONFIRMATION" -> {
                Approver approver = findRelatedLeaveApprover(application.id());
                yield approver != null ? approver : findApprover(resolveApproverRoleCode(type));
            }
            case "GRADUATION_PROJECT_OPENING" -> {
                Approver approver = findOpeningAdvisorApprover(application.id());
                if (approver == null) {
                    throw new WorkflowException("未找到毕业设计开题的指导老师审批人");
                }
                yield approver;
            }
            case "GRADUATION_PROJECT_MIDTERM" -> {
                Approver approver = findMidtermAdvisorApprover(application.id());
                if (approver == null) {
                    throw new WorkflowException("未找到毕业设计中期检查的指导老师审批人");
                }
                yield approver;
            }
            default -> findApprover(resolveApproverRoleCode(type));
        };
    }

    private Approver findRelatedLeaveApprover(Long returnApplicationId) {
        List<Long> leaveApplicationIds = jdbcTemplate.query("""
                        SELECT leave_wf.id
                        FROM biz_student_return_confirmation rc
                        JOIN biz_student_leave_application sl
                          ON sl.student_id = rc.student_id
                        JOIN wf_application leave_wf
                          ON leave_wf.id = sl.workflow_application_id
                        WHERE rc.workflow_application_id = ?
                          AND leave_wf.application_no = rc.related_leave_no
                        ORDER BY sl.id DESC
                        LIMIT 1
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                returnApplicationId
        );
        if (leaveApplicationIds.isEmpty()) {
            return null;
        }

        List<Approver> approvers = jdbcTemplate.query("""
                        SELECT u.id, u.real_name
                        FROM wf_approval_record r
                        JOIN sys_user u ON u.id = r.actor_id
                        WHERE r.application_id = ?
                          AND r.action_type = 'APPROVE'
                        ORDER BY r.id DESC
                        LIMIT 1
                        """,
                (rs, rowNum) -> new Approver(rs.getLong("id"), rs.getString("real_name")),
                leaveApplicationIds.get(0)
        );
        return approvers.isEmpty() ? null : approvers.get(0);
    }

    private Approver findOpeningAdvisorApprover(Long openingApplicationId) {
        List<String> advisorNames = jdbcTemplate.query("""
                        SELECT advisor_name
                        FROM biz_graduation_project_opening
                        WHERE workflow_application_id = ?
                        """,
                (rs, rowNum) -> rs.getString("advisor_name"),
                openingApplicationId
        );
        if (advisorNames.isEmpty()) {
            return null;
        }
        return findTeacherApproverByRealName(advisorNames.get(0));
    }

    private Approver findMidtermAdvisorApprover(Long midtermApplicationId) {
        List<String> advisorNames = jdbcTemplate.query("""
                        SELECT opening.advisor_name
                        FROM biz_graduation_project_midterm midterm
                        JOIN biz_graduation_project_opening opening
                          ON opening.student_id = midterm.student_id
                         AND opening.project_name = midterm.project_name
                        JOIN wf_application opening_wf
                          ON opening_wf.id = opening.workflow_application_id
                        WHERE midterm.workflow_application_id = ?
                          AND opening_wf.status = 'APPROVED'
                        ORDER BY opening.id DESC
                        LIMIT 1
                        """,
                (rs, rowNum) -> rs.getString("advisor_name"),
                midtermApplicationId
        );
        if (advisorNames.isEmpty()) {
            return null;
        }
        return findTeacherApproverByRealName(advisorNames.get(0));
    }

    private Approver findTeacherApproverByRealName(String realName) {
        List<Approver> approvers = jdbcTemplate.query("""
                        SELECT u.id, u.real_name
                        FROM sys_user u
                        JOIN sys_user_role ur ON ur.user_id = u.id
                        JOIN sys_role r ON r.id = ur.role_id
                        WHERE u.real_name = ?
                          AND u.status = 1
                          AND r.role_code = 'TEACHER'
                          AND r.status = 1
                        ORDER BY u.id
                        """,
                (rs, rowNum) -> new Approver(rs.getLong("id"), rs.getString("real_name")),
                realName
        );
        if (approvers.isEmpty()) {
            return null;
        }
        if (approvers.size() > 1) {
            throw new WorkflowException("指导老师重名，无法自动匹配审批人");
        }
        return approvers.get(0);
    }

    private String resolveApproverRoleCode(ApplicationType type) {
        List<String> approverRoles = jdbcTemplate.query("""
                        SELECT n.approver_role_code
                        FROM wf_definition d
                        JOIN wf_node_definition n ON n.definition_id = d.id
                        WHERE d.business_type = ?
                          AND d.status = 1
                          AND n.status = 1
                          AND n.approver_role_code IS NOT NULL
                          AND n.approver_role_code <> ''
                        ORDER BY d.version_no DESC, n.sort_no ASC, n.id ASC
                        LIMIT 1
                        """,
                (rs, rowNum) -> rs.getString("approver_role_code"),
                type.typeCode()
        );
        return approverRoles.isEmpty() ? type.approverRoleCode() : approverRoles.get(0);
    }

    private void insertRecord(Long applicationId, String actionType, Long actorId, String comment) {
        jdbcTemplate.update("""
                        INSERT INTO wf_approval_record (application_id, action_type, actor_id, comment_text)
                        VALUES (?, ?, ?, ?)
                        """,
                applicationId,
                actionType,
                actorId,
                comment
        );
    }

    private void syncBusinessStatus(Long workflowApplicationId, String status) {
        updateStatusColumns("biz_internship_material", workflowApplicationId, status,
                "submit_status", "review_status", "business_status", "flow_status");
        updateStatusColumns("biz_student_alert", workflowApplicationId, status, "business_status");
        updateStatusColumns("biz_project_application", workflowApplicationId, status, "review_status", "flow_status");
        updateStatusColumns("biz_course_standard_review", workflowApplicationId, status, "review_status", "flow_status");
        updateStatusColumns("biz_leave_request", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_leave_cancellation", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_schedule_adjustment", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_meeting_room_booking", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_dorm_repair", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_asset_repair", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_scholarship_application", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_grant_application", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_difficulty_recognition", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_enrollment_certificate", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_textbook_order", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_goods_borrow_application", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_course_suspension_application", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_makeup_class_application", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_research_midterm_check", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_research_completion_application", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_lesson_plan_submission", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_teaching_outline_submission", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_grade_correction_request", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_exam_schedule_request", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_classroom_borrow_request", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_student_leave_application", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_student_return_confirmation", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_graduation_project_opening", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_graduation_project_midterm", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_research_achievement_registration", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_academic_lecture_application", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_office_supply_request", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_lab_safety_hazard_report", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_dorm_adjustment_request", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_stamp_request", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_vehicle_request", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_class_notice_receipt", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_student_warning_process", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_material_supplement", workflowApplicationId, status, "business_status", "flow_status");
        updateStatusColumns("biz_announcement_publish", workflowApplicationId, status, "business_status", "flow_status");
    }

    private void updateStatusColumns(String tableName, Long workflowApplicationId, String status, String... columnNames) {
        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (int index = 0; index < columnNames.length; index++) {
            if (index > 0) {
                sql.append(", ");
            }
            sql.append(columnNames[index]).append(" = ?");
        }
        sql.append(", updated_at = NOW() WHERE workflow_application_id = ?");

        Object[] args = new Object[columnNames.length + 1];
        for (int index = 0; index < columnNames.length; index++) {
            args[index] = status;
        }
        args[columnNames.length] = workflowApplicationId;
        jdbcTemplate.update(sql.toString(), args);
    }

    private void ensureApplicant(AuthenticatedUser currentUser, ApplicationRecord application) {
        if (!Objects.equals(currentUser.userId(), application.applicantId())) {
            throw new WorkflowException("仅申请人可执行该操作");
        }
    }

    private void ensureApprover(AuthenticatedUser currentUser, ApplicationRecord application) {
        if (!Objects.equals(currentUser.userId(), application.currentApproverId())) {
            throw new WorkflowException("当前用户不是该申请单审批人");
        }
    }

    private void ensureReadable(AuthenticatedUser currentUser, ApplicationRecord application) {
        boolean isApplicant = Objects.equals(currentUser.userId(), application.applicantId());
        boolean isApprover = Objects.equals(currentUser.userId(), application.currentApproverId());
        boolean isAdmin = isAdmin(currentUser);
        boolean hasHandled = hasHandledApplication(currentUser.userId(), application.id());
        boolean isPublishedReadable = STATUS_APPROVED.equals(application.status())
                && List.of("CLASS_NOTICE_RECEIPT", "ANNOUNCEMENT_PUBLISH").contains(application.typeCode());
        if (!isApplicant && !isApprover && !isAdmin && !hasHandled && !isPublishedReadable) {
            throw new WorkflowException("无权查看该申请单");
        }
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return currentUser.roles().contains("ADMIN") || "ADMIN".equalsIgnoreCase(currentUser.userType());
    }

    private boolean hasHandledApplication(Long userId, Long applicationId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM wf_approval_record
                        WHERE application_id = ?
                          AND actor_id = ?
                        """,
                Integer.class,
                applicationId,
                userId
        );
        return count != null && count > 0;
    }

    private void ensureStatus(String actualStatus, String... allowedStatuses) {
        for (String allowedStatus : allowedStatuses) {
            if (allowedStatus.equals(actualStatus)) {
                return;
            }
        }
        throw new WorkflowException("当前申请状态不允许执行该操作");
    }

    private boolean canSubmit(AuthenticatedUser currentUser, ApplicationRecord application) {
        return Objects.equals(currentUser.userId(), application.applicantId()) && STATUS_DRAFT.equals(application.status());
    }

    private boolean canWithdraw(AuthenticatedUser currentUser, ApplicationRecord application) {
        return Objects.equals(currentUser.userId(), application.applicantId())
                && (STATUS_PENDING.equals(application.status()) || STATUS_IN_PROGRESS.equals(application.status()));
    }

    private boolean canApprove(AuthenticatedUser currentUser, ApplicationRecord application) {
        return Objects.equals(currentUser.userId(), application.currentApproverId())
                && (STATUS_PENDING.equals(application.status()) || STATUS_IN_PROGRESS.equals(application.status()));
    }

    private boolean canReject(AuthenticatedUser currentUser, ApplicationRecord application) {
        return canApprove(currentUser, application);
    }

    private Long getLong(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime getDateTime(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String generateApplicationNo() {
        return "APP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private String normalizeComment(String comment, String fallback) {
        return comment == null || comment.isBlank() ? fallback : comment.trim();
    }

    private record ApplicationType(
            Long id,
            String typeCode,
            String typeName,
            String description,
            String approverRoleCode,
            Integer status
    ) {
    }

    private record Approver(Long userId, String realName) {
    }

    private record ApplicationRecord(
            Long id,
            String applicationNo,
            Long typeId,
            String typeCode,
            String typeName,
            String title,
            String content,
            Long applicantId,
            String applicantName,
            Long currentApproverId,
            String currentApproverName,
            String status,
            LocalDateTime submittedAt,
            LocalDateTime finishedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}
