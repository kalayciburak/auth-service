# Auth Service - Modern Kimlik Doğrulama Servisi

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-Latest-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Enabled-red.svg)](https://redis.io/)
[![Vault](https://img.shields.io/badge/Vault-Enabled-black.svg)](https://www.vaultproject.io/)
[![Graylog](https://img.shields.io/badge/Graylog-Enabled-purple.svg)](https://www.graylog.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-white.svg)](LICENSE.txt)

## Özet

**Auth Service**, modern mikroservis mimarileri için **güvenli kimlik doğrulama ve yetkilendirme** hizmeti sunan, **Spring Boot 3**
ile geliştirilmiş, **Common JPA**, **Auditing**, **Graylog merkezi loglama** ve **soft-delete** destekli bir servistir.
**RS256 RSA imzalı JWT** tabanlı oturum yönetimi, kullanıcı yetkilendirme, **JWK (JSON Web Key)** desteği ve güvenlik mekanizmalarını kapsayan bu servis, mikroservis mimarilerine kolayca entegre edilebilir.

## Özellikler

- 🔐 **RS256 RSA algoritması ile imzalanmış JWT tabanlı kimlik doğrulama**
- 🔑 **JWK (JSON Web Key) endpoint'leri - RFC 7517 standardına uygun**
- 🌐 **Mikroservis entegrasyonu için `.well-known/jwks.json` desteği**
- 👥 **Kullanıcı ve rol tabanlı yetkilendirme**
- 🔄 **Token yenileme ve kara listeye alma (Redis destekli)**
- 🔒 **RSA key çifti yönetimi (Vault entegrasyonu veya runtime üretimi)**
- 🛡️ **Spring Security ile entegre kimlik ve yetkilendirme yönetimi**
- 📜 **[Common JPA Package](https://github.com/kalayciburak/common-jpa-package) entegrasyonu**
- 🛠 **Auditing ile kimlik doğrulama logları tutulur**
- 📊 **Graylog + Elasticsearch + MongoDB tabanlı merkezi loglama sistemi**
- 🔒 **HashiCorp Vault ile güvenli konfigürasyon yönetimi**
- 📝 **Swagger/OpenAPI dokümantasyonu**
- 💪 **Docker desteği**
- 🛃️ **MySQL veritabanı entegrasyonu**
- ❌ **Soft-delete mekanizması ile silinmeyen veri yönetimi**
- ✅ **Merkezi hata yönetimi**

## Gereksinimler

- **Java 21+**
- **Docker & Docker Compose**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis**
- **Vault**
- **Graylog**
- **Common JPA Package**

## Kurulum

1. **Projeyi klonlayın:**

   ```bash
   git clone https://github.com/kalayciburak/auth-service.git
   cd auth-service
   ```

2. **Ortam değişkenlerini ayarlayın:**
   **Kök dizinde** `.env` dosyası oluşturun ve aşağıdaki bilgileri ekleyin:

   ```properties
   MYSQL_ROOT_PASSWORD=your_mysql_password
   DB_USERNAME=root
   DB_PASSWORD=your_mysql_password
   SECRET_KEY=your_jwt_secret_key
   EXPIRATION_TIME_MS=3600000
   REFRESH_EXPIRATION_TIME_MS=86400000
   REDIS_HOST=localhost
   REDIS_PORT=6379
   REDIS_PASSWORD=your_redis_password
   VAULT_URI=http://localhost:8200
   VAULT_TOKEN=my-root-token
   GRAYLOG_HOST=localhost
   GRAYLOG_PORT=12201
   GRAYLOG_PASSWORD_SECRET=your_graylog_password_secret
   GRAYLOG_ROOT_PASSWORD_SHA2=your_graylog_root_password_sha2
   ```

3. **RSA Key Dosyalarını Yapılandırın (İsteğe Bağlı):**

   ```bash
   # Özel bir RSA key çifti kullanmak istiyorsanız
   mkdir -p src/main/resources/keys

   # Private key oluşturma (PKCS#8 formatında)
   openssl genpkey -algorithm RSA -out src/main/resources/keys/private.pem -pkeyopt rsa_keygen_bits:2048

   # Public key oluşturma
   openssl rsa -pubout -in src/main/resources/keys/private.pem -out src/main/resources/keys/public.pem
   ```

   > **Not:** Key dosyaları sağlanmazsa, uygulama otomatik olarak runtime'da RSA key çifti oluşturacaktır.

4. **Docker kullanarak servisleri başlatın:**

   ```bash
   docker-compose up -d
   ```

5. **Uygulamayı derleyin:**

   ```bash
   ./mvnw clean install
   ```

6. **Uygulamayı çalıştırın:**
   ```bash
   ./mvnw spring-boot:run
   ```

📌 **Servis çalıştığında:** `http://localhost:8080` adresinde kullanıma hazır olacaktır.

## API Kullanımı

### Temel Endpointler

- **POST** `/api/auth/register` - Yeni kullanıcı kaydı
- **POST** `/api/auth/login` - Kullanıcı girişi ve JWT token alma
- **POST** `/api/auth/refresh` - Token yenileme
- **POST** `/api/auth/logout` - Kullanıcı çıkışı ve token kara listeye alma

### JWK (JSON Web Key) Endpointleri

- **GET** `/.well-known/jwks.json` - JWT token doğrulaması için public key bilgileri (JWK formatında)

### Kullanıcı Yönetimi Endpointleri

- **GET** `/api/user` - Tüm kullanıcıları listele (ADMIN yetkisi gerekli)
- **PUT** `/api/user/{id}/roles` - Kullanıcı rollerini güncelle (ADMIN yetkisi gerekli)
- **PUT** `/api/user/{id}/change-password` - Kullanıcı parolasını değiştir
- **DELETE** `/api/user/{id}` - Kullanıcı sil (ADMIN yetkisi gerekli)

🛠 **API dokümantasyonu:**

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Örnek API İstekleri

#### **1️⃣ Giriş Yap & Token Al**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password123"
  }'
```

#### **2️⃣ Token Kara Listeye Alarak Çıkış Yap**

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <your_jwt_token>"
```

#### **3️⃣ JWK Set Bilgilerini Al**

```bash
curl -X GET http://localhost:8080/.well-known/jwks.json
```

## Mikroservis Entegrasyonu

Bu auth-service diğer mikroservislerin JWT token'larını doğrulaması için JWK endpoint'i sunar:

```bash
# Diğer mikroservisler bu endpoint'i kullanarak public key bilgilerini alabilir
GET http://auth-service:8080/.well-known/jwks.json
```

### Diğer Mikroservislerde JWT Doğrulama

Diğer mikroservislerde JWT token'larını doğrulamak için:

1. JWK endpoint'ini kullanarak public key bilgilerini alın
2. RS256 algoritması ile token signature'ını doğrulayın
3. Token'daki `iss` (issuer) claim'inin `auth-service` olduğunu kontrol edin
4. Token'daki `aud` (audience) claim'inin `auth-service-clients` olduğunu kontrol edin

## Güvenlik Özellikleri

- **RS256 RSA İmza:** Asymmetric key pair ile güvenli token imzalama
- **Parola İhlal Kontrolü:** HaveIBeenPwned API ile parola güvenlik kontrolü
- **Token Kara Liste:** Redis ile token geçersizleştirme
- **Soft Delete:** Kullanıcı verilerinin güvenli silinmesi
- **Role-Based Access Control:** Detaylı yetkilendirme sistemi

## Katkıda Bulunma

Projeye katkı sağlamak için:

1. Projeyi **fork** edin
2. Yeni bir **branch** oluşturun (`git checkout -b feature/yeni-ozellik`)
3. Değişiklikleri **commit** edin (`git commit -m 'Yeni özellik eklendi'`)
4. Branch'inizi **push** edin (`git push origin feature/yeni-ozellik`)
5. Bir **Pull Request** oluşturun

🛠 PR'nızın aşağıdaki şartlara uygun olmasına dikkat edin:

- Kod stiline uygunluk
- Dokümantasyon eklenmesi
- Yeterli test kapsamı

## Lisans

Bu proje **MIT Lisansı** ile lisanslanmıştır. Detaylar için [LICENSE](LICENSE.txt) dosyasına bakabilirsiniz.

## Destek

Sorularınız ve geri bildirimleriniz için GitHub'da bir **issue** oluşturabilirsiniz.

---

💙 **Modern kimlik doğrulama sisteminizi güvenli hale getirin!**
