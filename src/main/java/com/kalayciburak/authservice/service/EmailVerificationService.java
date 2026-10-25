package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.advice.exception.InvalidVerificationTokenException;
import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.model.entity.VerificationToken;
import com.kalayciburak.authservice.repository.UserRepository;
import com.kalayciburak.authservice.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final int TOKEN_VALIDITY_HOURS = 24;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;

    /**
     * Kullanıcı için email doğrulama token'ı oluşturur ve email gönderir.
     *
     * @param user Token oluşturulacak kullanıcı
     */
    public void createVerificationToken(User user) {
        var token = tokenRepository.findByUser(user)
                .orElseGet(() -> VerificationToken.builder().user(user).build());
        var tokenValue = UUID.randomUUID().toString();
        token.setToken(tokenValue);
        token.setExpiryDate(LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS));
        token.setUsed(false);

        tokenRepository.save(token);

        // Email gönder
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), user.getLastName(), tokenValue);
        log.info("Doğrulama token'ı oluşturuldu: {}", user.getEmail());
    }

    /**
     * Email doğrulama token'ını kontrol eder ve kullanıcının email'ini doğrular.
     *
     * @param tokenValue Doğrulama token'ı
     * @return Doğrulanan kullanıcı
     */
    public User verifyEmail(String tokenValue) {
        var token = tokenRepository.findByToken(tokenValue).orElseThrow(() -> new InvalidVerificationTokenException("Geçersiz doğrulama kodu."));

        // Token kullanılmış mı?
        if (token.isUsed()) throw new InvalidVerificationTokenException("Bu doğrulama kodu daha önce kullanılmış.");

        // Token süresi dolmuş mu?
        boolean isExpired = token.getExpiryDate().isBefore(LocalDateTime.now());
        if (isExpired) throw new InvalidVerificationTokenException("Doğrulama kodunun süresi dolmuş.");

        // Email'i doğrula
        var user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Token'ı kullanıldı olarak işaretle
        token.setUsed(true);
        tokenRepository.save(token);

        // Hoş geldiniz emaili gönder
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName(), user.getLastName());
        log.info("Email doğrulandı: {}", user.getEmail());

        return user;
    }

    /**
     * Yeni doğrulama emaili gönderir.
     *
     * @param email Kullanıcı email adresi
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        if (user.isEmailVerified()) throw new RuntimeException("Email zaten doğrulanmış.");
        createVerificationToken(user);
    }

    /**
     * Süresi dolmuş token'ları temizler. Her gün gece yarısı çalışır.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredTokens() {
        try {
            long deletedCount = tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
            log.info("Süresi dolmuş {} adet doğrulama token'ı temizlendi.", deletedCount);
        } catch (Exception e) {
            log.error("Token temizleme işlemi başarısız.", e);
        }
    }
}