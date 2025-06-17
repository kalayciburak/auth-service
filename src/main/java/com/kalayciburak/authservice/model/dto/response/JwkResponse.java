package com.kalayciburak.authservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * JWK (JSON Web Key) formatında RSA public key bilgilerini temsil eden response
 * sınıfı.
 * <p>
 * RFC 7517 standardına uygun olarak JWK formatında key bilgilerini sunar.
 */
@Builder
public record JwkResponse(
        @JsonProperty("kty") String keyType,
        @JsonProperty("use") String keyUse,
        @JsonProperty("kid") String keyId,
        @JsonProperty("alg") String algorithm,
        @JsonProperty("n") String modulus,
        @JsonProperty("e") String exponent) {
    /**
     * RSA Public Key'den JWK Response oluşturur.
     *
     * @param publicKey RSA Public Key
     * @param keyId     Key ID
     * @return JWK formatında key bilgileri
     */
    public static JwkResponse fromRSAPublicKey(RSAPublicKey publicKey, String keyId) {
        return JwkResponse.builder()
                .keyType("RSA")
                .keyUse("sig")
                .keyId(keyId)
                .algorithm("RS256")
                .modulus(encodeBase64URL(publicKey.getModulus()))
                .exponent(encodeBase64URL(publicKey.getPublicExponent()))
                .build();
    }

    /**
     * BigInteger değerini Base64URL formatında encode eder.
     *
     * @param value Encode edilecek değer
     * @return Base64URL encoded string
     */
    private static String encodeBase64URL(BigInteger value) {
        byte[] bytes = value.toByteArray();

        // Pozitif sayılar için başındaki sıfır byte'ını kaldır
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}