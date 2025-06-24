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

**Auth Service**, modern mikroservis mimarileri için **güvenli kimlik doğrulama ve yetkilendirme** hizmeti sunan, **Spring
Boot 3**
ile geliştirilmiş, **Common JPA**, **Auditing**, **Graylog merkezi loglama** ve **soft-delete** destekli bir servistir.
**RS256 RSA imzalı JWT** tabanlı oturum yönetimi, kullanıcı yetkilendirme, **JWK (JSON Web Key)** desteği ve güvenlik
mekanizmalarını kapsayan bu servis, mikroservis mimarilerine kolayca entegre edilebilir.

## Özellikler

- 🔐 **RS256 RSA algoritması ile imzalanmış JWT tabanlı kimlik doğrulama**
- 🔑 **JWK (JSON Web Key) endpoint'leri - RFC 7517 standardına uygun**
- 🌐 **Mikroservis entegrasyonu için `.well-known/jwks.json` desteği**
- 👥 **Üyelik bazlı rol sistemi (FREE, PREMIUM, GUEST, ADMIN, MODERATOR)**
- 🔄 **Token yenileme ve kara listeye alma (Redis destekli)**
- 🔒 **RSA key çifti yönetimi (Vault entegrasyonu ile otomatik üretim)**
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
- 📧 **Email tabanlı giriş sistemi**
- 👤 **Ad-Soyad validasyonu ve normalizasyonu**

## Rol Sistemi

Servis, üyelik bazlı rol sistemi kullanır:

- **ROLE_FREE**: Ücretsiz üyelik
- **ROLE_PREMIUM**: Premium üyelik
- **ROLE_GUEST**: Misafir kullanıcı
- **ROLE_ADMIN**: Yönetici
- **ROLE_MODERATOR**: Moderatör (kritik endpointlerde readonly yetkili)

## Gereksinimler

- **Java 21+**
- **Docker & Docker Compose**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis**
- **HashiCorp Vault**
- **Graylog**
- **OpenSSL** (RSA key üretimi için)
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
    # Vault Configuration
    VAULT_URI=http://localhost:8200
    VAULT_TOKEN=my-root-token
    # Database
    DB_USERNAME=liflow_user
    DB_PASSWORD=secure_db_pass
    DB_URL=jdbc:mysql://localhost:3306/liflow_db
    # Redis
    REDIS_HOST=localhost
    REDIS_PORT=6379
    REDIS_PASSWORD=secure_redis_pass
    # Mail
    MAIL_HOST=smtp.gmail.com
    MAIL_PORT=587
    MAIL_USERNAME=user@example.com
    MAIL_PASSWORD=your-app-password
    # JWT
    JWT_EXPIRATION_MS=3600000
    JWT_REFRESH_EXPIRATION_MS=86400000
    # Graylog
    GRAYLOG_HOST=localhost
    GRAYLOG_PORT=12201
    GRAYLOG_PASSWORD_SECRET=your-graylog-secret
    GRAYLOG_ROOT_PASSWORD_SHA2=your-graylog-root-password-sha2
    # Application
    FRONTEND_URL=http://localhost:3000
   ```

3. **Vault secret'larını yapılandırın:**

   ```bash
   # RSA key çifti otomatik olarak üretilir ve Vault'a yazılır
   ./scripts/setup-vault-secrets.sh
   ```

   > **Not:** Bu script `.env` dosyasındaki tüm konfigürasyonları Vault'a yazar, RSA key çiftini otomatik üretir ve güvenlik
   için yerel diskten siler.

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

> 💡 **Detaylı script bilgileri için:** [`scripts/README.md`](scripts/README.md) dosyasına bakabilirsiniz.

## API Kullanımı

### Temel Endpointler

- **POST** `/api/auth/register` - Yeni kullanıcı kaydı (ad, soyad, email, şifre)
- **POST** `/api/auth/login` - Kullanıcı girişi ve JWT token alma (email, şifre)
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

#### **1️⃣ Kayıt Ol**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

#### **2️⃣ Giriş Yap & Token Al**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

#### **3️⃣ Token Kara Listeye Alarak Çıkış Yap**

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <your_jwt_token>"
```

#### **4️⃣ JWK Set Bilgilerini Al**

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
- **Ad-Soyad Normalizasyonu:** İsimler otomatik olarak baş harfleri büyük olacak şekilde normalize edilir

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
