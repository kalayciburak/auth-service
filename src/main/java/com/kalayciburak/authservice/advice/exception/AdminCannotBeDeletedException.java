package com.kalayciburak.authservice.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <b>ADMIN</b> rolüne sahip kullanıcıların silinemeyeceğini belirten özel bir exception.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AdminCannotBeDeletedException extends RuntimeException {
    public AdminCannotBeDeletedException() {
        super("ADMIN rolüne sahip kullanıcılar silinemez!");
    }

    public AdminCannotBeDeletedException(String message) {
        super(message);
    }
}

