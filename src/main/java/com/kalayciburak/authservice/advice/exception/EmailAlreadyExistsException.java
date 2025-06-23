package com.kalayciburak.authservice.advice.exception;

import jakarta.persistence.EntityExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends EntityExistsException {
    public EmailAlreadyExistsException() {
        super("Bu email adresi zaten kullanılmaktadır.");
    }

    public EmailAlreadyExistsException(String email) {
        super("Bu email adresi zaten kullanılmaktadır: " + email);
    }
}