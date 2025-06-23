package com.kalayciburak.authservice.model.enums;

public enum RoleType {
    ROLE_FREE, // Ücretsiz üyelik
    ROLE_PREMIUM, // Premium üyelik
    ROLE_GUEST, // Misafir kullanıcı
    ROLE_ADMIN, // Yönetici
    ROLE_MODERATOR // Moderatör (readonly admin)
}
