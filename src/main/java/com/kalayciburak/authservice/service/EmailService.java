package com.kalayciburak.authservice.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Email doğrulama linki gönderir.
     *
     * @param toEmail   Alıcı email adresi
     * @param firstName Kullanıcı adı
     * @param lastName  Kullanıcı soyadı
     * @param token     Doğrulama token'ı
     */
    public void sendVerificationEmail(String toEmail, String firstName, String lastName, String token) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Email Adresinizi Doğrulayın");

            var verificationLink = frontendUrl + "/verify-email?token=" + token;
            var fullName = firstName + " " + lastName;
            var htmlContent = buildVerificationEmailContent(fullName, verificationLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Doğrulama emaili gönderildi: {}", toEmail);
        } catch (MessagingException | MailException e) {
            log.error("Email gönderimi başarısız: {}", toEmail, e);
            throw new RuntimeException("Email gönderilemedi", e);
        }
    }

    /**
     * Başarılı email doğrulama sonrası bilgilendirme emaili gönderir.
     *
     * @param toEmail   Alıcı email adresi
     * @param firstName Kullanıcı adı
     * @param lastName  Kullanıcı soyadı
     */
    public void sendWelcomeEmail(String toEmail, String firstName, String lastName) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Hoş Geldiniz!");

            var fullName = firstName + " " + lastName;
            var htmlContent = buildWelcomeEmailContent(fullName);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Hoş geldiniz emaili gönderildi: {}", toEmail);
        } catch (MessagingException | MailException e) {
            log.error("Email gönderimi başarısız: {}", toEmail, e);
            // Welcome email kritik değil, hata fırlatmıyoruz
        }
    }

    private String buildVerificationEmailContent(String name, String verificationLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f8f9fa; }
                        .button { display: inline-block; padding: 12px 24px; background-color: #007bff;
                                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 14px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Email Doğrulama</h1>
                        </div>
                        <div class="content">
                            <h2>Merhaba %s,</h2>
                            <p>Hesabınızı oluşturduğunuz için teşekkür ederiz. Email adresinizi doğrulamak için lütfen aşağıdaki butona tıklayın:</p>
                            <center>
                                <a href="%s" class="button">Email Adresimi Doğrula</a>
                            </center>
                            <p>Veya aşağıdaki linki tarayıcınıza kopyalayın:</p>
                            <p style="word-break: break-all;">%s</p>
                            <p><strong>Not:</strong> Bu link 24 saat içinde geçerliliğini yitirecektir.</p>
                        </div>
                        <div class="footer">
                            <p>Bu email'i siz talep etmediyseniz, lütfen görmezden gelin.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(name, verificationLink, verificationLink);
    }

    private String buildWelcomeEmailContent(String name) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f8f9fa; }
                        .footer { text-align: center; padding: 20px; color: #6c757d; font-size: 14px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Hoş Geldiniz!</h1>
                        </div>
                        <div class="content">
                            <h2>Merhaba %s,</h2>
                            <p>Email adresiniz başarıyla doğrulandı. Artık sistemimizin tüm özelliklerinden yararlanabilirsiniz.</p>
                            <p>Herhangi bir sorunuz olursa, bizimle iletişime geçmekten çekinmeyin.</p>
                            <p>İyi günler dileriz!</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 Tüm hakları saklıdır.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(name);
    }
}