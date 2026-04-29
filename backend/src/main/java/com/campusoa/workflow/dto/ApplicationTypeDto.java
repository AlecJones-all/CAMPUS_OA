package com.campusoa.workflow.dto;

public record ApplicationTypeDto(
        Long id,
        String typeCode,
        String typeName,
        String description,
        String approverRoleCode
) {
}
