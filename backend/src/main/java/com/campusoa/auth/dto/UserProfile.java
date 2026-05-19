package com.campusoa.auth.dto;

import java.util.List;

public record UserProfile(
        Long userId,
        String username,
        String realName,
        String userType,
        List<String> roles
) {
}
