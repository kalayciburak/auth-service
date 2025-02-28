package com.kalayciburak.authservice.model.dto.response;

import com.kalayciburak.authservice.model.entity.Role;

import java.util.Set;
import java.util.stream.Collectors;

public record RoleResponse(
        String name
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getName().name());
    }

    public static Set<RoleResponse> from(Set<Role> roles) {
        return roles.stream().map(RoleResponse::from).collect(Collectors.toSet());
    }
}
