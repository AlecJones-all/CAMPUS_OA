package com.campusoa.workflow.dto;

import java.time.LocalDateTime;

public record WorkflowApplicationSummary(
        Long id,
        String applicationNo,
        Long typeId,
        String typeName,
        String title,
        String status,
        String applicantName,
        String currentApproverName,
        String currentApproverRoleCode,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {
}
