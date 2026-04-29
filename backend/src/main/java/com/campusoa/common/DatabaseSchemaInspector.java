package com.campusoa.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DatabaseSchemaInspector implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInspector.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInspector(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String schemaName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            if (schemaName == null || schemaName.isBlank()) {
                log.warn("Database schema self-check skipped because no active schema is selected.");
                return;
            }
            log.info("Database schema self-check started. schema={}", schemaName);
            for (Map.Entry<String, List<String>> entry : requiredColumns().entrySet()) {
                inspectTable(schemaName, entry.getKey(), entry.getValue());
            }
            inspectApplicationTypes();
            inspectWorkflowDefinitions();
            log.info("Database schema self-check completed. schema={}", schemaName);
        } catch (DataAccessException exception) {
            log.error("Database schema self-check failed because the database is unavailable.", exception);
        }
    }

    private void inspectTable(String schemaName, String tableName, List<String> requiredColumns) {
        List<String> columns = jdbcTemplate.queryForList(
                """
                        SELECT column_name
                        FROM information_schema.columns
                        WHERE table_schema = ?
                          AND table_name = ?
                        """,
                String.class,
                schemaName,
                tableName
        );
        if (columns.isEmpty()) {
            log.error("Database schema self-check failed. missingTable={}, schema={}", tableName, schemaName);
            return;
        }
        Set<String> actualColumns = columns.stream().collect(Collectors.toSet());
        List<String> missingColumns = requiredColumns.stream()
                .filter(column -> !actualColumns.contains(column))
                .toList();
        if (!missingColumns.isEmpty()) {
            log.error("Database schema self-check failed. table={}, missingColumns={}",
                    tableName, missingColumns);
            return;
        }
        log.info("Database schema verified. table={}, columnCount={}", tableName, columns.size());
    }

    private void inspectApplicationTypes() {
        List<String> configuredTypes = jdbcTemplate.queryForList(
                """
                        SELECT type_code
                        FROM wf_application_type
                        WHERE status = 1
                        """,
                String.class
        );
        List<String> missingTypes = requiredApplicationTypes().stream()
                .filter(type -> !configuredTypes.contains(type))
                .toList();
        if (!missingTypes.isEmpty()) {
            log.error("Database schema self-check failed. missingWorkflowTypes={}", missingTypes);
            return;
        }
        log.info("Database schema verified. workflowTypes={}", requiredApplicationTypes().size());
    }

    private void inspectWorkflowDefinitions() {
        List<String> configuredDefinitions = jdbcTemplate.queryForList(
                """
                        SELECT business_type
                        FROM wf_definition
                        WHERE status = 1
                        """,
                String.class
        );
        List<String> missingDefinitions = requiredWorkflowDefinitions().stream()
                .filter(type -> !configuredDefinitions.contains(type))
                .toList();
        if (!missingDefinitions.isEmpty()) {
            log.error("Database schema self-check failed. missingWorkflowDefinitions={}", missingDefinitions);
            return;
        }
        log.info("Database schema verified. workflowDefinitions={}", requiredWorkflowDefinitions().size());
    }

    private Map<String, List<String>> requiredColumns() {
        Map<String, List<String>> tableColumns = new LinkedHashMap<>();
        tableColumns.put("biz_student_leave_application", List.of(
                "id", "workflow_application_id", "student_id",
                "leave_start_time", "leave_end_time", "business_status", "flow_status"
        ));
        tableColumns.put("biz_student_return_confirmation", List.of(
                "id", "workflow_application_id", "student_id",
                "related_leave_no", "return_time", "business_status", "flow_status"
        ));
        tableColumns.put("biz_graduation_project_opening", List.of(
                "id", "workflow_application_id", "student_id",
                "advisor_name", "project_name", "opening_date", "business_status", "flow_status"
        ));
        tableColumns.put("biz_graduation_project_midterm", List.of(
                "id", "workflow_application_id", "student_id",
                "project_name", "midterm_date", "progress_rate", "business_status", "flow_status"
        ));
        tableColumns.put("biz_research_achievement_registration", List.of(
                "id", "workflow_application_id", "teacher_id",
                "achievement_name", "publish_time", "business_status", "flow_status"
        ));
        tableColumns.put("biz_academic_lecture_application", List.of(
                "id", "workflow_application_id", "applicant_id",
                "lecture_topic", "speaker_name", "lecture_time", "business_status", "flow_status"
        ));
        tableColumns.put("biz_office_supply_request", List.of(
                "id", "workflow_application_id", "applicant_id",
                "item_name", "request_quantity", "usage_purpose", "business_status", "flow_status"
        ));
        tableColumns.put("biz_lab_safety_hazard_report", List.of(
                "id", "workflow_application_id", "reporter_id",
                "lab_name", "hazard_type", "risk_level", "business_status", "flow_status"
        ));
        tableColumns.put("biz_dorm_adjustment_request", List.of(
                "id", "workflow_application_id", "student_id",
                "student_info", "current_dormitory", "target_dormitory", "business_status", "flow_status"
        ));
        tableColumns.put("biz_stamp_request", List.of(
                "id", "workflow_application_id", "applicant_id",
                "request_subject", "seal_type", "usage_time", "business_status", "flow_status"
        ));
        tableColumns.put("wf_application", List.of(
                "id", "application_no", "type_id", "title",
                "applicant_id", "status", "current_approver_id"
        ));
        tableColumns.put("wf_application_type", List.of(
                "id", "type_code", "type_name", "approver_role_code", "status"
        ));
        tableColumns.put("wf_node_definition", List.of(
                "id", "definition_id", "node_code", "node_name",
                "approver_role_code", "sort_no", "status"
        ));
        tableColumns.put("sys_user", List.of("id", "username", "real_name", "status"));
        tableColumns.put("sys_user_role", List.of("id", "user_id", "role_id"));
        tableColumns.put("sys_role", List.of("id", "role_code", "role_name", "status"));
        return tableColumns;
    }

    private List<String> requiredApplicationTypes() {
        return List.of(
                "STUDENT_LEAVE_APPLICATION",
                "STUDENT_RETURN_CONFIRMATION",
                "GRADUATION_PROJECT_OPENING",
                "GRADUATION_PROJECT_MIDTERM",
                "RESEARCH_ACHIEVEMENT_REGISTRATION",
                "ACADEMIC_LECTURE_APPLICATION",
                "OFFICE_SUPPLY_REQUEST",
                "LAB_SAFETY_HAZARD_REPORT",
                "DORM_ADJUSTMENT_REQUEST",
                "STAMP_REQUEST"
        );
    }

    private List<String> requiredWorkflowDefinitions() {
        return List.of(
                "STUDENT_LEAVE_APPLICATION",
                "STUDENT_RETURN_CONFIRMATION",
                "GRADUATION_PROJECT_OPENING",
                "GRADUATION_PROJECT_MIDTERM",
                "RESEARCH_ACHIEVEMENT_REGISTRATION",
                "ACADEMIC_LECTURE_APPLICATION",
                "OFFICE_SUPPLY_REQUEST",
                "LAB_SAFETY_HAZARD_REPORT",
                "DORM_ADJUSTMENT_REQUEST",
                "STAMP_REQUEST"
        );
    }
}
