package com.kalayciburak.authservice.model.dto.response;

import java.util.List;

/**
 * JWKSet (JSON Web Key Set) formatında key listesini temsil eden response sınıfı.
 * <p>
 * RFC 7517 standardına uygun olarak JWK setini sunar.
 */
public record JwkSetResponse(List<JwkResponse> keys) {

    /**
     * Tek bir JWK'dan JWKSet oluşturur.
     *
     * @param jwk JWK response
     * @return JWKSet response
     */
    public static JwkSetResponse of(JwkResponse jwk) {
        return new JwkSetResponse(List.of(jwk));
    }

    /**
     * Birden fazla JWK'dan JWKSet oluşturur.
     *
     * @param jwks JWK response listesi
     * @return JWKSet response
     */
    public static JwkSetResponse of(List<JwkResponse> jwks) {
        return new JwkSetResponse(jwks);
    }
}