package com.kalayciburak.authservice.constant;

public final class Regexp {
    /**
     * Güçlü parola kuralları:
     * <ul>
     *     <li>En az 8 karakter</li>
     *     <li>En az bir küçük harf (a-z)</li>
     *     <li>En az bir büyük harf (A-Z)</li>
     *     <li>En az bir rakam (0-9)</li>
     *     <li>En az bir özel karakter (!@#$%^&*()_+)</li>
     * </ul>
     */
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,}$";

    private Regexp() {
    }
}
