package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.model.entity.VerificationToken;
import com.kalayciburak.authservice.repository.UserRepository;
import com.kalayciburak.authservice.repository.VerificationTokenRepository;
import com.kalayciburak.commonjpa.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest(properties = {"spring.config.location=classpath:/test-application.yml"}, showSql = false)
@ContextConfiguration(classes = EmailVerificationPersistenceTest.JpaConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class EmailVerificationPersistenceTest {
    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = User.class)
    @EnableJpaRepositories(basePackageClasses = VerificationTokenRepository.class)
    @Import({EmailVerificationService.class, JpaConfig.class})
    static class JpaConfiguration {
        @Bean(name = "auditorAwareImpl")
        AuditorAware<String> auditorAware() {
            return () -> Optional.of("test");
        }
    }

    @Autowired
    private EmailVerificationService service;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private EmailService emailService;

    private TransactionTemplate transactions;

    @BeforeEach
    void setUp() {
        transactions = new TransactionTemplate(transactionManager);
        transactions.executeWithoutResult(status -> {
            tokenRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();
        });
    }

    @Test
    void resendCommitsTokenReplacementWithoutChangingRowOrViolatingUniqueness() {
        var original = saveToken("resend@example.com", "old-token", LocalDateTime.now().minusDays(1), true);
        var beforeResend = LocalDateTime.now();

        service.resendVerificationEmail(original.getUser().getEmail());
        service.resendVerificationEmail(original.getUser().getEmail());

        var sentTokens = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(2)).sendVerificationEmail(eq("resend@example.com"), eq("Test"), eq("User"), sentTokens.capture());
        var firstToken = sentTokens.getAllValues().get(0);
        var latestToken = sentTokens.getAllValues().get(1);
        assertThat(firstToken).isNotEqualTo("old-token");
        assertThat(latestToken).isNotEqualTo(firstToken);
        transactions.executeWithoutResult(status -> {
            assertThat(tokenRepository.count()).isEqualTo(1L);
            assertThat(tokenRepository.findByToken("old-token")).isEmpty();
            assertThat(tokenRepository.findByToken(firstToken)).isEmpty();
            var stored = tokenRepository.findByToken(latestToken).orElseThrow();
            assertThat(stored.getId()).isEqualTo(original.getId());
            assertThat(stored.getUser().getId()).isEqualTo(original.getUser().getId());
            assertThat(stored.isUsed()).isFalse();
            assertThat(stored.getExpiryDate()).isBetween(beforeResend.plusHours(24), LocalDateTime.now().plusHours(24));
        });
    }

    @Test
    void createsAndCommitsTokenWhenUserHasNoExistingToken() {
        var user = transactions.execute(status -> userRepository.saveAndFlush(createUser("new@example.com")));

        service.createVerificationToken(user);

        var sentToken = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq("new@example.com"), eq("Test"), eq("User"), sentToken.capture());
        transactions.executeWithoutResult(status -> {
            assertThat(tokenRepository.count()).isEqualTo(1L);
            var stored = tokenRepository.findByToken(sentToken.getValue()).orElseThrow();
            assertThat(stored.getUser().getId()).isEqualTo(user.getId());
            assertThat(stored.isUsed()).isFalse();
        });
    }

    @Test
    void deletionReturnsActualCountAndPreservesTokensAtOrAfterCutoff() {
        var cutoff = LocalDateTime.of(2026, 1, 1, 12, 0);
        saveToken("expired1@example.com", "expired-one", cutoff.minusSeconds(1), false);
        saveToken("expired2@example.com", "expired-two", cutoff.minusDays(1), true);
        saveToken("boundary@example.com", "at-cutoff", cutoff, false);
        saveToken("future@example.com", "future-token", cutoff.plusDays(1), false);

        var deletedCount = transactions.execute(status -> tokenRepository.deleteByExpiryDateBefore(cutoff));

        assertThat(deletedCount).isEqualTo(2L);
        transactions.executeWithoutResult(status -> assertThat(tokenRepository.findAll())
                .extracting(VerificationToken::getToken).containsExactlyInAnyOrder("at-cutoff", "future-token"));
        var secondDeletedCount = transactions.execute(status -> tokenRepository.deleteByExpiryDateBefore(cutoff));
        assertThat(secondDeletedCount).isZero();
    }

    @Test
    void databaseEnforcesOneTokenPerUser() {
        var original = saveToken("unique@example.com", "original-token", LocalDateTime.now().plusDays(1), false);

        assertThatThrownBy(() -> transactions.executeWithoutResult(status -> tokenRepository.saveAndFlush(
                VerificationToken.builder()
                        .user(original.getUser())
                        .token("second-token")
                        .expiryDate(LocalDateTime.now().plusDays(1))
                        .build())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private VerificationToken saveToken(String email, String value, LocalDateTime expiryDate, boolean used) {
        return transactions.execute(status -> {
            var user = userRepository.saveAndFlush(createUser(email));
            return tokenRepository.saveAndFlush(VerificationToken.builder()
                    .user(user)
                    .token(value)
                    .expiryDate(expiryDate)
                    .used(used)
                    .build());
        });
    }

    private User createUser(String email) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password("unused")
                .roles(Set.of())
                .build();
    }
}
