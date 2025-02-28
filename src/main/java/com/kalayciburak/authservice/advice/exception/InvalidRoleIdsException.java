package com.kalayciburak.authservice.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRoleIdsException extends RuntimeException {
    public InvalidRoleIdsException(String message) {
        super(message);
    }

    public InvalidRoleIdsException(Set<Long> missingRoleIds) {
        super("Belirtilen rol ID'leri geçersiz veya sistemde bulunamadı: " + missingRoleIds);
    }
}