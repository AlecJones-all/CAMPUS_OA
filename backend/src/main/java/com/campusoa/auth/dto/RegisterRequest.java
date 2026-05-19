package com.campusoa.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 32, message = "用户名长度需为 4 到 32 个字符")
        String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需为 6 到 32 个字符")
        String password,
        @NotBlank(message = "确认密码不能为空")
        String confirmPassword,
        @NotBlank(message = "姓名不能为空")
        @Size(max = 64, message = "姓名长度不能超过 64 个字符")
        String realName,
        @Size(max = 32, message = "手机号长度不能超过 32 个字符")
        String phone,
        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过 128 个字符")
        String email
) {
}
