package com.kalayciburak.authservice.service.validator;

import com.kalayciburak.authservice.advice.exception.BreachedPasswordException;
import com.kalayciburak.authservice.advice.exception.EmailAlreadyExistsException;
import com.kalayciburak.authservice.advice.exception.OldPasswordMismatchException;
import com.kalayciburak.authservice.model.dto.request.ChangePasswordRequest;
import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Kullanıcı ile ilgili validasyon işlemlerini yöneten sınıf.
 */
@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker passwordChecker;

    /**
     * Email adresinin eşsiz olup olmadığını kontrol eder.
     *
     * @param email Email adresi
     */
    public void validateUniqueEmail(String email) {
        if (repository.existsByEmail(email)) throw new EmailAlreadyExistsException(email);
    }

    /**
     * Eski parolanın doğruluğunu kontrol eder.
     * <p>
     * Eğer eski parola doğru değilse {@link IllegalArgumentException} fırlatır.
     *
     * @param request ChangePasswordRequest
     * @param user    User
     */
    public void validateOldPassword(ChangePasswordRequest request, User user) {
        boolean isOldPasswordIncorrect = !passwordEncoder.matches(request.oldPassword(), user.getPassword());
        if (isOldPasswordIncorrect) throw new OldPasswordMismatchException();
    }

    /**
     * Parolanın güvenlik ihlallerine karşı durumunu kontrol eder.
     * <p>
     * Eğer parola herhangi bir veri ihlalinde tespit edilmişse, işlemi reddeder.
     *
     * @param password Kontrol edilecek parola
     * @throws BreachedPasswordException parolanın veri ihlallerinde tespit edilmesi durumunda
     */
    public void validatePasswordDataBreachStatus(String password) {
        if (passwordChecker.check(password).isCompromised()) throw new BreachedPasswordException();
    }
}
