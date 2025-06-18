package com.kalayciburak.authservice.service.helper;

import com.kalayciburak.authservice.model.dto.request.RegisterRequest;
import com.kalayciburak.authservice.model.entity.Role;
import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.model.enums.RoleType;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Kullanıcı ile ilgili yardımcı metotları içeren sınıf.
 */
@Component
@RequiredArgsConstructor
public class UserHelper {
    private final PasswordEncoder passwordEncoder;

    /**
     * Kullanıcının şifresini güvenli hale getirerek encode eder.
     *
     * @param rawPassword Ham şifre
     * @return Encode edilmiş (güvenli) şifre
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Kullanıcının ADMIN rolüne sahip olup olmadığını kontrol eder.
     *
     * @param user Kontrol edilecek kullanıcı
     * @return Kullanıcı ADMIN ise true, değilse false
     */
    public static boolean hasAdminRole(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleType.ROLE_ADMIN));
    }

    /**
     * RegisterRequest ile yeni bir User varlığı oluşturur.
     *
     * @param request Kullanıcı bilgileri.
     * @param roles   Kullanıcıya atanacak roller.
     * @return Oluşturulan User nesnesi.
     */
    public User buildUser(RegisterRequest request, Set<Role> roles) {
        return User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .roles(roles)
                .build();
    }
}
