package com.kalayciburak.authservice.model.dto.response;

import com.kalayciburak.authservice.model.entity.User;

import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<RoleResponse> roles) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                RoleResponse.from(user.getRoles()));
    }
}
