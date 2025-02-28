package com.kalayciburak.authservice.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TokenTypeMismatchException extends RuntimeException {
    public TokenTypeMismatchException(String tokenType) {
        super("Geçersiz token tipi: " + tokenType);
    }
}