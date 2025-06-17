package com.kalayciburak.authservice.controller;

import com.kalayciburak.authservice.model.dto.response.JwkResponse;
import com.kalayciburak.authservice.model.dto.response.JwkSetResponse;
import com.kalayciburak.authservice.security.token.RsaKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWK (JSON Web Key) endpoint'lerini sunan controller sınıfı.
 * <p>
 * RFC 7517 standardına uygun olarak JWK setini sunar. Bu endpoint diğer microservislerin JWT token'larını doğrulaması için
 * gereklidir.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "JWK (JSON Web Key)", description = "JWT token doğrulaması için public key bilgileri")
public class JwkController {
    private final RsaKeyService rsaKeyService;

    /**
     * JWK Set endpoint'i.
     * <p>
     * Bu endpoint diğer microservislerin JWT token'larını doğrulaması için RSA public key bilgilerini JWK formatında sunar.
     *
     * @return JWK Set formatında public key bilgileri
     */
    @GetMapping("/.well-known/jwks.json")
    @Operation(summary = "JWK Set al", description = "JWT token doğrulaması için public key bilgilerini JWK formatında döndürür")
    public JwkSetResponse getJwkSet() {
        return JwkSetResponse.of(JwkResponse.fromRSAPublicKey(rsaKeyService.getPublicKey(), rsaKeyService.getKeyId()));
    }
}