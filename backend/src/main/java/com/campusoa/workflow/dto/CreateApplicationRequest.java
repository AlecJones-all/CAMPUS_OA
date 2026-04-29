package com.campusoa.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateApplicationRequest(
        @NotNull(message = "申请类型不能为空")
        Long typeId,
        @NotBlank(message = "申请标题不能为空")
        String title,
        @NotBlank(message = "申请内容不能为空")
        String content
) {
}
