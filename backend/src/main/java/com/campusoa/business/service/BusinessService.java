package com.campusoa.business.service;

import com.campusoa.business.dto.BusinessFieldValue;
import com.campusoa.business.dto.BusinessRecordDetail;
import com.campusoa.business.dto.BusinessRecordSummary;
import com.campusoa.business.dto.BusinessSelectOption;
import com.campusoa.business.exception.BusinessException;
import com.campusoa.security.AuthenticatedUser;
import com.campusoa.workflow.dto.CreateApplicationRequest;
import com.campusoa.workflow.dto.WorkflowApplicationDetail;
import com.campusoa.workflow.service.WorkflowService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BusinessService {

    private static final List<String> STATUS_FILTERS = List.of(
            "DRAFT", "PENDING", "IN_PROGRESS", "APPROVED", "REJECTED", "WITHDRAWN"
    );

    private final JdbcTemplate jdbcTemplate;
    private final WorkflowService workflowService;

    public BusinessService(JdbcTemplate jdbcTemplate, WorkflowService workflowService) {
        this.jdbcTemplate = jdbcTemplate;
        this.workflowService = workflowService;
    }

    @Transactional
    public Long createDraft(AuthenticatedUser currentUser, String businessKey, Map<String, Object> payload) {
        BusinessDefinition definition = requireDefinition(businessKey);
        validateCreator(currentUser, definition);

        Long typeId = findWorkflowTypeId(definition.workflowTypeCode());
        String title = buildTitle(definition, payload);
        String content = buildContent(definition, payload);
        Long workflowApplicationId = workflowService.createDraft(
                currentUser,
                new CreateApplicationRequest(typeId, title, content)
        );

        return switch (definition.key()) {
            case "internship-materials" -> insertInternship(currentUser, workflowApplicationId, payload);
            case "abnormal-students" -> insertAbnormalStudent(currentUser, workflowApplicationId, payload);
            case "research-projects" -> insertResearchProject(currentUser, workflowApplicationId, payload);
            case "course-standards" -> insertCourseStandard(currentUser, workflowApplicationId, payload);
            case "leave-applications" -> insertLeaveApplication(currentUser, workflowApplicationId, payload);
            case "leave-cancellations" -> insertLeaveCancellation(currentUser, workflowApplicationId, payload);
            case "schedule-adjustments" -> insertScheduleAdjustment(currentUser, workflowApplicationId, payload);
            case "meeting-rooms" -> insertMeetingRoom(currentUser, workflowApplicationId, payload);
            case "dorm-repairs" -> insertDormRepair(currentUser, workflowApplicationId, payload);
            case "asset-repairs" -> insertAssetRepair(currentUser, workflowApplicationId, payload);
            case "scholarship-applications" -> insertScholarshipApplication(currentUser, workflowApplicationId, payload);
            case "grant-applications" -> insertGrantApplication(currentUser, workflowApplicationId, payload);
            case "difficulty-recognitions" -> insertDifficultyRecognition(currentUser, workflowApplicationId, payload);
            case "enrollment-certificates" -> insertEnrollmentCertificate(currentUser, workflowApplicationId, payload);
            case "textbook-orders" -> insertTextbookOrder(currentUser, workflowApplicationId, payload);
            case "goods-borrow-applications" -> insertGoodsBorrowApplication(currentUser, workflowApplicationId, payload);
            case "course-suspension-applications" -> insertCourseSuspensionApplication(currentUser, workflowApplicationId, payload);
            case "makeup-class-applications" -> insertMakeupClassApplication(currentUser, workflowApplicationId, payload);
            case "research-midterm-checks" -> insertResearchMidtermCheck(currentUser, workflowApplicationId, payload);
            case "research-completion-applications" -> insertResearchCompletionApplication(currentUser, workflowApplicationId, payload);
            case "lesson-plan-submissions" -> insertLessonPlanSubmission(currentUser, workflowApplicationId, payload);
            case "teaching-outline-submissions" -> insertTeachingOutlineSubmission(currentUser, workflowApplicationId, payload);
            case "grade-correction-applications" -> insertGradeCorrectionApplication(currentUser, workflowApplicationId, payload);
            case "exam-schedule-applications" -> insertExamScheduleApplication(currentUser, workflowApplicationId, payload);
            case "classroom-borrow-applications" -> insertClassroomBorrowApplication(currentUser, workflowApplicationId, payload);
            case "student-leave-applications" -> insertStudentLeaveApplication(currentUser, workflowApplicationId, payload);
            case "student-return-confirmations" -> insertStudentReturnConfirmation(currentUser, workflowApplicationId, payload);
            case "graduation-project-openings" -> insertGraduationProjectOpening(currentUser, workflowApplicationId, payload);
            case "graduation-project-midterms" -> insertGraduationProjectMidterm(currentUser, workflowApplicationId, payload);
            case "research-achievement-registrations" -> insertResearchAchievementRegistration(currentUser, workflowApplicationId, payload);
            case "academic-lecture-applications" -> insertAcademicLectureApplication(currentUser, workflowApplicationId, payload);
            case "office-supply-requests" -> insertOfficeSupplyRequest(currentUser, workflowApplicationId, payload);
            case "lab-safety-hazard-reports" -> insertLabSafetyHazardReport(currentUser, workflowApplicationId, payload);
            case "dorm-adjustment-requests" -> insertDormAdjustmentRequest(currentUser, workflowApplicationId, payload);
            case "stamp-requests" -> insertStampRequest(currentUser, workflowApplicationId, payload);
            case "vehicle-requests" -> insertVehicleRequest(currentUser, workflowApplicationId, payload);
            case "class-notice-receipts" -> insertClassNoticeReceipt(currentUser, workflowApplicationId, payload);
            case "student-warning-processes" -> insertStudentWarningProcess(currentUser, workflowApplicationId, payload);
            case "material-supplements" -> insertMaterialSupplement(currentUser, workflowApplicationId, payload);
            case "announcement-publishes" -> insertAnnouncementPublish(currentUser, workflowApplicationId, payload);
            default -> throw new BusinessException("不支持的业务类型");
        };
    }

    @Transactional
    public void submit(AuthenticatedUser currentUser, String businessKey, Long id) {
        BusinessRecord record = findBusinessRecord(currentUser, businessKey, id);
        workflowService.submit(currentUser, record.workflowApplicationId());
    }

    public List<BusinessSelectOption> listFieldOptions(AuthenticatedUser currentUser, String businessKey, String fieldKey) {
        return switch (businessKey) {
            case "student-return-confirmations" -> {
                if (!"relatedLeaveNo".equals(fieldKey)) {
                    throw new BusinessException("不支持的业务字段选项");
                }
                yield listSelectableStudentLeaveOptions(currentUser.userId());
            }
            case "graduation-project-openings" -> {
                if (!"advisorName".equals(fieldKey)) {
                    throw new BusinessException("不支持的业务字段选项");
                }
                yield listSelectableTeacherOptions();
            }
            case "graduation-project-midterms" -> {
                if (!"projectName".equals(fieldKey)) {
                    throw new BusinessException("不支持的业务字段选项");
                }
                yield listSelectableGraduationOpeningOptions(currentUser.userId());
            }
            default -> throw new BusinessException("当前业务未提供可选字段数据");
        };
    }

    public List<BusinessRecordSummary> list(AuthenticatedUser currentUser, String businessKey, String status) {
        BusinessDefinition definition = requireDefinition(businessKey);
        StringBuilder sql = new StringBuilder("""
                SELECT b.id,
                       """);
        sql.append(definition.listTitleSql()).append("""
                       AS title,
                       wf.status,
                       applicant.real_name AS applicant_name,
                       approver.real_name AS current_approver_name,
                       wf.submitted_at,
                       b.updated_at
                """);
        sql.append("FROM ").append(definition.tableName()).append("""
                 b
                JOIN wf_application wf ON wf.id = b.workflow_application_id
                JOIN sys_user applicant ON applicant.id = wf.applicant_id
                LEFT JOIN sys_user approver ON approver.id = wf.current_approver_id
                WHERE 1 = 1
                """);

        List<Object> args = new ArrayList<>();
        if (!isAdmin(currentUser)) {
            boolean publishedReadable = isPublishedReadableBusiness(currentUser, definition);
            sql.append("""
                     AND (
                         wf.applicant_id = ?
                         OR wf.current_approver_id = ?
                         OR EXISTS (
                             SELECT 1
                             FROM wf_approval_record r
                             WHERE r.application_id = wf.id
                               AND r.actor_id = ?
                         )
                    """);
            if (publishedReadable) {
                sql.append(" OR wf.status = 'APPROVED'");
            }
            sql.append("""
                     )
                    """);
            args.add(currentUser.userId());
            args.add(currentUser.userId());
            args.add(currentUser.userId());
        }
        if (status != null && !status.isBlank()) {
            if (!STATUS_FILTERS.contains(status)) {
                throw new BusinessException("不支持的状态筛选");
            }
            sql.append(" AND wf.status = ?");
            args.add(status);
        }
        sql.append(" ORDER BY b.updated_at DESC, b.id DESC");

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new BusinessRecordSummary(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("applicant_name"),
                        rs.getString("current_approver_name"),
                        getDateTime(rs.getTimestamp("submitted_at")),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                args.toArray()
        );
    }

    public BusinessRecordDetail detail(AuthenticatedUser currentUser, String businessKey, Long id) {
        BusinessDefinition definition = requireDefinition(businessKey);
        BusinessRecord record = findBusinessRecord(currentUser, businessKey, id);
        WorkflowApplicationDetail workflow = workflowService.getDetail(currentUser, record.workflowApplicationId());
        List<BusinessFieldValue> fields = switch (definition.key()) {
            case "internship-materials" -> internshipFields(id);
            case "abnormal-students" -> abnormalStudentFields(id);
            case "research-projects" -> researchProjectFields(id);
            case "course-standards" -> courseStandardFields(id);
            case "leave-applications" -> leaveApplicationFields(id);
            case "leave-cancellations" -> leaveCancellationFields(id);
            case "schedule-adjustments" -> scheduleAdjustmentFields(id);
            case "meeting-rooms" -> meetingRoomFields(id);
            case "dorm-repairs" -> dormRepairFields(id);
            case "asset-repairs" -> assetRepairFields(id);
            case "scholarship-applications" -> scholarshipApplicationFields(id);
            case "grant-applications" -> grantApplicationFields(id);
            case "difficulty-recognitions" -> difficultyRecognitionFields(id);
            case "enrollment-certificates" -> enrollmentCertificateFields(id);
            case "textbook-orders" -> textbookOrderFields(id);
            case "goods-borrow-applications" -> goodsBorrowApplicationFields(id);
            case "course-suspension-applications" -> courseSuspensionApplicationFields(id);
            case "makeup-class-applications" -> makeupClassApplicationFields(id);
            case "research-midterm-checks" -> researchMidtermCheckFields(id);
            case "research-completion-applications" -> researchCompletionApplicationFields(id);
            case "lesson-plan-submissions" -> lessonPlanSubmissionFields(id);
            case "teaching-outline-submissions" -> teachingOutlineSubmissionFields(id);
            case "grade-correction-applications" -> gradeCorrectionApplicationFields(id);
            case "exam-schedule-applications" -> examScheduleApplicationFields(id);
            case "classroom-borrow-applications" -> classroomBorrowApplicationFields(id);
            case "student-leave-applications" -> studentLeaveApplicationFields(id);
            case "student-return-confirmations" -> studentReturnConfirmationFields(id);
            case "graduation-project-openings" -> graduationProjectOpeningFields(id);
            case "graduation-project-midterms" -> graduationProjectMidtermFields(id);
            case "research-achievement-registrations" -> researchAchievementRegistrationFields(id);
            case "academic-lecture-applications" -> academicLectureApplicationFields(id);
            case "office-supply-requests" -> officeSupplyRequestFields(id);
            case "lab-safety-hazard-reports" -> labSafetyHazardReportFields(id);
            case "dorm-adjustment-requests" -> dormAdjustmentRequestFields(id);
            case "stamp-requests" -> stampRequestFields(id);
            case "vehicle-requests" -> vehicleRequestFields(id);
            case "class-notice-receipts" -> classNoticeReceiptFields(id);
            case "student-warning-processes" -> studentWarningProcessFields(id);
            case "material-supplements" -> materialSupplementFields(id);
            case "announcement-publishes" -> announcementPublishFields(id);
            default -> throw new BusinessException("不支持的业务类型");
        };
        return new BusinessRecordDetail(id, definition.key(), definition.name(), record.title(), fields, workflow);
    }

    private BusinessRecord findBusinessRecord(AuthenticatedUser currentUser, String businessKey, Long id) {
        BusinessDefinition definition = requireDefinition(businessKey);
        String sql = "SELECT b.id, b.workflow_application_id, " + definition.detailTitleSql() + " AS title, b.created_by "
                + "FROM " + definition.tableName() + " b WHERE b.id = ?";
        List<BusinessRecord> records = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    Long workflowApplicationId = rs.getLong("workflow_application_id");
                    if (rs.wasNull()) {
                        workflowApplicationId = null;
                    }
                    return new BusinessRecord(
                            rs.getLong("id"),
                            workflowApplicationId,
                            rs.getString("title"),
                            rs.getLong("created_by")
                    );
                },
                id
        );
        if (records.isEmpty()) {
            throw new BusinessException("业务记录不存在");
        }
        BusinessRecord record = records.get(0);
        if (record.workflowApplicationId() == null) {
            throw new BusinessException("业务流程草稿不存在");
        }
        if (!isAdmin(currentUser)) {
            workflowService.ensureReadable(currentUser, record.workflowApplicationId());
        }
        return record;
    }

    private Long insertInternship(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String semesterCode = requiredString(payload, "semesterCode", "semesterCode");
        String internshipCompany = requiredString(payload, "internshipCompany", "internshipCompany");
        String internshipPosition = requiredString(payload, "internshipPosition", "internshipPosition");
        String tutorName = requiredString(payload, "tutorName", "tutorName");
        String tutorPhone = requiredString(payload, "tutorPhone", "tutorPhone");
        String startDate = requiredString(payload, "startDate", "startDate");
        String endDate = requiredString(payload, "endDate", "endDate");
        String materialType = requiredString(payload, "materialType", "materialType");
        String materialTitle = requiredString(payload, "materialTitle", "materialTitle");
        String materialSummary = requiredString(payload, "materialSummary", "materialSummary");

        jdbcTemplate.update("""
                        INSERT INTO biz_internship_material (
                            workflow_application_id, student_id, semester_code, internship_company, internship_position,
                            tutor_name, tutor_phone, start_date, end_date, material_type, material_title,
                            material_summary, submit_status, review_status, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), semesterCode, internshipCompany, internshipPosition,
                tutorName, tutorPhone, startDate, endDate, materialType, materialTitle,
                materialSummary, "DRAFT", "DRAFT", "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertAbnormalStudent(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String studentNo = requiredString(payload, "studentNo", "studentNo");
        String studentName = requiredString(payload, "studentName", "studentName");
        String alertType = requiredString(payload, "alertType", "alertType");
        String alertLevel = requiredString(payload, "alertLevel", "alertLevel");
        String problemDescription = requiredString(payload, "problemDescription", "problemDescription");
        String interventionPlan = requiredString(payload, "interventionPlan", "interventionPlan");
        String followUpStatus = requiredString(payload, "followUpStatus", "followUpStatus");

        jdbcTemplate.update("""
                        INSERT INTO biz_student_alert (
                            workflow_application_id, student_id, student_no, student_name, alert_type, alert_level,
                            problem_description, intervention_plan, follow_up_status, business_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), studentNo, studentName, alertType, alertLevel,
                problemDescription, interventionPlan, followUpStatus, "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertResearchProject(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String projectName = requiredString(payload, "projectName", "projectName");
        String projectCategory = requiredString(payload, "projectCategory", "projectCategory");
        String applicationYear = requiredString(payload, "applicationYear", "applicationYear");
        String projectLevel = requiredString(payload, "projectLevel", "projectLevel");
        String teamMembers = requiredString(payload, "teamMembers", "teamMembers");
        String projectSummary = requiredString(payload, "projectSummary", "projectSummary");
        BigDecimal budgetAmount = requiredDecimal(payload, "budgetAmount", "budgetAmount");

        jdbcTemplate.update("""
                        INSERT INTO biz_project_application (
                            workflow_application_id, applicant_id, project_name, project_category, application_year,
                            project_level, budget_amount, team_members, project_summary,
                            review_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), projectName, projectCategory, applicationYear,
                projectLevel, budgetAmount, teamMembers, projectSummary,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertCourseStandard(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseCode = requiredString(payload, "courseCode", "courseCode");
        String courseName = requiredString(payload, "courseName", "courseName");
        String academicYear = requiredString(payload, "academicYear", "academicYear");
        String targetMajor = requiredString(payload, "targetMajor", "targetMajor");
        Integer totalHours = requiredInteger(payload, "totalHours", "totalHours");
        String standardVersion = requiredString(payload, "standardVersion", "standardVersion");
        String revisionNote = requiredString(payload, "revisionNote", "revisionNote");

        jdbcTemplate.update("""
                        INSERT INTO biz_course_standard_review (
                            workflow_application_id, teacher_id, course_code, course_name, academic_year,
                            target_major, total_hours, standard_version, revision_note,
                            review_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseCode, courseName, academicYear,
                targetMajor, totalHours, standardVersion, revisionNote,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertLeaveApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String leaveType = requiredString(payload, "leaveType", "leaveType");
        String startTime = requiredString(payload, "startTime", "startTime");
        String endTime = requiredString(payload, "endTime", "endTime");
        String reason = requiredString(payload, "reason", "reason");
        String emergencyContact = requiredString(payload, "emergencyContact", "emergencyContact");
        String emergencyPhone = requiredString(payload, "emergencyPhone", "emergencyPhone");
        String destination = requiredString(payload, "destination", "destination");

        jdbcTemplate.update("""
                        INSERT INTO biz_leave_request (
                            workflow_application_id, student_id, leave_type, start_time, end_time, reason,
                            emergency_contact, emergency_phone, destination, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), leaveType, startTime, endTime, reason,
                emergencyContact, emergencyPhone, destination, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertLeaveCancellation(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String relatedLeaveNo = requiredString(payload, "relatedLeaveNo", "relatedLeaveNo");
        String returnTime = requiredString(payload, "returnTime", "returnTime");
        String cancelReason = requiredString(payload, "cancelReason", "cancelReason");
        String actualReturnNote = requiredString(payload, "actualReturnNote", "actualReturnNote");

        jdbcTemplate.update("""
                        INSERT INTO biz_leave_cancellation (
                            workflow_application_id, student_id, related_leave_no, return_time,
                            cancel_reason, actual_return_note, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), relatedLeaveNo, returnTime,
                cancelReason, actualReturnNote, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertScheduleAdjustment(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseName = requiredString(payload, "courseName", "courseName");
        String originalTime = requiredString(payload, "originalTime", "originalTime");
        String adjustedTime = requiredString(payload, "adjustedTime", "adjustedTime");
        String originalClassroom = requiredString(payload, "originalClassroom", "originalClassroom");
        String adjustedClassroom = requiredString(payload, "adjustedClassroom", "adjustedClassroom");
        String adjustmentReason = requiredString(payload, "adjustmentReason", "adjustmentReason");

        jdbcTemplate.update("""
                        INSERT INTO biz_schedule_adjustment (
                            workflow_application_id, teacher_id, course_name, original_time, adjusted_time,
                            original_classroom, adjusted_classroom, adjustment_reason, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseName, originalTime, adjustedTime,
                originalClassroom, adjustedClassroom, adjustmentReason, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertMeetingRoom(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String meetingSubject = requiredString(payload, "meetingSubject", "meetingSubject");
        String roomName = requiredString(payload, "roomName", "roomName");
        String startTime = requiredString(payload, "startTime", "startTime");
        String endTime = requiredString(payload, "endTime", "endTime");
        Integer attendeeCount = requiredInteger(payload, "attendeeCount", "attendeeCount");
        String equipmentNeeds = requiredString(payload, "equipmentNeeds", "equipmentNeeds");
        String contactName = requiredString(payload, "contactName", "contactName");
        String contactPhone = requiredString(payload, "contactPhone", "contactPhone");

        jdbcTemplate.update("""
                        INSERT INTO biz_meeting_room_booking (
                            workflow_application_id, applicant_id, meeting_subject, room_name, start_time, end_time,
                            attendee_count, equipment_needs, contact_name, contact_phone, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), meetingSubject, roomName, startTime, endTime,
                attendeeCount, equipmentNeeds, contactName, contactPhone, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertDormRepair(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String dormBuilding = requiredString(payload, "dormBuilding", "dormBuilding");
        String roomNo = requiredString(payload, "roomNo", "roomNo");
        String repairType = requiredString(payload, "repairType", "repairType");
        String problemDescription = requiredString(payload, "problemDescription", "problemDescription");
        String contactPhone = requiredString(payload, "contactPhone", "contactPhone");
        String urgencyLevel = requiredString(payload, "urgencyLevel", "urgencyLevel");

        jdbcTemplate.update("""
                        INSERT INTO biz_dorm_repair (
                            workflow_application_id, student_id, dorm_building, room_no, repair_type,
                            problem_description, contact_phone, urgency_level, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), dormBuilding, roomNo, repairType,
                problemDescription, contactPhone, urgencyLevel, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertAssetRepair(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String assetNo = requiredString(payload, "assetNo", "assetNo");
        String assetName = requiredString(payload, "assetName", "assetName");
        String locationText = requiredString(payload, "locationText", "locationText");
        String faultDescription = requiredString(payload, "faultDescription", "faultDescription");
        String urgencyLevel = requiredString(payload, "urgencyLevel", "urgencyLevel");

        jdbcTemplate.update("""
                        INSERT INTO biz_asset_repair (
                            workflow_application_id, applicant_id, asset_no, asset_name, location_text,
                            fault_description, urgency_level, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), assetNo, assetName, locationText,
                faultDescription, urgencyLevel, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertScholarshipApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String scholarshipType = requiredString(payload, "scholarshipType", "scholarshipType");
        String gradeRank = requiredString(payload, "gradeRank", "gradeRank");
        String comprehensiveScore = requiredString(payload, "comprehensiveScore", "comprehensiveScore");
        String awardRecords = requiredString(payload, "awardRecords", "awardRecords");
        String familySituation = requiredString(payload, "familySituation", "familySituation");
        String applicationReason = requiredString(payload, "applicationReason", "applicationReason");

        jdbcTemplate.update("""
                        INSERT INTO biz_scholarship_application (
                            workflow_application_id, student_id, scholarship_type, grade_rank, comprehensive_score,
                            award_records, family_situation, application_reason, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), scholarshipType, gradeRank, comprehensiveScore,
                awardRecords, familySituation, applicationReason, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertGrantApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String familyIncome = requiredString(payload, "familyIncome", "familyIncome");
        Integer householdSize = requiredInteger(payload, "householdSize", "householdSize");
        String difficultyLevel = requiredString(payload, "difficultyLevel", "difficultyLevel");
        String applicationReason = requiredString(payload, "applicationReason", "applicationReason");
        String specialNotes = requiredString(payload, "specialNotes", "specialNotes");

        jdbcTemplate.update("""
                        INSERT INTO biz_grant_application (
                            workflow_application_id, student_id, family_income, household_size, difficulty_level,
                            application_reason, special_notes, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), familyIncome, householdSize, difficultyLevel,
                applicationReason, specialNotes, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertDifficultyRecognition(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String familyMembers = requiredString(payload, "familyMembers", "familyMembers");
        String annualIncome = requiredString(payload, "annualIncome", "annualIncome");
        String specialCondition = requiredString(payload, "specialCondition", "specialCondition");
        String recognitionLevel = requiredString(payload, "recognitionLevel", "recognitionLevel");
        String applicationReason = requiredString(payload, "applicationReason", "applicationReason");

        jdbcTemplate.update("""
                        INSERT INTO biz_difficulty_recognition (
                            workflow_application_id, student_id, family_members, annual_income, special_condition,
                            recognition_level, application_reason, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), familyMembers, annualIncome, specialCondition,
                recognitionLevel, applicationReason, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertEnrollmentCertificate(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String certificatePurpose = requiredString(payload, "certificatePurpose", "certificatePurpose");
        String receiverOrg = requiredString(payload, "receiverOrg", "receiverOrg");
        String languageType = requiredString(payload, "languageType", "languageType");
        String deliveryMethod = requiredString(payload, "deliveryMethod", "deliveryMethod");
        String remarkText = requiredString(payload, "remarkText", "remarkText");

        jdbcTemplate.update("""
                        INSERT INTO biz_enrollment_certificate (
                            workflow_application_id, student_id, certificate_purpose, receiver_org, language_type,
                            delivery_method, remark_text, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), certificatePurpose, receiverOrg, languageType,
                deliveryMethod, remarkText, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertTextbookOrder(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseName = requiredString(payload, "courseName", "courseName");
        String textbookName = requiredString(payload, "textbookName", "textbookName");
        String isbn = requiredString(payload, "isbn", "isbn");
        String publisher = requiredString(payload, "publisher", "publisher");
        String authorName = requiredString(payload, "authorName", "authorName");
        String classNames = requiredString(payload, "classNames", "classNames");
        Integer orderQuantity = requiredInteger(payload, "orderQuantity", "orderQuantity");
        String selectionReason = requiredString(payload, "selectionReason", "selectionReason");

        jdbcTemplate.update("""
                        INSERT INTO biz_textbook_order (
                            workflow_application_id, teacher_id, course_name, textbook_name, isbn,
                            publisher, author_name, class_names, order_quantity, selection_reason,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseName, textbookName, isbn,
                publisher, authorName, classNames, orderQuantity, selectionReason,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertGoodsBorrowApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String itemName = requiredString(payload, "itemName", "itemName");
        String itemSpec = requiredString(payload, "itemSpec", "itemSpec");
        Integer borrowQuantity = requiredInteger(payload, "borrowQuantity", "borrowQuantity");
        String borrowStartTime = requiredString(payload, "borrowStartTime", "borrowStartTime");
        String borrowEndTime = requiredString(payload, "borrowEndTime", "borrowEndTime");
        String borrowPurpose = requiredString(payload, "borrowPurpose", "borrowPurpose");
        String returnPlan = requiredString(payload, "returnPlan", "returnPlan");
        String contactName = requiredString(payload, "contactName", "contactName");
        String contactPhone = requiredString(payload, "contactPhone", "contactPhone");
        String remarks = requiredString(payload, "remarks", "remarks");

        jdbcTemplate.update("""
                        INSERT INTO biz_goods_borrow_application (
                            workflow_application_id, applicant_id, item_name, item_spec, borrow_quantity,
                            borrow_start_time, borrow_end_time, borrow_purpose, return_plan,
                            contact_name, contact_phone, remarks, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), itemName, itemSpec, borrowQuantity,
                borrowStartTime, borrowEndTime, borrowPurpose, returnPlan,
                contactName, contactPhone, remarks, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertCourseSuspensionApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseName = requiredString(payload, "courseName", "courseName");
        String courseCode = requiredString(payload, "courseCode", "courseCode");
        String suspensionDate = requiredString(payload, "suspensionDate", "suspensionDate");
        String suspensionStartTime = requiredString(payload, "suspensionStartTime", "suspensionStartTime");
        String suspensionEndTime = requiredString(payload, "suspensionEndTime", "suspensionEndTime");
        String suspensionReason = requiredString(payload, "suspensionReason", "suspensionReason");
        String makeupSuggestion = requiredString(payload, "makeupSuggestion", "makeupSuggestion");
        String affectedClass = requiredString(payload, "affectedClass", "affectedClass");
        String remarks = requiredString(payload, "remarks", "remarks");

        jdbcTemplate.update("""
                        INSERT INTO biz_course_suspension_application (
                            workflow_application_id, teacher_id, course_name, course_code, suspension_date,
                            suspension_start_time, suspension_end_time, suspension_reason, makeup_suggestion,
                            affected_class, remarks, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseName, courseCode, suspensionDate,
                suspensionStartTime, suspensionEndTime, suspensionReason, makeupSuggestion,
                affectedClass, remarks, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertMakeupClassApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseName = requiredString(payload, "courseName", "courseName");
        String courseCode = requiredString(payload, "courseCode", "courseCode");
        String makeupDate = requiredString(payload, "makeupDate", "makeupDate");
        String makeupStartTime = requiredString(payload, "makeupStartTime", "makeupStartTime");
        String makeupEndTime = requiredString(payload, "makeupEndTime", "makeupEndTime");
        String makeupLocation = requiredString(payload, "makeupLocation", "makeupLocation");
        String relatedSuspensionNo = requiredString(payload, "relatedSuspensionNo", "relatedSuspensionNo");
        String makeupReason = requiredString(payload, "makeupReason", "makeupReason");
        String affectedClass = requiredString(payload, "affectedClass", "affectedClass");
        String noticePlan = requiredString(payload, "noticePlan", "noticePlan");

        jdbcTemplate.update("""
                        INSERT INTO biz_makeup_class_application (
                            workflow_application_id, teacher_id, course_name, course_code, makeup_date,
                            makeup_start_time, makeup_end_time, makeup_location, related_suspension_no,
                            makeup_reason, affected_class, notice_plan, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseName, courseCode, makeupDate,
                makeupStartTime, makeupEndTime, makeupLocation, relatedSuspensionNo,
                makeupReason, affectedClass, noticePlan, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertResearchMidtermCheck(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String projectName = requiredString(payload, "projectName", "projectName");
        String projectNo = requiredString(payload, "projectNo", "projectNo");
        String progressRate = requiredString(payload, "progressRate", "progressRate");
        String stageOutcome = requiredString(payload, "stageOutcome", "stageOutcome");
        String existingProblems = requiredString(payload, "existingProblems", "existingProblems");
        String correctionPlan = requiredString(payload, "correctionPlan", "correctionPlan");
        String budgetUsage = requiredString(payload, "budgetUsage", "budgetUsage");
        String remarks = requiredString(payload, "remarks", "remarks");

        jdbcTemplate.update("""
                        INSERT INTO biz_research_midterm_check (
                            workflow_application_id, teacher_id, project_name, project_no, progress_rate,
                            stage_outcome, existing_problems, correction_plan, budget_usage, remarks,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), projectName, projectNo, progressRate,
                stageOutcome, existingProblems, correctionPlan, budgetUsage, remarks,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertResearchCompletionApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String projectName = requiredString(payload, "projectName", "projectName");
        String projectNo = requiredString(payload, "projectNo", "projectNo");
        String achievements = requiredString(payload, "achievements", "achievements");
        String fundingUsage = requiredString(payload, "fundingUsage", "fundingUsage");
        String completionReport = requiredString(payload, "completionReport", "completionReport");
        String expertList = requiredString(payload, "expertList", "expertList");
        String conclusionNote = requiredString(payload, "conclusionNote", "conclusionNote");

        jdbcTemplate.update("""
                        INSERT INTO biz_research_completion_application (
                            workflow_application_id, teacher_id, project_name, project_no, achievements,
                            funding_usage, completion_report, expert_list, conclusion_note,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), projectName, projectNo, achievements,
                fundingUsage, completionReport, expertList, conclusionNote,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private List<BusinessFieldValue> internshipFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT semester_code, internship_company, internship_position, tutor_name, tutor_phone,
                               start_date, end_date, material_type, material_title, material_summary
                        FROM biz_internship_material
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("semesterCode", "学期", rs.getString("semester_code")),
                        field("internshipCompany", "实习单位", rs.getString("internship_company")),
                        field("internshipPosition", "实习岗位", rs.getString("internship_position")),
                        field("tutorName", "指导老师", rs.getString("tutor_name")),
                        field("tutorPhone", "指导老师电话", rs.getString("tutor_phone")),
                        field("startDate", "开始日期", rs.getDate("start_date") == null ? null : rs.getDate("start_date").toString()),
                        field("endDate", "结束日期", rs.getDate("end_date") == null ? null : rs.getDate("end_date").toString()),
                        field("materialType", "材料类型", rs.getString("material_type")),
                        field("materialTitle", "材料标题", rs.getString("material_title")),
                        field("materialSummary", "材料说明", rs.getString("material_summary"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> abnormalStudentFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT student_no, student_name, alert_type, alert_level, alert_reason,
                               problem_description, intervention_plan, follow_up_status
                        FROM biz_student_alert
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("studentNo", "学号", rs.getString("student_no")),
                        field("studentName", "姓名", rs.getString("student_name")),
                        field("alertType", "异常类型", rs.getString("alert_type")),
                        field("alertLevel", "异常等级", rs.getString("alert_level")),
                        field("alertReason", "异常原因", rs.getString("alert_reason")),
                        field("problemDescription", "问题描述", rs.getString("problem_description")),
                        field("interventionPlan", "干预措施", rs.getString("intervention_plan")),
                        field("followUpStatus", "跟进状态", rs.getString("follow_up_status"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> researchProjectFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT project_name, project_category, application_year, project_level, budget_amount,
                               team_members, project_summary
                        FROM biz_project_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("projectName", "课题名称", rs.getString("project_name")),
                        field("projectCategory", "课题类别", rs.getString("project_category")),
                        field("applicationYear", "申报年度", rs.getString("application_year")),
                        field("projectLevel", "课题级别", rs.getString("project_level")),
                        field("budgetAmount", "预算金额", rs.getString("budget_amount")),
                        field("teamMembers", "团队成员", rs.getString("team_members")),
                        field("projectSummary", "课题简介", rs.getString("project_summary"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> courseStandardFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_code, course_name, academic_year, target_major, total_hours,
                               standard_version, revision_note
                        FROM biz_course_standard_review
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseCode", "课程编号", rs.getString("course_code")),
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("academicYear", "学年", rs.getString("academic_year")),
                        field("targetMajor", "适用专业", rs.getString("target_major")),
                        field("totalHours", "总学时", rs.getString("total_hours")),
                        field("standardVersion", "标准版本", rs.getString("standard_version")),
                        field("revisionNote", "修订说明", rs.getString("revision_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> leaveApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT leave_type, start_time, end_time, reason, emergency_contact,
                               emergency_phone, destination
                        FROM biz_leave_request
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("leaveType", "请假类型", rs.getString("leave_type")),
                        field("startTime", "开始时间", formatDateTime(rs.getTimestamp("start_time"))),
                        field("endTime", "结束时间", formatDateTime(rs.getTimestamp("end_time"))),
                        field("reason", "请假原因", rs.getString("reason")),
                        field("emergencyContact", "紧急联系人", rs.getString("emergency_contact")),
                        field("emergencyPhone", "紧急联系电话", rs.getString("emergency_phone")),
                        field("destination", "去向", rs.getString("destination"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> leaveCancellationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT related_leave_no, return_time, cancel_reason, actual_return_note
                        FROM biz_leave_cancellation
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("relatedLeaveNo", "关联请假单号", rs.getString("related_leave_no")),
                        field("returnTime", "返校时间", formatDateTime(rs.getTimestamp("return_time"))),
                        field("cancelReason", "销假原因", rs.getString("cancel_reason")),
                        field("actualReturnNote", "返校说明", rs.getString("actual_return_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> scheduleAdjustmentFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_name, original_time, adjusted_time, original_classroom,
                               adjusted_classroom, adjustment_reason
                        FROM biz_schedule_adjustment
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("originalTime", "原上课时间", formatDateTime(rs.getTimestamp("original_time"))),
                        field("adjustedTime", "调整后时间", formatDateTime(rs.getTimestamp("adjusted_time"))),
                        field("originalClassroom", "原教室", rs.getString("original_classroom")),
                        field("adjustedClassroom", "调整后教室", rs.getString("adjusted_classroom")),
                        field("adjustmentReason", "调课原因", rs.getString("adjustment_reason"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> meetingRoomFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT meeting_subject, room_name, start_time, end_time, attendee_count,
                               equipment_needs, contact_name, contact_phone
                        FROM biz_meeting_room_booking
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("meetingSubject", "会议主题", rs.getString("meeting_subject")),
                        field("roomName", "会议室", rs.getString("room_name")),
                        field("startTime", "开始时间", formatDateTime(rs.getTimestamp("start_time"))),
                        field("endTime", "结束时间", formatDateTime(rs.getTimestamp("end_time"))),
                        field("attendeeCount", "参会人数", rs.getString("attendee_count")),
                        field("equipmentNeeds", "设备需求", rs.getString("equipment_needs")),
                        field("contactName", "联系人", rs.getString("contact_name")),
                        field("contactPhone", "联系电话", rs.getString("contact_phone"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> dormRepairFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT dorm_building, room_no, repair_type, problem_description, contact_phone, urgency_level
                        FROM biz_dorm_repair
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("dormBuilding", "宿舍楼", rs.getString("dorm_building")),
                        field("roomNo", "房间号", rs.getString("room_no")),
                        field("repairType", "报修类型", rs.getString("repair_type")),
                        field("problemDescription", "问题描述", rs.getString("problem_description")),
                        field("contactPhone", "联系电话", rs.getString("contact_phone")),
                        field("urgencyLevel", "紧急程度", rs.getString("urgency_level"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> assetRepairFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT asset_no, asset_name, location_text, fault_description, urgency_level
                        FROM biz_asset_repair
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("assetNo", "资产编号", rs.getString("asset_no")),
                        field("assetName", "资产名称", rs.getString("asset_name")),
                        field("locationText", "位置", rs.getString("location_text")),
                        field("faultDescription", "故障描述", rs.getString("fault_description")),
                        field("urgencyLevel", "紧急程度", rs.getString("urgency_level"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> scholarshipApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT scholarship_type, grade_rank, comprehensive_score, award_records,
                               family_situation, application_reason
                        FROM biz_scholarship_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("scholarshipType", "奖学金类型", rs.getString("scholarship_type")),
                        field("gradeRank", "成绩排名", rs.getString("grade_rank")),
                        field("comprehensiveScore", "综合成绩", rs.getString("comprehensive_score")),
                        field("awardRecords", "获奖记录", rs.getString("award_records")),
                        field("familySituation", "家庭情况", rs.getString("family_situation")),
                        field("applicationReason", "申请原因", rs.getString("application_reason"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> grantApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT family_income, household_size, difficulty_level, application_reason, special_notes
                        FROM biz_grant_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("familyIncome", "家庭收入", rs.getString("family_income")),
                        field("householdSize", "家庭人口", rs.getString("household_size")),
                        field("difficultyLevel", "困难等级", rs.getString("difficulty_level")),
                        field("applicationReason", "申请原因", rs.getString("application_reason")),
                        field("specialNotes", "补充说明", rs.getString("special_notes"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> difficultyRecognitionFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT family_members, annual_income, special_condition, recognition_level, application_reason
                        FROM biz_difficulty_recognition
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("familyMembers", "家庭成员", rs.getString("family_members")),
                        field("annualIncome", "年收入", rs.getString("annual_income")),
                        field("specialCondition", "特殊情况", rs.getString("special_condition")),
                        field("recognitionLevel", "认定等级", rs.getString("recognition_level")),
                        field("applicationReason", "申请原因", rs.getString("application_reason"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> enrollmentCertificateFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT certificate_purpose, receiver_org, language_type, delivery_method, remark_text
                        FROM biz_enrollment_certificate
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("certificatePurpose", "证明用途", rs.getString("certificate_purpose")),
                        field("receiverOrg", "接收单位", rs.getString("receiver_org")),
                        field("languageType", "语言类型", rs.getString("language_type")),
                        field("deliveryMethod", "领取方式", rs.getString("delivery_method")),
                        field("remarkText", "备注", rs.getString("remark_text"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> textbookOrderFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_name, textbook_name, isbn, publisher, author_name, class_names,
                               order_quantity, selection_reason
                        FROM biz_textbook_order
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("textbookName", "教材名称", rs.getString("textbook_name")),
                        field("isbn", "ISBN", rs.getString("isbn")),
                        field("publisher", "出版社", rs.getString("publisher")),
                        field("authorName", "作者", rs.getString("author_name")),
                        field("classNames", "班级范围", rs.getString("class_names")),
                        field("orderQuantity", "征订数量", rs.getString("order_quantity")),
                        field("selectionReason", "选用理由", rs.getString("selection_reason"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> goodsBorrowApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT item_name, item_spec, borrow_quantity, borrow_start_time, borrow_end_time,
                               borrow_purpose, return_plan, contact_name, contact_phone, remarks
                        FROM biz_goods_borrow_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("itemName", "物资名称", rs.getString("item_name")),
                        field("itemSpec", "物资规格", rs.getString("item_spec")),
                        field("borrowQuantity", "借用数量", rs.getString("borrow_quantity")),
                        field("borrowStartTime", "借用开始时间", formatDateTime(rs.getTimestamp("borrow_start_time"))),
                        field("borrowEndTime", "借用结束时间", formatDateTime(rs.getTimestamp("borrow_end_time"))),
                        field("borrowPurpose", "借用用途", rs.getString("borrow_purpose")),
                        field("returnPlan", "归还计划", rs.getString("return_plan")),
                        field("contactName", "联系人", rs.getString("contact_name")),
                        field("contactPhone", "联系电话", rs.getString("contact_phone")),
                        field("remarks", "备注", rs.getString("remarks"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> courseSuspensionApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_name, course_code, suspension_date, suspension_start_time, suspension_end_time,
                               suspension_reason, makeup_suggestion, affected_class, remarks
                        FROM biz_course_suspension_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("courseCode", "课程编号", rs.getString("course_code")),
                        field("suspensionDate", "停课日期", rs.getDate("suspension_date") == null ? null : rs.getDate("suspension_date").toString()),
                        field("suspensionStartTime", "停课开始时间", formatDateTime(rs.getTimestamp("suspension_start_time"))),
                        field("suspensionEndTime", "停课结束时间", formatDateTime(rs.getTimestamp("suspension_end_time"))),
                        field("suspensionReason", "停课原因", rs.getString("suspension_reason")),
                        field("makeupSuggestion", "补课建议", rs.getString("makeup_suggestion")),
                        field("affectedClass", "受影响班级", rs.getString("affected_class")),
                        field("remarks", "备注", rs.getString("remarks"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> makeupClassApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_name, course_code, makeup_date, makeup_start_time, makeup_end_time,
                               makeup_location, related_suspension_no, makeup_reason, affected_class, notice_plan
                        FROM biz_makeup_class_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("courseCode", "课程编号", rs.getString("course_code")),
                        field("makeupDate", "补课日期", rs.getDate("makeup_date") == null ? null : rs.getDate("makeup_date").toString()),
                        field("makeupStartTime", "补课开始时间", formatDateTime(rs.getTimestamp("makeup_start_time"))),
                        field("makeupEndTime", "补课结束时间", formatDateTime(rs.getTimestamp("makeup_end_time"))),
                        field("makeupLocation", "补课地点", rs.getString("makeup_location")),
                        field("relatedSuspensionNo", "关联停课单号", rs.getString("related_suspension_no")),
                        field("makeupReason", "补课原因", rs.getString("makeup_reason")),
                        field("affectedClass", "受影响班级", rs.getString("affected_class")),
                        field("noticePlan", "通知方案", rs.getString("notice_plan"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> researchMidtermCheckFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT project_name, project_no, progress_rate, stage_outcome, existing_problems,
                               correction_plan, budget_usage, remarks
                        FROM biz_research_midterm_check
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("projectName", "课题名称", rs.getString("project_name")),
                        field("projectNo", "课题编号", rs.getString("project_no")),
                        field("progressRate", "执行进度", rs.getString("progress_rate")),
                        field("stageOutcome", "阶段成果", rs.getString("stage_outcome")),
                        field("existingProblems", "存在问题", rs.getString("existing_problems")),
                        field("correctionPlan", "整改计划", rs.getString("correction_plan")),
                        field("budgetUsage", "经费使用说明", rs.getString("budget_usage")),
                        field("remarks", "备注", rs.getString("remarks"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> researchCompletionApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT project_name, project_no, achievements, funding_usage, completion_report,
                               expert_list, conclusion_note
                        FROM biz_research_completion_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("projectName", "课题名称", rs.getString("project_name")),
                        field("projectNo", "课题编号", rs.getString("project_no")),
                        field("achievements", "成果列表", rs.getString("achievements")),
                        field("fundingUsage", "经费说明", rs.getString("funding_usage")),
                        field("completionReport", "结题报告", rs.getString("completion_report")),
                        field("expertList", "专家名单", rs.getString("expert_list")),
                        field("conclusionNote", "结论说明", rs.getString("conclusion_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private Long insertLessonPlanSubmission(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseName = requiredString(payload, "courseName", "courseName");
        String semesterCode = requiredString(payload, "semesterCode", "semesterCode");
        String chapterRange = requiredString(payload, "chapterRange", "chapterRange");
        String versionNo = requiredString(payload, "versionNo", "versionNo");
        String lessonPlanTitle = requiredString(payload, "lessonPlanTitle", "lessonPlanTitle");
        String lessonPlanSummary = requiredString(payload, "lessonPlanSummary", "lessonPlanSummary");

        jdbcTemplate.update("""
                        INSERT INTO biz_lesson_plan_submission (
                            workflow_application_id, teacher_id, course_name, semester_code, chapter_range,
                            version_no, lesson_plan_title, lesson_plan_summary, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseName, semesterCode, chapterRange,
                versionNo, lessonPlanTitle, lessonPlanSummary, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertTeachingOutlineSubmission(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseName = requiredString(payload, "courseName", "courseName");
        String academicYear = requiredString(payload, "academicYear", "academicYear");
        String targetMajor = requiredString(payload, "targetMajor", "targetMajor");
        String versionNo = requiredString(payload, "versionNo", "versionNo");
        String revisionNote = requiredString(payload, "revisionNote", "revisionNote");
        String outlineSummary = requiredString(payload, "outlineSummary", "outlineSummary");

        jdbcTemplate.update("""
                        INSERT INTO biz_teaching_outline_submission (
                            workflow_application_id, teacher_id, course_name, academic_year, target_major,
                            version_no, revision_note, outline_summary, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseName, academicYear, targetMajor,
                versionNo, revisionNote, outlineSummary, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertGradeCorrectionApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseName = requiredString(payload, "courseName", "courseName");
        String courseCode = requiredString(payload, "courseCode", "courseCode");
        String studentNo = requiredString(payload, "studentNo", "studentNo");
        String studentName = requiredString(payload, "studentName", "studentName");
        String originalGrade = requiredString(payload, "originalGrade", "originalGrade");
        String newGrade = requiredString(payload, "newGrade", "newGrade");
        String correctionReason = requiredString(payload, "correctionReason", "correctionReason");
        String proofMaterials = requiredString(payload, "proofMaterials", "proofMaterials");

        jdbcTemplate.update("""
                        INSERT INTO biz_grade_correction_request (
                            workflow_application_id, teacher_id, course_name, course_code, student_no, student_name,
                            original_grade, new_grade, correction_reason, proof_materials, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseName, courseCode, studentNo, studentName,
                originalGrade, newGrade, correctionReason, proofMaterials, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertExamScheduleApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String courseName = requiredString(payload, "courseName", "courseName");
        String className = requiredString(payload, "className", "className");
        Integer examCount = requiredInteger(payload, "examCount", "examCount");
        String examTimeSuggestion = requiredString(payload, "examTimeSuggestion", "examTimeSuggestion");
        String classroomNeed = requiredString(payload, "classroomNeed", "classroomNeed");
        String invigilatorNeed = requiredString(payload, "invigilatorNeed", "invigilatorNeed");
        String remarks = requiredString(payload, "remarks", "remarks");

        jdbcTemplate.update("""
                        INSERT INTO biz_exam_schedule_request (
                            workflow_application_id, applicant_id, course_name, class_name, exam_count,
                            exam_time_suggestion, classroom_need, invigilator_need, remarks,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), courseName, className, examCount,
                examTimeSuggestion, classroomNeed, invigilatorNeed, remarks, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertClassroomBorrowApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String classroomName = requiredString(payload, "classroomName", "classroomName");
        String borrowDate = requiredString(payload, "borrowDate", "borrowDate");
        String borrowStartTime = requiredString(payload, "borrowStartTime", "borrowStartTime");
        String borrowEndTime = requiredString(payload, "borrowEndTime", "borrowEndTime");
        String borrowPurpose = requiredString(payload, "borrowPurpose", "borrowPurpose");
        Integer attendeeCount = requiredInteger(payload, "attendeeCount", "attendeeCount");
        String equipmentNeeds = requiredString(payload, "equipmentNeeds", "equipmentNeeds");
        String contactName = requiredString(payload, "contactName", "contactName");
        String contactPhone = requiredString(payload, "contactPhone", "contactPhone");

        jdbcTemplate.update("""
                        INSERT INTO biz_classroom_borrow_request (
                            workflow_application_id, applicant_id, classroom_name, borrow_date, borrow_start_time,
                            borrow_end_time, borrow_purpose, attendee_count, equipment_needs, contact_name,
                            contact_phone, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), classroomName, borrowDate, borrowStartTime,
                borrowEndTime, borrowPurpose, attendeeCount, equipmentNeeds, contactName,
                contactPhone, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertStudentLeaveApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String leaveType = requiredString(payload, "leaveType", "leaveType");
        LocalDateTime leaveStartTime = requiredDateTime(payload, "leaveStartTime", "leaveStartTime");
        LocalDateTime leaveEndTime = requiredDateTime(payload, "leaveEndTime", "leaveEndTime");
        String leaveDestination = requiredString(payload, "leaveDestination", "leaveDestination");
        String leaveReason = requiredString(payload, "leaveReason", "leaveReason");
        String emergencyContact = requiredString(payload, "emergencyContact", "emergencyContact");
        String emergencyPhone = requiredString(payload, "emergencyPhone", "emergencyPhone");
        String returnPlan = requiredString(payload, "returnPlan", "returnPlan");

        if (!leaveEndTime.isAfter(leaveStartTime)) {
            throw new BusinessException("leaveEndTime必须晚于leaveStartTime");
        }
        ensureNoActiveStudentLeaveOverlap(currentUser.userId(), leaveStartTime, leaveEndTime);

        jdbcTemplate.update("""
                        INSERT INTO biz_student_leave_application (
                            workflow_application_id, student_id, leave_type, leave_start_time, leave_end_time,
                            leave_destination, leave_reason, emergency_contact, emergency_phone, return_plan,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), leaveType,
                Timestamp.valueOf(leaveStartTime), Timestamp.valueOf(leaveEndTime),
                leaveDestination, leaveReason, emergencyContact, emergencyPhone, returnPlan,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertStudentReturnConfirmation(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String relatedLeaveNo = requiredString(payload, "relatedLeaveNo", "relatedLeaveNo");
        LocalDateTime returnTime = requiredDateTime(payload, "returnTime", "returnTime");
        String returnNote = requiredString(payload, "returnNote", "returnNote");
        validateStudentReturnConfirmation(currentUser.userId(), relatedLeaveNo, returnTime);

        jdbcTemplate.update("""
                        INSERT INTO biz_student_return_confirmation (
                            workflow_application_id, student_id, related_leave_no, return_time, return_note,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), relatedLeaveNo, Timestamp.valueOf(returnTime), returnNote,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertGraduationProjectOpening(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String projectName = requiredString(payload, "projectName", "projectName");
        String topicDirection = requiredString(payload, "topicDirection", "topicDirection");
        String advisorName = requiredString(payload, "advisorName", "advisorName");
        LocalDate openingDate = requiredDate(payload, "openingDate", "openingDate");
        String openingSummary = requiredString(payload, "openingSummary", "openingSummary");
        ensureTeacherExistsByRealName(advisorName);
        ensureNoActiveGraduationOpening(currentUser.userId(), projectName);

        jdbcTemplate.update("""
                        INSERT INTO biz_graduation_project_opening (
                            workflow_application_id, student_id, project_name, topic_direction, advisor_name,
                            opening_date, opening_summary, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), projectName, topicDirection, advisorName,
                Date.valueOf(openingDate), openingSummary, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertGraduationProjectMidterm(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String projectName = requiredString(payload, "projectName", "projectName");
        Integer progressRate = requiredInteger(payload, "progressRate", "progressRate");
        LocalDate midtermDate = requiredDate(payload, "midtermDate", "midtermDate");
        String problemsFound = requiredString(payload, "problemsFound", "problemsFound");
        String rectificationPlan = requiredString(payload, "rectificationPlan", "rectificationPlan");
        String midtermSummary = requiredString(payload, "midtermSummary", "midtermSummary");
        if (progressRate < 0 || progressRate > 100) {
            throw new BusinessException("progressRate必须在0到100之间");
        }
        ensureApprovedGraduationOpening(currentUser.userId(), projectName, midtermDate);
        ensureNoActiveGraduationMidterm(currentUser.userId(), projectName);

        jdbcTemplate.update("""
                        INSERT INTO biz_graduation_project_midterm (
                            workflow_application_id, student_id, project_name, progress_rate, midterm_date,
                            problems_found, rectification_plan, midterm_summary, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), projectName, progressRate, Date.valueOf(midtermDate),
                problemsFound, rectificationPlan, midtermSummary, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertResearchAchievementRegistration(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String achievementName = requiredString(payload, "achievementName", "achievementName");
        String achievementType = requiredString(payload, "achievementType", "achievementType");
        LocalDate publishTime = requiredDate(payload, "publishTime", "publishTime");
        String issueUnit = requiredString(payload, "issueUnit", "issueUnit");
        String achievementLevel = requiredString(payload, "achievementLevel", "achievementLevel");
        String achievementSummary = requiredString(payload, "achievementSummary", "achievementSummary");

        jdbcTemplate.update("""
                        INSERT INTO biz_research_achievement_registration (
                            workflow_application_id, teacher_id, achievement_name, achievement_type, publish_time,
                            issue_unit, achievement_level, achievement_summary, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), achievementName, achievementType, Date.valueOf(publishTime),
                issueUnit, achievementLevel, achievementSummary, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertAcademicLectureApplication(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String lectureTopic = requiredString(payload, "lectureTopic", "lectureTopic");
        String speakerName = requiredString(payload, "speakerName", "speakerName");
        LocalDateTime lectureTime = requiredDateTime(payload, "lectureTime", "lectureTime");
        String lectureLocation = requiredString(payload, "lectureLocation", "lectureLocation");
        String audienceScope = requiredString(payload, "audienceScope", "audienceScope");
        BigDecimal budgetAmount = requiredDecimal(payload, "budgetAmount", "budgetAmount");
        String attachmentNote = requiredString(payload, "attachmentNote", "attachmentNote");
        if (budgetAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("budgetAmount不能小于0");
        }

        jdbcTemplate.update("""
                        INSERT INTO biz_academic_lecture_application (
                            workflow_application_id, applicant_id, lecture_topic, speaker_name, lecture_time,
                            lecture_location, audience_scope, budget_amount, attachment_note, business_status,
                            flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), lectureTopic, speakerName, Timestamp.valueOf(lectureTime),
                lectureLocation, audienceScope, budgetAmount, attachmentNote, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertOfficeSupplyRequest(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String itemName = requiredString(payload, "itemName", "itemName");
        Integer requestQuantity = requiredInteger(payload, "requestQuantity", "requestQuantity");
        String usagePurpose = requiredString(payload, "usagePurpose", "usagePurpose");
        String departmentName = requiredString(payload, "departmentName", "departmentName");
        String remarks = requiredString(payload, "remarks", "remarks");
        if (requestQuantity <= 0) {
            throw new BusinessException("requestQuantity必须大于0");
        }

        jdbcTemplate.update("""
                        INSERT INTO biz_office_supply_request (
                            workflow_application_id, applicant_id, item_name, request_quantity, usage_purpose,
                            department_name, remarks, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), itemName, requestQuantity, usagePurpose,
                departmentName, remarks, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertLabSafetyHazardReport(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String labName = requiredString(payload, "labName", "labName");
        String hazardType = requiredString(payload, "hazardType", "hazardType");
        String hazardDescription = requiredString(payload, "hazardDescription", "hazardDescription");
        String riskLevel = requiredString(payload, "riskLevel", "riskLevel");
        String rectificationRequirement = requiredString(payload, "rectificationRequirement", "rectificationRequirement");
        String attachmentNote = requiredString(payload, "attachmentNote", "attachmentNote");

        jdbcTemplate.update("""
                        INSERT INTO biz_lab_safety_hazard_report (
                            workflow_application_id, reporter_id, lab_name, hazard_type, hazard_description,
                            risk_level, rectification_requirement, attachment_note, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), labName, hazardType, hazardDescription,
                riskLevel, rectificationRequirement, attachmentNote, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertDormAdjustmentRequest(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String studentInfo = requiredString(payload, "studentInfo", "studentInfo");
        String currentDormitory = requiredString(payload, "currentDormitory", "currentDormitory");
        String targetDormitory = requiredString(payload, "targetDormitory", "targetDormitory");
        String adjustmentReason = requiredString(payload, "adjustmentReason", "adjustmentReason");
        String remarks = requiredString(payload, "remarks", "remarks");

        jdbcTemplate.update("""
                        INSERT INTO biz_dorm_adjustment_request (
                            workflow_application_id, student_id, student_info, current_dormitory, target_dormitory,
                            adjustment_reason, remarks, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), studentInfo, currentDormitory, targetDormitory,
                adjustmentReason, remarks, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertStampRequest(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String requestSubject = requiredString(payload, "requestSubject", "requestSubject");
        String sealType = requiredString(payload, "sealType", "sealType");
        LocalDateTime usageTime = requiredDateTime(payload, "usageTime", "usageTime");
        String documentName = requiredString(payload, "documentName", "documentName");
        Integer documentCount = requiredInteger(payload, "documentCount", "documentCount");
        String attachmentNote = requiredString(payload, "attachmentNote", "attachmentNote");
        if (documentCount <= 0) {
            throw new BusinessException("documentCount必须大于0");
        }

        jdbcTemplate.update("""
                        INSERT INTO biz_stamp_request (
                            workflow_application_id, applicant_id, request_subject, seal_type, usage_time,
                            document_name, document_count, attachment_note, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), requestSubject, sealType, Timestamp.valueOf(usageTime),
                documentName, documentCount, attachmentNote, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertVehicleRequest(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        LocalDateTime useStartTime = requiredDateTime(payload, "useStartTime", "useStartTime");
        LocalDateTime useEndTime = requiredDateTime(payload, "useEndTime", "useEndTime");
        String destination = requiredString(payload, "destination", "destination");
        Integer passengerCount = requiredInteger(payload, "passengerCount", "passengerCount");
        String useReason = requiredString(payload, "useReason", "useReason");
        String contactName = requiredString(payload, "contactName", "contactName");
        String contactPhone = requiredString(payload, "contactPhone", "contactPhone");
        String dispatchRequirement = requiredString(payload, "dispatchRequirement", "dispatchRequirement");
        if (!useEndTime.isAfter(useStartTime)) {
            throw new BusinessException("useEndTime必须晚于useStartTime");
        }
        if (passengerCount <= 0) {
            throw new BusinessException("passengerCount必须大于0");
        }

        jdbcTemplate.update("""
                        INSERT INTO biz_vehicle_request (
                            workflow_application_id, applicant_id, use_start_time, use_end_time, destination,
                            passenger_count, use_reason, contact_name, contact_phone, dispatch_requirement,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), Timestamp.valueOf(useStartTime), Timestamp.valueOf(useEndTime),
                destination, passengerCount, useReason, contactName, contactPhone, dispatchRequirement,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertClassNoticeReceipt(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String targetClass = requiredString(payload, "targetClass", "targetClass");
        String noticeTitle = requiredString(payload, "noticeTitle", "noticeTitle");
        String noticeContent = requiredString(payload, "noticeContent", "noticeContent");
        LocalDateTime deadlineTime = requiredDateTime(payload, "deadlineTime", "deadlineTime");
        String receiptRequiredValue = requiredString(payload, "receiptRequired", "receiptRequired");
        Integer expectedReceiptCount = requiredInteger(payload, "expectedReceiptCount", "expectedReceiptCount");
        Integer receivedReceiptCount = requiredInteger(payload, "receivedReceiptCount", "receivedReceiptCount");
        String attachmentNote = requiredString(payload, "attachmentNote", "attachmentNote");
        if (expectedReceiptCount < 0 || receivedReceiptCount < 0 || receivedReceiptCount > expectedReceiptCount) {
            throw new BusinessException("回执人数填写不正确");
        }

        jdbcTemplate.update("""
                        INSERT INTO biz_class_notice_receipt (
                            workflow_application_id, publisher_id, target_class, notice_title, notice_content,
                            deadline_time, receipt_required, expected_receipt_count, received_receipt_count,
                            attachment_note, business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), targetClass, noticeTitle, noticeContent,
                Timestamp.valueOf(deadlineTime), booleanFlag(receiptRequiredValue), expectedReceiptCount,
                receivedReceiptCount, attachmentNote, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertStudentWarningProcess(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String studentNo = requiredString(payload, "studentNo", "studentNo");
        String studentName = requiredString(payload, "studentName", "studentName");
        String warningType = requiredString(payload, "warningType", "warningType");
        String warningLevel = requiredString(payload, "warningLevel", "warningLevel");
        String triggerReason = requiredString(payload, "triggerReason", "triggerReason");
        String processRecord = requiredString(payload, "processRecord", "processRecord");
        String followUpPlan = requiredString(payload, "followUpPlan", "followUpPlan");
        String attachmentNote = requiredString(payload, "attachmentNote", "attachmentNote");

        jdbcTemplate.update("""
                        INSERT INTO biz_student_warning_process (
                            workflow_application_id, handler_id, student_no, student_name, warning_type,
                            warning_level, trigger_reason, process_record, follow_up_plan, attachment_note,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), studentNo, studentName, warningType, warningLevel,
                triggerReason, processRecord, followUpPlan, attachmentNote, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertMaterialSupplement(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String relatedBusinessNo = requiredString(payload, "relatedBusinessNo", "relatedBusinessNo");
        String returnReason = requiredString(payload, "returnReason", "returnReason");
        String supplementDescription = requiredString(payload, "supplementDescription", "supplementDescription");
        String supplementMaterialNote = requiredString(payload, "supplementMaterialNote", "supplementMaterialNote");
        String originalReviewer = requiredString(payload, "originalReviewer", "originalReviewer");

        jdbcTemplate.update("""
                        INSERT INTO biz_material_supplement (
                            workflow_application_id, student_id, related_business_no, return_reason,
                            supplement_description, supplement_material_note, original_reviewer,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), relatedBusinessNo, returnReason,
                supplementDescription, supplementMaterialNote, originalReviewer, "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private Long insertAnnouncementPublish(AuthenticatedUser currentUser, Long workflowApplicationId, Map<String, Object> payload) {
        String announcementTitle = requiredString(payload, "announcementTitle", "announcementTitle");
        String announcementContent = requiredString(payload, "announcementContent", "announcementContent");
        String publishScope = requiredString(payload, "publishScope", "publishScope");
        LocalDateTime plannedPublishTime = requiredDateTime(payload, "plannedPublishTime", "plannedPublishTime");
        String topFlagValue = requiredString(payload, "topFlag", "topFlag");
        String attachmentNote = requiredString(payload, "attachmentNote", "attachmentNote");

        jdbcTemplate.update("""
                        INSERT INTO biz_announcement_publish (
                            workflow_application_id, publisher_id, announcement_title, announcement_content,
                            publish_scope, planned_publish_time, top_flag, attachment_note,
                            business_status, flow_status, created_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                workflowApplicationId, currentUser.userId(), announcementTitle, announcementContent, publishScope,
                Timestamp.valueOf(plannedPublishTime), booleanFlag(topFlagValue), attachmentNote,
                "DRAFT", "DRAFT", currentUser.userId()
        );
        return queryLastInsertId();
    }

    private List<BusinessFieldValue> lessonPlanSubmissionFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_name, semester_code, chapter_range, version_no, lesson_plan_title, lesson_plan_summary
                        FROM biz_lesson_plan_submission
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("semesterCode", "学期", rs.getString("semester_code")),
                        field("chapterRange", "章节范围", rs.getString("chapter_range")),
                        field("versionNo", "版本号", rs.getString("version_no")),
                        field("lessonPlanTitle", "教案标题", rs.getString("lesson_plan_title")),
                        field("lessonPlanSummary", "教案说明", rs.getString("lesson_plan_summary"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> teachingOutlineSubmissionFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_name, academic_year, target_major, version_no, revision_note, outline_summary
                        FROM biz_teaching_outline_submission
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("academicYear", "学年", rs.getString("academic_year")),
                        field("targetMajor", "适用专业", rs.getString("target_major")),
                        field("versionNo", "版本号", rs.getString("version_no")),
                        field("revisionNote", "修订说明", rs.getString("revision_note")),
                        field("outlineSummary", "教学大纲说明", rs.getString("outline_summary"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> gradeCorrectionApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_name, course_code, student_no, student_name, original_grade, new_grade,
                               correction_reason, proof_materials
                        FROM biz_grade_correction_request
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("courseCode", "课程编号", rs.getString("course_code")),
                        field("studentNo", "学号", rs.getString("student_no")),
                        field("studentName", "学生姓名", rs.getString("student_name")),
                        field("originalGrade", "原成绩", rs.getString("original_grade")),
                        field("newGrade", "新成绩", rs.getString("new_grade")),
                        field("correctionReason", "更正原因", rs.getString("correction_reason")),
                        field("proofMaterials", "证明材料", rs.getString("proof_materials"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> examScheduleApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT course_name, class_name, exam_count, exam_time_suggestion, classroom_need,
                               invigilator_need, remarks
                        FROM biz_exam_schedule_request
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("courseName", "课程名称", rs.getString("course_name")),
                        field("className", "班级", rs.getString("class_name")),
                        field("examCount", "考试人数", rs.getString("exam_count")),
                        field("examTimeSuggestion", "考试时间建议", rs.getString("exam_time_suggestion")),
                        field("classroomNeed", "教室需求", rs.getString("classroom_need")),
                        field("invigilatorNeed", "监考需求", rs.getString("invigilator_need")),
                        field("remarks", "备注", rs.getString("remarks"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> classroomBorrowApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT classroom_name, borrow_date, borrow_start_time, borrow_end_time, borrow_purpose,
                               attendee_count, equipment_needs, contact_name, contact_phone
                        FROM biz_classroom_borrow_request
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("classroomName", "教室名称", rs.getString("classroom_name")),
                        field("borrowDate", "借用日期", rs.getDate("borrow_date") == null ? null : rs.getDate("borrow_date").toString()),
                        field("borrowStartTime", "借用开始时间", formatDateTime(rs.getTimestamp("borrow_start_time"))),
                        field("borrowEndTime", "借用结束时间", formatDateTime(rs.getTimestamp("borrow_end_time"))),
                        field("borrowPurpose", "借用用途", rs.getString("borrow_purpose")),
                        field("attendeeCount", "预计人数", rs.getString("attendee_count")),
                        field("equipmentNeeds", "设备需求", rs.getString("equipment_needs")),
                        field("contactName", "联系人", rs.getString("contact_name")),
                        field("contactPhone", "联系电话", rs.getString("contact_phone"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> studentLeaveApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT leave_type, leave_start_time, leave_end_time, leave_destination, leave_reason,
                               emergency_contact, emergency_phone, return_plan
                        FROM biz_student_leave_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("leaveType", "离校类型", rs.getString("leave_type")),
                        field("leaveStartTime", "离校开始时间", formatDateTime(rs.getTimestamp("leave_start_time"))),
                        field("leaveEndTime", "离校结束时间", formatDateTime(rs.getTimestamp("leave_end_time"))),
                        field("leaveDestination", "离校去向", rs.getString("leave_destination")),
                        field("leaveReason", "离校原因", rs.getString("leave_reason")),
                        field("emergencyContact", "紧急联系人", rs.getString("emergency_contact")),
                        field("emergencyPhone", "紧急联系电话", rs.getString("emergency_phone")),
                        field("returnPlan", "返校计划", rs.getString("return_plan"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> studentReturnConfirmationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT related_leave_no, return_time, return_note
                        FROM biz_student_return_confirmation
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("relatedLeaveNo", "关联离校单号", rs.getString("related_leave_no")),
                        field("returnTime", "返校时间", formatDateTime(rs.getTimestamp("return_time"))),
                        field("returnNote", "返校说明", rs.getString("return_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> graduationProjectOpeningFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT project_name, topic_direction, advisor_name, opening_date, opening_summary
                        FROM biz_graduation_project_opening
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("projectName", "课题名称", rs.getString("project_name")),
                        field("topicDirection", "研究方向", rs.getString("topic_direction")),
                        field("advisorName", "指导老师", rs.getString("advisor_name")),
                        field("openingDate", "开题日期", rs.getString("opening_date")),
                        field("openingSummary", "开题说明", rs.getString("opening_summary"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> graduationProjectMidtermFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT project_name, progress_rate, midterm_date, problems_found, rectification_plan, midterm_summary
                        FROM biz_graduation_project_midterm
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("projectName", "课题名称", rs.getString("project_name")),
                        field("progressRate", "进度比例", rs.getString("progress_rate")),
                        field("midtermDate", "检查日期", rs.getString("midterm_date")),
                        field("problemsFound", "发现问题", rs.getString("problems_found")),
                        field("rectificationPlan", "整改计划", rs.getString("rectification_plan")),
                        field("midtermSummary", "中期说明", rs.getString("midterm_summary"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> researchAchievementRegistrationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT achievement_name, achievement_type, publish_time, issue_unit, achievement_level, achievement_summary
                        FROM biz_research_achievement_registration
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("achievementName", "成果名称", rs.getString("achievement_name")),
                        field("achievementType", "成果类型", rs.getString("achievement_type")),
                        field("publishTime", "登记日期", rs.getString("publish_time")),
                        field("issueUnit", "发布单位", rs.getString("issue_unit")),
                        field("achievementLevel", "成果级别", rs.getString("achievement_level")),
                        field("achievementSummary", "成果说明", rs.getString("achievement_summary"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> academicLectureApplicationFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT lecture_topic, speaker_name, lecture_time, lecture_location, audience_scope,
                               budget_amount, attachment_note
                        FROM biz_academic_lecture_application
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("lectureTopic", "讲座主题", rs.getString("lecture_topic")),
                        field("speakerName", "主讲人", rs.getString("speaker_name")),
                        field("lectureTime", "讲座时间", formatDateTime(rs.getTimestamp("lecture_time"))),
                        field("lectureLocation", "讲座地点", rs.getString("lecture_location")),
                        field("audienceScope", "听众范围", rs.getString("audience_scope")),
                        field("budgetAmount", "预算金额", rs.getString("budget_amount")),
                        field("attachmentNote", "附件说明", rs.getString("attachment_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("Business record not found"));
    }

    private List<BusinessFieldValue> officeSupplyRequestFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT item_name, request_quantity, usage_purpose, department_name, remarks
                        FROM biz_office_supply_request
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("itemName", "用品名称", rs.getString("item_name")),
                        field("requestQuantity", "申领数量", rs.getString("request_quantity")),
                        field("usagePurpose", "用途说明", rs.getString("usage_purpose")),
                        field("departmentName", "使用部门", rs.getString("department_name")),
                        field("remarks", "备注", rs.getString("remarks"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("Business record not found"));
    }

    private List<BusinessFieldValue> labSafetyHazardReportFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT lab_name, hazard_type, hazard_description, risk_level, rectification_requirement, attachment_note
                        FROM biz_lab_safety_hazard_report
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("labName", "实验室名称", rs.getString("lab_name")),
                        field("hazardType", "隐患类型", rs.getString("hazard_type")),
                        field("hazardDescription", "隐患描述", rs.getString("hazard_description")),
                        field("riskLevel", "风险等级", rs.getString("risk_level")),
                        field("rectificationRequirement", "整改要求", rs.getString("rectification_requirement")),
                        field("attachmentNote", "附件说明", rs.getString("attachment_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("Business record not found"));
    }

    private List<BusinessFieldValue> dormAdjustmentRequestFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT student_info, current_dormitory, target_dormitory, adjustment_reason, remarks
                        FROM biz_dorm_adjustment_request
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("studentInfo", "学生信息", rs.getString("student_info")),
                        field("currentDormitory", "当前宿舍", rs.getString("current_dormitory")),
                        field("targetDormitory", "目标宿舍", rs.getString("target_dormitory")),
                        field("adjustmentReason", "调宿原因", rs.getString("adjustment_reason")),
                        field("remarks", "备注", rs.getString("remarks"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("Business record not found"));
    }

    private List<BusinessFieldValue> stampRequestFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT request_subject, seal_type, usage_time, document_name, document_count, attachment_note
                        FROM biz_stamp_request
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("requestSubject", "用章事项", rs.getString("request_subject")),
                        field("sealType", "印章类型", rs.getString("seal_type")),
                        field("usageTime", "用章时间", formatDateTime(rs.getTimestamp("usage_time"))),
                        field("documentName", "文件名称", rs.getString("document_name")),
                        field("documentCount", "文件份数", rs.getString("document_count")),
                        field("attachmentNote", "附件说明", rs.getString("attachment_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("Business record not found"));
    }

    private List<BusinessFieldValue> vehicleRequestFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT use_start_time, use_end_time, destination, passenger_count, use_reason,
                               contact_name, contact_phone, dispatch_requirement
                        FROM biz_vehicle_request
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("useStartTime", "用车开始时间", formatDateTime(rs.getTimestamp("use_start_time"))),
                        field("useEndTime", "用车结束时间", formatDateTime(rs.getTimestamp("use_end_time"))),
                        field("destination", "目的地", rs.getString("destination")),
                        field("passengerCount", "乘车人数", rs.getString("passenger_count")),
                        field("useReason", "用车事由", rs.getString("use_reason")),
                        field("contactName", "联系人", rs.getString("contact_name")),
                        field("contactPhone", "联系电话", rs.getString("contact_phone")),
                        field("dispatchRequirement", "调度要求", rs.getString("dispatch_requirement"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> classNoticeReceiptFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT target_class, notice_title, notice_content, deadline_time, receipt_required,
                               expected_receipt_count, received_receipt_count, attachment_note
                        FROM biz_class_notice_receipt
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("targetClass", "通知班级", rs.getString("target_class")),
                        field("noticeTitle", "通知标题", rs.getString("notice_title")),
                        field("noticeContent", "通知内容", rs.getString("notice_content")),
                        field("deadlineTime", "回执截止时间", formatDateTime(rs.getTimestamp("deadline_time"))),
                        field("receiptRequired", "是否需要回执", rs.getInt("receipt_required") == 1 ? "是" : "否"),
                        field("expectedReceiptCount", "应回执人数", rs.getString("expected_receipt_count")),
                        field("receivedReceiptCount", "已回执人数", rs.getString("received_receipt_count")),
                        field("attachmentNote", "附件说明", rs.getString("attachment_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> studentWarningProcessFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT student_no, student_name, warning_type, warning_level, trigger_reason,
                               process_record, follow_up_plan, attachment_note
                        FROM biz_student_warning_process
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("studentNo", "学号", rs.getString("student_no")),
                        field("studentName", "学生姓名", rs.getString("student_name")),
                        field("warningType", "预警类型", rs.getString("warning_type")),
                        field("warningLevel", "预警等级", rs.getString("warning_level")),
                        field("triggerReason", "触发原因", rs.getString("trigger_reason")),
                        field("processRecord", "处理记录", rs.getString("process_record")),
                        field("followUpPlan", "跟进计划", rs.getString("follow_up_plan")),
                        field("attachmentNote", "附件说明", rs.getString("attachment_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> materialSupplementFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT related_business_no, return_reason, supplement_description,
                               supplement_material_note, original_reviewer
                        FROM biz_material_supplement
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("relatedBusinessNo", "关联业务单号", rs.getString("related_business_no")),
                        field("returnReason", "退回原因", rs.getString("return_reason")),
                        field("supplementDescription", "补交说明", rs.getString("supplement_description")),
                        field("supplementMaterialNote", "补交材料说明", rs.getString("supplement_material_note")),
                        field("originalReviewer", "原审批人", rs.getString("original_reviewer"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private List<BusinessFieldValue> announcementPublishFields(Long id) {
        return jdbcTemplate.query("""
                        SELECT announcement_title, announcement_content, publish_scope, planned_publish_time,
                               top_flag, attachment_note
                        FROM biz_announcement_publish
                        WHERE id = ?
                        """,
                (rs, rowNum) -> List.of(
                        field("announcementTitle", "公告标题", rs.getString("announcement_title")),
                        field("announcementContent", "公告内容", rs.getString("announcement_content")),
                        field("publishScope", "发布范围", rs.getString("publish_scope")),
                        field("plannedPublishTime", "计划发布时间", formatDateTime(rs.getTimestamp("planned_publish_time"))),
                        field("topFlag", "是否置顶", rs.getInt("top_flag") == 1 ? "是" : "否"),
                        field("attachmentNote", "附件说明", rs.getString("attachment_note"))
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new BusinessException("业务记录不存在"));
    }

    private Long findWorkflowTypeId(String workflowTypeCode) {
        List<Long> ids = jdbcTemplate.query("""
                        SELECT id
                        FROM wf_application_type
                        WHERE type_code = ? AND status = 1
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                workflowTypeCode
        );
        if (ids.isEmpty()) {
            throw new BusinessException("流程类型未配置");
        }
        return ids.get(0);
    }

    private void validateCreator(AuthenticatedUser currentUser, BusinessDefinition definition) {
        boolean allowed = switch (definition.key()) {
            case "internship-materials", "leave-applications", "leave-cancellations", "dorm-repairs",
                 "scholarship-applications", "grant-applications", "difficulty-recognitions",
                 "enrollment-certificates" -> currentUser.roles().contains("STUDENT");
            case "abnormal-students" -> currentUser.roles().contains("ADVISER");
            case "research-projects", "course-standards", "schedule-adjustments", "asset-repairs",
                 "textbook-orders", "course-suspension-applications", "makeup-class-applications",
                 "research-midterm-checks", "research-completion-applications" -> currentUser.roles().contains("TEACHER");
            case "lesson-plan-submissions", "teaching-outline-submissions", "grade-correction-applications",
                 "exam-schedule-applications", "classroom-borrow-applications" ->
                    currentUser.roles().stream().anyMatch(List.of("TEACHER", "OFFICE")::contains);
            case "student-leave-applications", "student-return-confirmations", "graduation-project-openings", "graduation-project-midterms" ->
                    currentUser.roles().contains("STUDENT");
            case "research-achievement-registrations", "academic-lecture-applications", "office-supply-requests",
                 "lab-safety-hazard-reports", "stamp-requests", "vehicle-requests",
                 "announcement-publishes" -> currentUser.roles().contains("TEACHER");
            case "dorm-adjustment-requests" -> currentUser.roles().contains("STUDENT");
            case "class-notice-receipts", "student-warning-processes" -> currentUser.roles().contains("ADVISER");
            case "material-supplements" -> currentUser.roles().contains("STUDENT");
            case "goods-borrow-applications" -> currentUser.roles().stream().anyMatch(List.of("STUDENT", "TEACHER")::contains);
            case "meeting-rooms" -> currentUser.roles().stream().anyMatch(List.of("TEACHER", "ADVISER", "OFFICE", "RESEARCH")::contains);
            default -> false;
        };
        if (!allowed && !isAdmin(currentUser)) {
            throw new BusinessException("当前角色不能发起该业务");
        }
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return currentUser.roles().contains("ADMIN") || "ADMIN".equalsIgnoreCase(currentUser.userType());
    }

    private boolean isPublishedReadableBusiness(AuthenticatedUser currentUser, BusinessDefinition definition) {
        return List.of("class-notice-receipts", "announcement-publishes").contains(definition.key())
                && currentUser.roles().stream().anyMatch(definition.viewerRoles()::contains);
    }

    private BusinessDefinition requireDefinition(String businessKey) {
        return switch (businessKey) {
            case "internship-materials" -> new BusinessDefinition(
                    "internship-materials", "实习材料", "biz_internship_material", "INTERNSHIP_MATERIAL",
                    List.of("ADVISER"),
                    "CONCAT(material_title, ' - ', internship_company)",
                    "CONCAT(material_title, ' - ', internship_company)"
            );
            case "abnormal-students" -> new BusinessDefinition(
                    "abnormal-students", "异常学生", "biz_student_alert", "ABNORMAL_STUDENT_CASE",
                    List.of("ADVISER"),
                    "student_name",
                    "student_name"
            );
            case "research-projects" -> new BusinessDefinition(
                    "research-projects", "课题申报", "biz_project_application", "RESEARCH_PROJECT_REVIEW",
                    List.of("RESEARCH"),
                    "project_name",
                    "project_name"
            );
            case "course-standards" -> new BusinessDefinition(
                    "course-standards", "课程标准", "biz_course_standard_review", "COURSE_STANDARD_REVIEW",
                    List.of("OFFICE"),
                    "course_name",
                    "course_name"
            );
            case "leave-applications" -> new BusinessDefinition(
                    "leave-applications", "请假申请", "biz_leave_request", "LEAVE_APPLICATION",
                    List.of("ADVISER"),
                    "leave_type",
                    "leave_type"
            );
            case "leave-cancellations" -> new BusinessDefinition(
                    "leave-cancellations", "销假申请", "biz_leave_cancellation", "LEAVE_CANCELLATION",
                    List.of("ADVISER"),
                    "related_leave_no",
                    "related_leave_no"
            );
            case "schedule-adjustments" -> new BusinessDefinition(
                    "schedule-adjustments", "调课申请", "biz_schedule_adjustment", "SCHEDULE_ADJUSTMENT",
                    List.of("OFFICE"),
                    "course_name",
                    "course_name"
            );
            case "meeting-rooms" -> new BusinessDefinition(
                    "meeting-rooms", "会议室预约", "biz_meeting_room_booking", "MEETING_ROOM_BOOKING",
                    List.of("TEACHER", "ADVISER", "OFFICE", "RESEARCH"),
                    "meeting_subject",
                    "meeting_subject"
            );
            case "dorm-repairs" -> new BusinessDefinition(
                    "dorm-repairs", "宿舍维修", "biz_dorm_repair", "DORM_REPAIR",
                    List.of("ADMIN"),
                    "CONCAT(dorm_building, ' ', room_no)",
                    "CONCAT(dorm_building, ' ', room_no)"
            );
            case "asset-repairs" -> new BusinessDefinition(
                    "asset-repairs", "资产报修", "biz_asset_repair", "ASSET_REPAIR",
                    List.of("TEACHER"),
                    "asset_name",
                    "asset_name"
            );
            case "scholarship-applications" -> new BusinessDefinition(
                    "scholarship-applications", "奖学金申请", "biz_scholarship_application", "SCHOLARSHIP_APPLICATION",
                    List.of("STUDENT_AFFAIRS"),
                    "scholarship_type",
                    "scholarship_type"
            );
            case "grant-applications" -> new BusinessDefinition(
                    "grant-applications", "助学金申请", "biz_grant_application", "GRANT_APPLICATION",
                    List.of("STUDENT_AFFAIRS"),
                    "difficulty_level",
                    "difficulty_level"
            );
            case "difficulty-recognitions" -> new BusinessDefinition(
                    "difficulty-recognitions", "困难认定", "biz_difficulty_recognition", "DIFFICULTY_RECOGNITION",
                    List.of("STUDENT_AFFAIRS"),
                    "recognition_level",
                    "recognition_level"
            );
            case "enrollment-certificates" -> new BusinessDefinition(
                    "enrollment-certificates", "在读证明", "biz_enrollment_certificate", "ENROLLMENT_CERTIFICATE",
                    List.of("ADVISER"),
                    "certificate_purpose",
                    "certificate_purpose"
            );
            case "textbook-orders" -> new BusinessDefinition(
                    "textbook-orders", "教材征订", "biz_textbook_order", "TEXTBOOK_ORDER",
                    List.of("OFFICE"),
                    "course_name",
                    "course_name"
            );
            case "goods-borrow-applications" -> new BusinessDefinition(
                    "goods-borrow-applications", "物资借用", "biz_goods_borrow_application", "GOODS_BORROW_APPLICATION",
                    List.of("ADMIN"),
                    "item_name",
                    "item_name"
            );
            case "course-suspension-applications" -> new BusinessDefinition(
                    "course-suspension-applications", "停课申请", "biz_course_suspension_application", "COURSE_SUSPENSION_APPLICATION",
                    List.of("OFFICE"),
                    "course_name",
                    "course_name"
            );
            case "makeup-class-applications" -> new BusinessDefinition(
                    "makeup-class-applications", "补课申请", "biz_makeup_class_application", "MAKEUP_CLASS_APPLICATION",
                    List.of("OFFICE"),
                    "course_name",
                    "course_name"
            );
            case "research-midterm-checks" -> new BusinessDefinition(
                    "research-midterm-checks", "课题中期检查", "biz_research_midterm_check", "RESEARCH_MIDTERM_CHECK",
                    List.of("RESEARCH"),
                    "project_name",
                    "project_name"
            );
            case "research-completion-applications" -> new BusinessDefinition(
                    "research-completion-applications", "课题结题申请", "biz_research_completion_application", "RESEARCH_COMPLETION_APPLICATION",
                    List.of("RESEARCH"),
                    "project_name",
                    "project_name"
            );
            case "lesson-plan-submissions" -> new BusinessDefinition(
                    "lesson-plan-submissions", "教案提交", "biz_lesson_plan_submission", "LESSON_PLAN_SUBMISSION",
                    List.of("TEACHER", "OFFICE"),
                    "course_name",
                    "course_name"
            );
            case "teaching-outline-submissions" -> new BusinessDefinition(
                    "teaching-outline-submissions", "教学大纲提交", "biz_teaching_outline_submission", "TEACHING_OUTLINE_SUBMISSION",
                    List.of("TEACHER", "OFFICE"),
                    "course_name",
                    "course_name"
            );
            case "grade-correction-applications" -> new BusinessDefinition(
                    "grade-correction-applications", "成绩更正申请", "biz_grade_correction_request", "GRADE_CORRECTION_REQUEST",
                    List.of("TEACHER", "OFFICE"),
                    "CONCAT(course_name, ' - ', student_name)",
                    "CONCAT(course_name, ' - ', student_name)"
            );
            case "exam-schedule-applications" -> new BusinessDefinition(
                    "exam-schedule-applications", "考试安排申请", "biz_exam_schedule_request", "EXAM_SCHEDULE_APPLICATION",
                    List.of("TEACHER", "OFFICE"),
                    "course_name",
                    "course_name"
            );
            case "classroom-borrow-applications" -> new BusinessDefinition(
                    "classroom-borrow-applications", "教室借用申请", "biz_classroom_borrow_request", "CLASSROOM_BORROW_APPLICATION",
                    List.of("TEACHER", "OFFICE"),
                    "classroom_name",
                    "classroom_name"
            );
                        case "student-leave-applications" -> new BusinessDefinition(
                    "student-leave-applications", "学生离校申请", "biz_student_leave_application", "STUDENT_LEAVE_APPLICATION",
                    List.of("STUDENT", "ADVISER", "STUDENT_AFFAIRS", "ADMIN"),
                    "leave_destination",
                    "leave_destination"
            );
            case "student-return-confirmations" -> new BusinessDefinition(
                    "student-return-confirmations", "学生返校确认", "biz_student_return_confirmation", "STUDENT_RETURN_CONFIRMATION",
                    List.of("STUDENT", "ADVISER", "STUDENT_AFFAIRS", "ADMIN"),
                    "related_leave_no",
                    "related_leave_no"
            );
            case "graduation-project-openings" -> new BusinessDefinition(
                    "graduation-project-openings", "毕业设计开题申请", "biz_graduation_project_opening", "GRADUATION_PROJECT_OPENING",
                    List.of("STUDENT", "TEACHER", "ADMIN"),
                    "project_name",
                    "project_name"
            );
            case "graduation-project-midterms" -> new BusinessDefinition(
                    "graduation-project-midterms", "毕业设计中期检查", "biz_graduation_project_midterm", "GRADUATION_PROJECT_MIDTERM",
                    List.of("STUDENT", "TEACHER", "ADMIN"),
                    "project_name",
                    "project_name"
            );
            case "research-achievement-registrations" -> new BusinessDefinition(
                    "research-achievement-registrations", "科研成果登记", "biz_research_achievement_registration", "RESEARCH_ACHIEVEMENT_REGISTRATION",
                    List.of("TEACHER", "RESEARCH", "ADMIN"),
                    "achievement_name",
                    "achievement_name"
            );
            case "academic-lecture-applications" -> new BusinessDefinition(
                    "academic-lecture-applications", "学术讲座申请", "biz_academic_lecture_application", "ACADEMIC_LECTURE_APPLICATION",
                    List.of("TEACHER", "RESEARCH", "ADMIN"),
                    "lecture_topic",
                    "lecture_topic"
            );
            case "office-supply-requests" -> new BusinessDefinition(
                    "office-supply-requests", "办公用品申领", "biz_office_supply_request", "OFFICE_SUPPLY_REQUEST",
                    List.of("TEACHER", "ADMIN"),
                    "item_name",
                    "item_name"
            );
            case "lab-safety-hazard-reports" -> new BusinessDefinition(
                    "lab-safety-hazard-reports", "实验室安全隐患上报", "biz_lab_safety_hazard_report", "LAB_SAFETY_HAZARD_REPORT",
                    List.of("TEACHER", "ADMIN"),
                    "lab_name",
                    "lab_name"
            );
            case "dorm-adjustment-requests" -> new BusinessDefinition(
                    "dorm-adjustment-requests", "宿舍调宿申请", "biz_dorm_adjustment_request", "DORM_ADJUSTMENT_REQUEST",
                    List.of("STUDENT", "ADVISER", "ADMIN"),
                    "target_dormitory",
                    "target_dormitory"
            );
            case "stamp-requests" -> new BusinessDefinition(
                    "stamp-requests", "用章申请", "biz_stamp_request", "STAMP_REQUEST",
                    List.of("TEACHER", "ADMIN"),
                    "request_subject",
                    "request_subject"
            );
            case "vehicle-requests" -> new BusinessDefinition(
                    "vehicle-requests", "车辆申请", "biz_vehicle_request", "VEHICLE_REQUEST",
                    List.of("TEACHER", "ADMIN"),
                    "destination",
                    "destination"
            );
            case "class-notice-receipts" -> new BusinessDefinition(
                    "class-notice-receipts", "班级通知回执", "biz_class_notice_receipt", "CLASS_NOTICE_RECEIPT",
                    List.of("ADVISER", "STUDENT", "ADMIN"),
                    "notice_title",
                    "notice_title"
            );
            case "student-warning-processes" -> new BusinessDefinition(
                    "student-warning-processes", "学生预警处理", "biz_student_warning_process", "STUDENT_WARNING_PROCESS",
                    List.of("ADVISER", "STUDENT_AFFAIRS", "ADMIN"),
                    "CONCAT(student_name, ' - ', warning_type)",
                    "CONCAT(student_name, ' - ', warning_type)"
            );
            case "material-supplements" -> new BusinessDefinition(
                    "material-supplements", "证明材料补交", "biz_material_supplement", "MATERIAL_SUPPLEMENT",
                    List.of("STUDENT", "ADVISER", "ADMIN"),
                    "related_business_no",
                    "related_business_no"
            );
            case "announcement-publishes" -> new BusinessDefinition(
                    "announcement-publishes", "公告发布", "biz_announcement_publish", "ANNOUNCEMENT_PUBLISH",
                    List.of("TEACHER", "ADVISER", "OFFICE", "RESEARCH", "STUDENT_AFFAIRS", "ADMIN"),
                    "announcement_title",
                    "announcement_title"
            );
            default -> throw new BusinessException("不支持的业务类型");
        };
    }

    private String buildTitle(BusinessDefinition definition, Map<String, Object> payload) {
        return switch (definition.key()) {
            case "academic-lecture-applications" -> requiredString(payload, "lectureTopic", "lectureTopic") + "讲座申请";
            case "office-supply-requests" -> requiredString(payload, "itemName", "itemName") + "申领";
            case "lab-safety-hazard-reports" -> requiredString(payload, "labName", "labName") + "隐患上报";
            case "dorm-adjustment-requests" -> requiredString(payload, "targetDormitory", "targetDormitory") + "调宿申请";
            case "stamp-requests" -> requiredString(payload, "requestSubject", "requestSubject") + "用章申请";
            case "vehicle-requests" -> requiredString(payload, "destination", "destination") + "用车申请";
            case "class-notice-receipts" -> requiredString(payload, "noticeTitle", "noticeTitle");
            case "student-warning-processes" -> requiredString(payload, "studentName", "studentName") + "预警处理";
            case "material-supplements" -> requiredString(payload, "relatedBusinessNo", "relatedBusinessNo") + "材料补交";
            case "announcement-publishes" -> requiredString(payload, "announcementTitle", "announcementTitle");
            case "internship-materials" -> requiredString(payload, "materialTitle", "材料标题");
            case "abnormal-students" -> requiredString(payload, "studentName", "学生姓名") + "异常记录";
            case "research-projects" -> requiredString(payload, "projectName", "课题名称");
            case "course-standards" -> requiredString(payload, "courseName", "课程名称");
            case "leave-applications" -> requiredString(payload, "leaveType", "请假类型") + "申请";
            case "leave-cancellations" -> requiredString(payload, "relatedLeaveNo", "关联请假单号") + "销假";
            case "schedule-adjustments" -> requiredString(payload, "courseName", "课程名称") + "调课";
            case "meeting-rooms" -> requiredString(payload, "meetingSubject", "会议主题");
            case "dorm-repairs" -> requiredString(payload, "dormBuilding", "宿舍楼") + " " + requiredString(payload, "roomNo", "房间号") + "维修";
            case "asset-repairs" -> requiredString(payload, "assetName", "资产名称") + "报修";
            case "scholarship-applications" -> requiredString(payload, "scholarshipType", "奖学金类型") + "申请";
            case "grant-applications" -> requiredString(payload, "difficultyLevel", "困难等级") + "申请";
            case "difficulty-recognitions" -> requiredString(payload, "recognitionLevel", "认定等级") + "认定";
            case "enrollment-certificates" -> requiredString(payload, "certificatePurpose", "证明用途") + "证明";
            case "textbook-orders" -> requiredString(payload, "courseName", "课程名称") + "教材征订";
            case "goods-borrow-applications" -> requiredString(payload, "itemName", "物资名称") + "借用";
            case "course-suspension-applications" -> requiredString(payload, "courseName", "课程名称") + "停课申请";
            case "makeup-class-applications" -> requiredString(payload, "courseName", "课程名称") + "补课申请";
            case "research-midterm-checks" -> requiredString(payload, "projectName", "课题名称") + "中期检查";
            case "research-completion-applications" -> requiredString(payload, "projectName", "课题名称") + "结题申请";
            case "lesson-plan-submissions" -> requiredString(payload, "courseName", "课程名称") + "教案";
            case "teaching-outline-submissions" -> requiredString(payload, "courseName", "课程名称") + "教学大纲";
            case "grade-correction-applications" -> requiredString(payload, "courseName", "课程名称") + "成绩更正";
            case "exam-schedule-applications" -> requiredString(payload, "courseName", "课程名称") + "考试安排";
            case "classroom-borrow-applications" -> requiredString(payload, "classroomName", "教室名称") + "借用";
            case "student-leave-applications" -> requiredString(payload, "leaveDestination", "离校去向") + "离校申请";
            case "student-return-confirmations" -> requiredString(payload, "relatedLeaveNo", "关联离校单号") + "返校确认";
            case "graduation-project-openings" -> requiredString(payload, "projectName", "课题名称") + "开题申请";
            case "graduation-project-midterms" -> requiredString(payload, "projectName", "课题名称") + "中期检查";
            case "research-achievement-registrations" -> requiredString(payload, "achievementName", "成果名称");
            default -> throw new BusinessException("不支持的业务类型");
        };
    }

    private String buildContent(BusinessDefinition definition, Map<String, Object> payload) {
        return switch (definition.key()) {
            case "academic-lecture-applications" -> "主讲人：" + requiredString(payload, "speakerName", "speakerName")
                    + "；讲座时间：" + requiredString(payload, "lectureTime", "lectureTime");
            case "office-supply-requests" -> "申领数量：" + requiredString(payload, "requestQuantity", "requestQuantity")
                    + "；使用部门：" + requiredString(payload, "departmentName", "departmentName");
            case "lab-safety-hazard-reports" -> "隐患类型：" + requiredString(payload, "hazardType", "hazardType")
                    + "；风险等级：" + requiredString(payload, "riskLevel", "riskLevel");
            case "dorm-adjustment-requests" -> "当前宿舍：" + requiredString(payload, "currentDormitory", "currentDormitory")
                    + "；目标宿舍：" + requiredString(payload, "targetDormitory", "targetDormitory");
            case "stamp-requests" -> "印章类型：" + requiredString(payload, "sealType", "sealType")
                    + "；用章时间：" + requiredString(payload, "usageTime", "usageTime");
            case "vehicle-requests" -> "目的地：" + requiredString(payload, "destination", "destination")
                    + "；用车时间：" + requiredString(payload, "useStartTime", "useStartTime");
            case "class-notice-receipts" -> "通知班级：" + requiredString(payload, "targetClass", "targetClass")
                    + "；截止时间：" + requiredString(payload, "deadlineTime", "deadlineTime");
            case "student-warning-processes" -> "预警类型：" + requiredString(payload, "warningType", "warningType")
                    + "；预警等级：" + requiredString(payload, "warningLevel", "warningLevel");
            case "material-supplements" -> "关联业务单号：" + requiredString(payload, "relatedBusinessNo", "relatedBusinessNo")
                    + "；原审批人：" + requiredString(payload, "originalReviewer", "originalReviewer");
            case "announcement-publishes" -> "发布范围：" + requiredString(payload, "publishScope", "publishScope")
                    + "；计划发布时间：" + requiredString(payload, "plannedPublishTime", "plannedPublishTime");
            case "internship-materials" -> "实习单位：" + requiredString(payload, "internshipCompany", "实习单位")
                    + "；实习岗位：" + requiredString(payload, "internshipPosition", "实习岗位");
            case "abnormal-students" -> "学生姓名：" + requiredString(payload, "studentName", "学生姓名")
                    + "；异常类型：" + requiredString(payload, "alertType", "异常类型");
            case "research-projects" -> "课题类别：" + requiredString(payload, "projectCategory", "课题类别")
                    + "；申报年度：" + requiredString(payload, "applicationYear", "申报年度");
            case "course-standards" -> "课程编号：" + requiredString(payload, "courseCode", "课程编号")
                    + "；适用专业：" + requiredString(payload, "targetMajor", "适用专业");
            case "leave-applications" -> "开始时间：" + requiredString(payload, "startTime", "开始时间")
                    + "；结束时间：" + requiredString(payload, "endTime", "结束时间");
            case "leave-cancellations" -> "关联请假单号：" + requiredString(payload, "relatedLeaveNo", "关联请假单号")
                    + "；返校时间：" + requiredString(payload, "returnTime", "返校时间");
            case "schedule-adjustments" -> "原上课时间：" + requiredString(payload, "originalTime", "原上课时间")
                    + "；调整后时间：" + requiredString(payload, "adjustedTime", "调整后时间");
            case "meeting-rooms" -> "会议室：" + requiredString(payload, "roomName", "会议室")
                    + "；开始时间：" + requiredString(payload, "startTime", "开始时间");
            case "dorm-repairs" -> "报修类型：" + requiredString(payload, "repairType", "报修类型")
                    + "；紧急程度：" + requiredString(payload, "urgencyLevel", "紧急程度");
            case "asset-repairs" -> "资产编号：" + requiredString(payload, "assetNo", "资产编号")
                    + "；紧急程度：" + requiredString(payload, "urgencyLevel", "紧急程度");
            case "scholarship-applications" -> "奖学金类型：" + requiredString(payload, "scholarshipType", "奖学金类型")
                    + "；成绩排名：" + requiredString(payload, "gradeRank", "成绩排名");
            case "grant-applications" -> "困难等级：" + requiredString(payload, "difficultyLevel", "困难等级")
                    + "；家庭人口：" + requiredString(payload, "householdSize", "家庭人口");
            case "difficulty-recognitions" -> "认定等级：" + requiredString(payload, "recognitionLevel", "认定等级")
                    + "；年收入：" + requiredString(payload, "annualIncome", "年收入");
            case "enrollment-certificates" -> "证明用途：" + requiredString(payload, "certificatePurpose", "证明用途")
                    + "；接收单位：" + requiredString(payload, "receiverOrg", "接收单位");
            case "textbook-orders" -> "教材名称：" + requiredString(payload, "textbookName", "教材名称")
                    + "；征订数量：" + requiredString(payload, "orderQuantity", "征订数量");
            case "goods-borrow-applications" -> "物资名称：" + requiredString(payload, "itemName", "物资名称")
                    + "；借用数量：" + requiredString(payload, "borrowQuantity", "借用数量");
            case "course-suspension-applications" -> "课程名称：" + requiredString(payload, "courseName", "课程名称")
                    + "；停课日期：" + requiredString(payload, "suspensionDate", "停课日期");
            case "makeup-class-applications" -> "课程名称：" + requiredString(payload, "courseName", "课程名称")
                    + "；补课日期：" + requiredString(payload, "makeupDate", "补课日期");
            case "research-midterm-checks" -> "课题名称：" + requiredString(payload, "projectName", "课题名称")
                    + "；执行进度：" + requiredString(payload, "progressRate", "执行进度");
            case "research-completion-applications" -> "课题名称：" + requiredString(payload, "projectName", "课题名称")
                    + "；成果列表：" + requiredString(payload, "achievements", "成果列表");
            case "lesson-plan-submissions" -> "课程名称：" + requiredString(payload, "courseName", "课程名称")
                    + "；学期：" + requiredString(payload, "semesterCode", "学期");
            case "teaching-outline-submissions" -> "课程名称：" + requiredString(payload, "courseName", "课程名称")
                    + "；适用专业：" + requiredString(payload, "targetMajor", "适用专业");
            case "grade-correction-applications" -> "课程名称：" + requiredString(payload, "courseName", "课程名称")
                    + "；学生姓名：" + requiredString(payload, "studentName", "学生姓名")
                    + "；原成绩：" + requiredString(payload, "originalGrade", "原成绩");
            case "exam-schedule-applications" -> "课程名称：" + requiredString(payload, "courseName", "课程名称")
                    + "；班级：" + requiredString(payload, "className", "班级")
                    + "；考试人数：" + requiredString(payload, "examCount", "考试人数");
            case "classroom-borrow-applications" -> "教室名称：" + requiredString(payload, "classroomName", "教室名称")
                    + "；借用日期：" + requiredString(payload, "borrowDate", "借用日期");
            case "student-leave-applications" -> "离校类型：" + requiredString(payload, "leaveType", "离校类型")
                    + "；离校时间：" + requiredString(payload, "leaveStartTime", "离校时间");
            case "student-return-confirmations" -> "关联离校单号：" + requiredString(payload, "relatedLeaveNo", "关联离校单号")
                    + "；返校时间：" + requiredString(payload, "returnTime", "返校时间");
            case "graduation-project-openings" -> "课题名称：" + requiredString(payload, "projectName", "课题名称")
                    + "；指导老师：" + requiredString(payload, "advisorName", "指导老师");
            case "graduation-project-midterms" -> "课题名称：" + requiredString(payload, "projectName", "课题名称")
                    + "；进度比例：" + requiredString(payload, "progressRate", "进度比例");
            case "research-achievement-registrations" -> "成果名称：" + requiredString(payload, "achievementName", "成果名称")
                    + "；成果类型：" + requiredString(payload, "achievementType", "成果类型");
            default -> throw new BusinessException("不支持的业务类型");
        };
    }

    private List<BusinessSelectOption> listSelectableStudentLeaveOptions(Long studentId) {
        return jdbcTemplate.query("""
                        SELECT leave_wf.application_no,
                               sl.leave_destination,
                               sl.leave_start_time,
                               sl.leave_end_time
                        FROM biz_student_leave_application sl
                        JOIN wf_application leave_wf ON leave_wf.id = sl.workflow_application_id
                        WHERE sl.student_id = ?
                          AND leave_wf.status = 'APPROVED'
                          AND NOT EXISTS (
                              SELECT 1
                              FROM biz_student_return_confirmation rc
                              JOIN wf_application return_wf ON return_wf.id = rc.workflow_application_id
                              WHERE rc.student_id = sl.student_id
                                AND rc.related_leave_no = leave_wf.application_no
                                AND return_wf.status IN ('DRAFT', 'PENDING', 'IN_PROGRESS', 'APPROVED')
                          )
                        ORDER BY sl.leave_start_time DESC, sl.id DESC
                        """,
                (rs, rowNum) -> new BusinessSelectOption(
                        rs.getString("application_no"),
                        rs.getString("application_no")
                                + " | "
                                + rs.getString("leave_destination")
                                + " | "
                                + formatDateTime(rs.getTimestamp("leave_start_time"))
                                + " - "
                                + formatDateTime(rs.getTimestamp("leave_end_time"))
                ),
                studentId
        );
    }

    private List<BusinessSelectOption> listSelectableTeacherOptions() {
        return jdbcTemplate.query("""
                        SELECT u.real_name
                        FROM sys_user u
                        JOIN sys_user_role ur ON ur.user_id = u.id
                        JOIN sys_role r ON r.id = ur.role_id
                        WHERE u.status = 1
                          AND r.role_code = 'TEACHER'
                          AND r.status = 1
                        GROUP BY u.real_name
                        HAVING COUNT(1) = 1
                        ORDER BY u.real_name ASC
                        """,
                (rs, rowNum) -> new BusinessSelectOption(
                        rs.getString("real_name"),
                        rs.getString("real_name")
                )
        );
    }

    private List<BusinessSelectOption> listSelectableGraduationOpeningOptions(Long studentId) {
        return jdbcTemplate.query("""
                        SELECT opening.project_name,
                               opening.advisor_name,
                               opening.opening_date
                        FROM biz_graduation_project_opening opening
                        JOIN wf_application wf ON wf.id = opening.workflow_application_id
                        WHERE opening.student_id = ?
                          AND wf.status = 'APPROVED'
                          AND NOT EXISTS (
                              SELECT 1
                              FROM biz_graduation_project_midterm midterm
                              JOIN wf_application midterm_wf ON midterm_wf.id = midterm.workflow_application_id
                              WHERE midterm.student_id = opening.student_id
                                AND midterm.project_name = opening.project_name
                                AND midterm_wf.status IN ('DRAFT', 'PENDING', 'IN_PROGRESS', 'APPROVED')
                          )
                        ORDER BY opening.opening_date DESC, opening.id DESC
                        """,
                (rs, rowNum) -> new BusinessSelectOption(
                        rs.getString("project_name"),
                        rs.getString("project_name")
                                + " | "
                                + rs.getString("advisor_name")
                                + " | 开题日期 "
                                + rs.getDate("opening_date").toLocalDate()
                ),
                studentId
        );
    }

    private void ensureNoActiveStudentLeaveOverlap(Long studentId, LocalDateTime leaveStartTime, LocalDateTime leaveEndTime) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM biz_student_leave_application sl
                        JOIN wf_application leave_wf ON leave_wf.id = sl.workflow_application_id
                        WHERE sl.student_id = ?
                          AND leave_wf.status IN ('PENDING', 'IN_PROGRESS', 'APPROVED')
                          AND sl.leave_start_time < ?
                          AND sl.leave_end_time > ?
                          AND NOT EXISTS (
                              SELECT 1
                              FROM biz_student_return_confirmation rc
                              JOIN wf_application return_wf ON return_wf.id = rc.workflow_application_id
                              WHERE rc.student_id = sl.student_id
                                AND rc.related_leave_no = leave_wf.application_no
                                AND return_wf.status = 'APPROVED'
                          )
                        """,
                Integer.class,
                studentId,
                Timestamp.valueOf(leaveEndTime),
                Timestamp.valueOf(leaveStartTime)
        );
        if (count != null && count > 0) {
            throw new BusinessException("存在未闭环且时间重叠的离校申请");
        }
    }

    private void validateStudentReturnConfirmation(Long studentId, String relatedLeaveNo, LocalDateTime returnTime) {
        StudentLeaveReference leaveReference = findApprovedStudentLeaveReference(studentId, relatedLeaveNo);
        if (leaveReference == null) {
            throw new BusinessException("关联离校单号不存在，或不属于当前学生，或尚未审批通过");
        }
        if (returnTime.isBefore(leaveReference.leaveStartTime())) {
            throw new BusinessException("返校时间不能早于离校开始时间");
        }

        Integer existingCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM biz_student_return_confirmation rc
                        JOIN wf_application wf ON wf.id = rc.workflow_application_id
                        WHERE rc.student_id = ?
                          AND rc.related_leave_no = ?
                          AND wf.status IN ('DRAFT', 'PENDING', 'IN_PROGRESS', 'APPROVED')
                        """,
                Integer.class,
                studentId,
                relatedLeaveNo
        );
        if (existingCount != null && existingCount > 0) {
            throw new BusinessException("该离校申请已存在返校确认记录");
        }
    }

    private StudentLeaveReference findApprovedStudentLeaveReference(Long studentId, String relatedLeaveNo) {
        List<StudentLeaveReference> references = jdbcTemplate.query("""
                        SELECT leave_wf.id AS workflow_application_id,
                               leave_wf.application_no,
                               sl.leave_start_time,
                               sl.leave_end_time
                        FROM biz_student_leave_application sl
                        JOIN wf_application leave_wf ON leave_wf.id = sl.workflow_application_id
                        WHERE sl.student_id = ?
                          AND leave_wf.application_no = ?
                          AND leave_wf.status = 'APPROVED'
                        ORDER BY sl.id DESC
                        LIMIT 1
                        """,
                (rs, rowNum) -> new StudentLeaveReference(
                        rs.getLong("workflow_application_id"),
                        rs.getString("application_no"),
                        rs.getTimestamp("leave_start_time").toLocalDateTime(),
                        rs.getTimestamp("leave_end_time").toLocalDateTime()
                ),
                studentId,
                relatedLeaveNo
        );
        return references.isEmpty() ? null : references.get(0);
    }

    private void ensureTeacherExistsByRealName(String teacherName) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM sys_user u
                        JOIN sys_user_role ur ON ur.user_id = u.id
                        JOIN sys_role r ON r.id = ur.role_id
                        WHERE u.real_name = ?
                          AND u.status = 1
                          AND r.role_code = 'TEACHER'
                          AND r.status = 1
                        """,
                Integer.class,
                teacherName
        );
        if (count == null || count == 0) {
            throw new BusinessException("指导老师不存在或未启用");
        }
        if (count > 1) {
            throw new BusinessException("指导老师重名，无法自动匹配审批人");
        }
    }

    private void ensureNoActiveGraduationOpening(Long studentId, String projectName) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM biz_graduation_project_opening opening
                        JOIN wf_application wf ON wf.id = opening.workflow_application_id
                        WHERE opening.student_id = ?
                          AND opening.project_name = ?
                          AND wf.status IN ('DRAFT', 'PENDING', 'IN_PROGRESS', 'APPROVED')
                        """,
                Integer.class,
                studentId,
                projectName
        );
        if (count != null && count > 0) {
            throw new BusinessException("该课题已存在开题申请记录");
        }
    }

    private void ensureApprovedGraduationOpening(Long studentId, String projectName, LocalDate midtermDate) {
        List<LocalDate> openingDates = jdbcTemplate.query("""
                        SELECT opening.opening_date
                        FROM biz_graduation_project_opening opening
                        JOIN wf_application wf ON wf.id = opening.workflow_application_id
                        WHERE opening.student_id = ?
                          AND opening.project_name = ?
                          AND wf.status = 'APPROVED'
                        ORDER BY opening.id DESC
                        LIMIT 1
                        """,
                (rs, rowNum) -> rs.getDate("opening_date").toLocalDate(),
                studentId,
                projectName
        );
        if (openingDates.isEmpty()) {
            throw new BusinessException("中期检查前必须先存在已审批通过的开题申请");
        }
        if (midtermDate.isBefore(openingDates.get(0))) {
            throw new BusinessException("中期检查日期不能早于开题日期");
        }
    }

    private void ensureNoActiveGraduationMidterm(Long studentId, String projectName) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM biz_graduation_project_midterm midterm
                        JOIN wf_application wf ON wf.id = midterm.workflow_application_id
                        WHERE midterm.student_id = ?
                          AND midterm.project_name = ?
                          AND wf.status IN ('DRAFT', 'PENDING', 'IN_PROGRESS', 'APPROVED')
                        """,
                Integer.class,
                studentId,
                projectName
        );
        if (count != null && count > 0) {
            throw new BusinessException("该课题已存在中期检查记录");
        }
    }

    private String requiredString(Map<String, Object> payload, String key, String label) {
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new BusinessException(label + "不能为空");
        }
        return String.valueOf(value).trim();
    }

    private Integer requiredInteger(Map<String, Object> payload, String key, String label) {
        try {
            return Integer.valueOf(requiredString(payload, key, label));
        } catch (NumberFormatException exception) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    private Integer booleanFlag(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "是".equals(value) ? 1 : 0;
    }

    private BigDecimal requiredDecimal(Map<String, Object> payload, String key, String label) {
        try {
            return new BigDecimal(requiredString(payload, key, label));
        } catch (NumberFormatException exception) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    private LocalDate requiredDate(Map<String, Object> payload, String key, String label) {
        String value = requiredString(payload, key, label);
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value.replace(' ', 'T')).toLocalDate();
            } catch (DateTimeParseException exception) {
                throw new BusinessException(label + "格式不正确");
            }
        }
    }

    private LocalDateTime requiredDateTime(Map<String, Object> payload, String key, String label) {
        String value = requiredString(payload, key, label).replace(' ', 'T');
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    private Long queryLastInsertId() {
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    private BusinessFieldValue field(String key, String label, String value) {
        return new BusinessFieldValue(key, label, value == null ? "-" : value);
    }

    private LocalDateTime getDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String formatDateTime(Timestamp timestamp) {
        return timestamp == null ? "-" : timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private record BusinessDefinition(
            String key,
            String name,
            String tableName,
            String workflowTypeCode,
            List<String> viewerRoles,
            String listTitleSql,
            String detailTitleSql
    ) {
    }

    private record BusinessRecord(
            Long id,
            Long workflowApplicationId,
            String title,
            Long createdBy
    ) {
    }

    private record StudentLeaveReference(
            Long workflowApplicationId,
            String applicationNo,
            LocalDateTime leaveStartTime,
            LocalDateTime leaveEndTime
    ) {
    }
}


