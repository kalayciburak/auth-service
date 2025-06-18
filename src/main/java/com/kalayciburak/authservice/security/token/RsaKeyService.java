package com.kalayciburak.authservice.security.token;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * RSA key çiftini yöneten servis sınıfı.
 * <p>
 * Bu sınıf JWT token'ları RS256 algoritması ile imzalamak ve doğrulamak için gerekli RSA public ve private key'leri yönetir.
 * Key'ler Vault'tan secrets/auth-service/jwt/ altında rsa-private-key ve rsa-public-key olarak okunur.
 */
@Service
@Getter
public class RsaKeyService {
    private static final int KEY_SIZE = 2048;
    private static final String RSA_ALGORITHM = "RSA";

    private String keyId;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @Value("${app.jwt.rsa.private-key:#{null}}")
    private String privateKeyString;

    @Value("${app.jwt.rsa.public-key:#{null}}")
    private String publicKeyString;

    /**
     * RSA key çiftini yükler veya oluşturur.
     * <p>
     * Eğer Vault'tan key'ler okunabilirse yükler, değilse runtime'da yeni bir çift oluşturur.
     */
    @PostConstruct
    private void initializeKeys() {
        try {
            if (areKeysAvailable()) loadKeysFromStrings();
            else generateKeyPair();
            this.keyId = UUID.randomUUID().toString();
        } catch (Exception ex) {
            throw new IllegalStateException("RSA key çifti yüklenemedi", ex);
        }
    }

    /**
     * Vault'tan key'lerin mevcut olup olmadığını kontrol eder.
     *
     * @return Key'ler mevcut ise true
     */
    private boolean areKeysAvailable() {
        return StringUtils.hasText(privateKeyString) && StringUtils.hasText(publicKeyString);
    }

    /**
     * Vault'tan okunmuş string'lerden RSA key çiftini yükler.
     *
     * @throws NoSuchAlgorithmException Algorithm hatası
     * @throws InvalidKeySpecException  Key spec hatası
     */
    private void loadKeysFromStrings() throws NoSuchAlgorithmException, InvalidKeySpecException {
        var keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);

        // Private key yükleme
        var privateKeyContent = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        var privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

        // Public key yükleme
        var publicKeyContent = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        var publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        this.publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
    }

    /**
     * Runtime'da yeni bir RSA key çifti oluşturur.
     *
     * @throws NoSuchAlgorithmException Algorithm hatası
     */
    private void generateKeyPair() throws NoSuchAlgorithmException {
        var keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        var keyPair = keyPairGenerator.generateKeyPair();

        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }
}