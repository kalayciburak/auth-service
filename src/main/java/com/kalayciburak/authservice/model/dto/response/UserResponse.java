package com.kalayciburak.authservice.model.dto.response;

import com.kalayciburak.authservice.model.entity.User;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        Set<RoleResponse> roles
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                RoleResponse.from(user.getRoles())
        );
    }
}
