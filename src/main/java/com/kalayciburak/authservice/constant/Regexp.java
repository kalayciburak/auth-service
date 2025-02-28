package com.kalayciburak.authservice.constant;

public final class Regexp {
    public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+]{6,}$";

    private Regexp() {}
}
