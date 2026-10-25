package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.model.entity.VerificationToken;
import com.kalayciburak.authservice.repository.UserRepository;
import com.kalayciburak.authservice.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {
    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @InjectMocks
    private EmailVerificationService service;

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void createsTokenWhenUserHasNoTokenWithoutLoggingIt(CapturedOutput output) {
        var user = createUser();
        when(tokenRepository.findByUser(user)).thenReturn(Optional.empty());
        var earliestExpiry = LocalDateTime.now().plusHours(24);

        service.createVerificationToken(user);

        var captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        var token = captor.getValue();
        assertThat(token.getUser()).isSameAs(user);
        assertDoesNotThrow(() -> UUID.fromString(token.getToken()));
        assertThat(token.isUsed()).isFalse();
        assertThat(token.getExpiryDate()).isBetween(earliestExpiry, LocalDateTime.now().plusHours(24));
        verify(emailService).sendVerificationEmail(user.getEmail(), user.getFirstName(), user.getLastName(), token.getToken());
        verify(tokenRepository, never()).delete(any());
        assertThat(output.getAll()).contains("Doğrulama token'ı oluşturuldu").doesNotContain(token.getToken());
    }

    @Test
    void replacesExistingTokenInPlaceAndResetsExpiryAndUsedFlag() {
        var user = createUser();
        var token = VerificationToken.builder()
                .user(user)
                .token("old-token")
                .expiryDate(LocalDateTime.now().minusDays(1))
                .used(true)
                .build();
        token.setId(42L);
        when(tokenRepository.findByUser(user)).thenReturn(Optional.of(token));
        var earliestExpiry = LocalDateTime.now().plusHours(24);

        service.createVerificationToken(user);

        assertThat(token.getId()).isEqualTo(42L);
        assertThat(token.getUser()).isSameAs(user);
        assertThat(token.getToken()).isNotEqualTo("old-token");
        assertDoesNotThrow(() -> UUID.fromString(token.getToken()));
        assertThat(token.getExpiryDate()).isBetween(earliestExpiry, LocalDateTime.now().plusHours(24));
        assertThat(token.isUsed()).isFalse();
        verify(tokenRepository).findByUser(user);
        verify(tokenRepository).save(same(token));
        verifyNoMoreInteractions(tokenRepository);
        verify(emailService).sendVerificationEmail(user.getEmail(), user.getFirstName(), user.getLastName(), token.getToken());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 3})
    @ExtendWith(OutputCaptureExtension.class)
    void cleanupReportsRepositoryDeletionCount(long deletedCount, CapturedOutput output) {
        when(tokenRepository.deleteByExpiryDateBefore(any(LocalDateTime.class))).thenReturn(deletedCount);
        var beforeCleanup = LocalDateTime.now();

        service.cleanupExpiredTokens();

        var cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(tokenRepository).deleteByExpiryDateBefore(cutoff.capture());
        assertThat(cutoff.getValue()).isBetween(beforeCleanup, LocalDateTime.now());
        assertThat(output.getAll()).contains("Süresi dolmuş " + deletedCount + " adet doğrulama token'ı temizlendi.");
    }

    private User createUser() {
        return User.builder().firstName("Test").lastName("User").email("test@example.com").build();
    }
}
