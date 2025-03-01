package com.kalayciburak.authservice.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Bu sınıf, kimliği doğrulanmış bir kullanıcının yetkisiz erişim denemesi durumunda 403 Forbidden hata yanıtı döndüren
 * AccessDeniedHandler implementasyonudur.
 *
 * <p>Örnek JSON yanıt:
 * { "error": "Erişim Reddedildi", "message": "Bu kaynağa erişim izniniz bulunmamaktadır." }
 *
 * <p>Bu sınıf, erişim izni olmayan kullanıcıların erişim taleplerinde devreye girerek
 * detaylı hata bilgisi sunar.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Erişim reddedildiğinde çağrılır ve 403 Forbidden hata yanıtı döndürür.
     *
     * @param request               HTTP isteği
     * @param response              HTTP yanıtı
     * @param accessDeniedException Erişim reddedilme sebebi
     * @throws IOException Yanıt yazımı sırasında hata oluşursa fırlatılır.
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.FORBIDDEN.value());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "Erişim Reddedildi");
        errorDetails.put("message", "Bu kaynağa erişim izniniz bulunmamaktadır.");

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
