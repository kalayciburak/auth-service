package com.kalayciburak.authservice.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OldPasswordMismatchException extends RuntimeException {
    public OldPasswordMismatchException() {
        super("Eski şifre hatalı.");
    }

    public OldPasswordMismatchException(String message) {
        super(message);
    }
}
