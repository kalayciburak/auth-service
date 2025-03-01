package com.kalayciburak.authservice.security.audit;

import com.kalayciburak.commonjpa.audit.AuditorProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * {@code SecurityAuditorProvider}, mevcut kullanıcıyı belirlemek için kullanılan bir bileşendir.
 * <p>
 * Spring Security'nin sağladığı {@link SecurityContextHolder} kullanılarak kimlik doğrulama bilgileri alınır ve geçerli
 * kullanıcı belirlenir.
 * </p>
 */
@Component
public class SecurityAuditorProvider implements AuditorProvider {
    private final static String SYSTEM_USER = "system";

    /**
     * Mevcut oturumun kimliğini döndürür.
     *
     * @return Geçerli kullanıcının adı veya kimliği, eğer kimlik doğrulama yoksa "system" döner.
     */
    @Override
    public String getCurrentAuditor() {
        Authentication authentication = getAuthentication();
        if (isAuthenticated(authentication)) return extractUsername(authentication);

        return SYSTEM_USER;
    }

    /**
     * Mevcut kimlik doğrulama nesnesini döndürür.
     *
     * @return {@link Authentication} nesnesi, eğer mevcut değilse {@code null} döner.
     */
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Kimlik doğrulamanın geçerli olup olmadığını kontrol eder.
     *
     * @param authentication {@link Authentication} nesnesi
     * @return Eğer kimlik doğrulama geçerli ve oturum açık ise {@code true}, aksi halde {@code false} döner.
     */
    private static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * {@link Authentication} nesnesinden kullanıcı adını çıkarır.
     *
     * @param authentication Geçerli kimlik doğrulama bilgilerini içeren nesne
     * @return Kullanıcı adı ya da kimliği
     */
    private static String extractUsername(Authentication authentication) {
        var principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) return ((UserDetails) principal).getUsername();

        return principal.toString();
    }
}
