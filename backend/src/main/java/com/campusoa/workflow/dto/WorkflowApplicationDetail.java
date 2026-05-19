package com.campusoa.workflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WorkflowApplicationDetail(
        Long id,
        String applicationNo,
        Long typeId,
        String typeName,
        String title,
        String content,
        String status,
        Long applicantId,
        String applicantName,
        Long currentApproverId,
        String currentApproverName,
        LocalDateTime submittedAt,
        LocalDateTime finishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean canSubmit,
        boolean canWithdraw,
        boolean canApprove,
        boolean canReject,
        List<ApprovalRecordDto> records
) {
}
