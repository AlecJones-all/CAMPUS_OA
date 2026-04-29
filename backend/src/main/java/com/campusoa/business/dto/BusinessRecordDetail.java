package com.campusoa.business.dto;

import com.campusoa.workflow.dto.WorkflowApplicationDetail;

import java.util.List;

public record BusinessRecordDetail(
        Long id,
        String businessKey,
        String businessName,
        String title,
        List<BusinessFieldValue> fields,
        WorkflowApplicationDetail workflow
) {
}
