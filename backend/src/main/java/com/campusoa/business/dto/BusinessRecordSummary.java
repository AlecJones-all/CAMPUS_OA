package com.campusoa.business.dto;

import java.time.LocalDateTime;

public record BusinessRecordSummary(
        Long id,
        String title,
        String status,
        String applicantName,
        String currentApproverName,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {
}
