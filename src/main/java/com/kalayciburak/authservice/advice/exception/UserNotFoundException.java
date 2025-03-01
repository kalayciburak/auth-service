package com.kalayciburak.authservice.advice.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException() {
        super("Kullanıcı bulunamadı.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
