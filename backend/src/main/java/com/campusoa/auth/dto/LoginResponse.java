package com.campusoa.auth.dto;

import java.util.List;

public record LoginResponse(
        String token,
        UserProfile profile,
        List<String> menus,
        List<String> permissions
) {
}
