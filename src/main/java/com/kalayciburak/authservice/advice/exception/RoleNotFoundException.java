package com.kalayciburak.authservice.advice.exception;

import com.kalayciburak.authservice.model.enums.RoleType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }

    public RoleNotFoundException(RoleType role) {
        super("Rol bulunamadı: " + role.name());
    }
}
