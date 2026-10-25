package com.kalayciburak.authservice.service;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService service;

    private MimeMessage message;

    @BeforeEach
    void setUp() {
        message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        ReflectionTestUtils.setField(service, "fromEmail", "auth@example.com");
        ReflectionTestUtils.setField(service, "frontendUrl", "https://example.com");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void escapesUserNamesInHtmlEmails(boolean verificationEmail) throws Exception {
        var firstName = "<img src=x onerror=alert(1)> & \"Ada\"";
        var lastName = "O'Neil <script>alert(2)</script> Çağrı";

        if (verificationEmail) service.sendVerificationEmail("test@example.com", firstName, lastName, "test-token");
        else service.sendWelcomeEmail("test@example.com", firstName, lastName);

        verify(mailSender).send(message);
        var html = htmlBody();
        assertThat(html).contains("&lt;img src=x onerror=alert(1)&gt; &amp; &quot;Ada&quot;");
        assertThat(html).contains("O&#39;Neil &lt;script&gt;alert(2)&lt;/script&gt; Çağrı");
        assertThat(html).doesNotContain("<img", "<script>");
        if (verificationEmail) assertThat(html).contains("href=\"https://example.com/verify-email?token=test-token\"");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void preservesPlainUserNames(boolean verificationEmail) throws Exception {
        if (verificationEmail) service.sendVerificationEmail("test@example.com", "Çağrı", "Yılmaz", "test-token");
        else service.sendWelcomeEmail("test@example.com", "Çağrı", "Yılmaz");

        verify(mailSender).send(message);
        assertThat(htmlBody()).contains("<h2>Merhaba Çağrı Yılmaz,</h2>");
    }

    private String htmlBody() throws Exception {
        message.saveChanges();
        BodyPart part = ((Multipart) message.getContent()).getBodyPart(0);
        while (part.getContent() instanceof Multipart multipart) part = multipart.getBodyPart(0);
        assertThat(part.isMimeType("text/html")).isTrue();
        return (String) part.getContent();
    }
}
