package com.eventhub.dto.response;

import com.eventhub.enums.Role;

public record AuthResponse(
        String token,
        String username,
        Role role
) {
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public String getAuthorizationHeader() {
        return "Bearer " + token;
    }
}
