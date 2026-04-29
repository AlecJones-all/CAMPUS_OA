package com.campusoa.security;

import java.util.List;

public record AuthenticatedUser(
        Long userId,
        String username,
        String realName,
        String userType,
        List<String> roles
) {
}
