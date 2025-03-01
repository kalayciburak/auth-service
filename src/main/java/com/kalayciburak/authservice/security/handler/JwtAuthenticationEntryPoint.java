package com.kalayciburak.authservice.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Bu sınıf, kimlik doğrulama hatası durumunda RESTful API'ye 401 Unauthorized hata yanıtı döndüren AuthenticationEntryPoint
 * implementasyonudur.
 *
 * <p>Örnek JSON yanıt:
 * { "error": "Kimlik Doğrulama Başarısız", "message": "Bu kaynağa erişmek için kimlik doğrulama gereklidir." }
 *
 * <p>Bu sınıf, güvenli erişim gerektiren kaynaklara erişim sağlanamadığında devreye girer.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Kimlik doğrulaması başarısız olduğunda çağrılır.
     *
     * @param request       HTTP isteği
     * @param response      HTTP yanıtı
     * @param authException Gerçekleşen kimlik doğrulama hatası
     * @throws IOException Yanıt yazımı sırasında hata oluşursa fırlatılır.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "Kimlik Doğrulama Başarısız");
        errorDetails.put("message", "Bu kaynağa erişmek için kimlik doğrulama gereklidir.");

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
