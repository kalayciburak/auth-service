package com.kalayciburak.authservice.security.token;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class RsaKeyServiceTest {
    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r\n"})
    void loadsStandardPkcs8PrivateKeyAndX509PublicKey(String lineSeparator) throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var keyPair = generator.generateKeyPair();
        var encoder = Base64.getMimeEncoder(64, lineSeparator.getBytes(StandardCharsets.US_ASCII));
        var privatePem = "-----BEGIN PRIVATE KEY-----" + lineSeparator
                + encoder.encodeToString(keyPair.getPrivate().getEncoded()) + lineSeparator
                + "-----END PRIVATE KEY-----" + lineSeparator;
        var publicPem = "-----BEGIN PUBLIC KEY-----" + lineSeparator
                + encoder.encodeToString(keyPair.getPublic().getEncoded()) + lineSeparator
                + "-----END PUBLIC KEY-----" + lineSeparator;
        var service = new RsaKeyService();
        ReflectionTestUtils.setField(service, "privateKeyString", privatePem);
        ReflectionTestUtils.setField(service, "publicKeyString", publicPem);

        ReflectionTestUtils.invokeMethod(service, "initializeKeys");

        assertThat(service.getPrivateKey().getEncoded()).isEqualTo(keyPair.getPrivate().getEncoded());
        assertThat(service.getPublicKey().getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
        assertThat(service.getKeyId()).isNotBlank();
    }
}
