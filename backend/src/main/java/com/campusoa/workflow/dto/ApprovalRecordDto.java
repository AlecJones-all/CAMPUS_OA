package com.campusoa.workflow.dto;

import java.time.LocalDateTime;

public record ApprovalRecordDto(
        Long id,
        String actionType,
        String actorName,
        String comment,
        LocalDateTime createdAt
) {
}
