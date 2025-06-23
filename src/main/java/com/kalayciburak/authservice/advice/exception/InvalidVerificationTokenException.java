package com.kalayciburak.authservice.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidVerificationTokenException extends RuntimeException {
    public InvalidVerificationTokenException() {
        super("Geçersiz veya süresi dolmuş doğrulama kodu.");
    }

    public InvalidVerificationTokenException(String message) {
        super(message);
    }
}