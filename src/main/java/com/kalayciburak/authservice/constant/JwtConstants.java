package com.kalayciburak.authservice.constant;

public final class JwtConstants {
    public static final String ROLES_CLAIM = "roles";
    public static final String REALM_ACCESS_CLAIM = "realm_access";
    public static final String SCOPE_CLAIM = "scope";
    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";
    public static final String ISSUER = "auth-service";
    public static final String AUDIENCE = "auth-service-clients";
    public static final String KEY_ID_HEADER = "kid";

    private JwtConstants() {
    }
}