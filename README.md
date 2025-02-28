# Auth Service - Modern Kimlik Doğrulama Servisi

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-Latest-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE.txt)

## Özet

**Auth Service**, modern uygulamalar için güvenli kimlik doğrulama ve yetkilendirme hizmeti sunan, **Spring Boot 3** ile
geliştirilmiş, ölçeklenebilir bir servistir. JWT tabanlı oturum yönetimi, kullanıcı yetkilendirme ve güvenlik mekanizmalarını
kapsayan bu servis, mikroservis mimarilerine kolayca entegre edilebilir.

## Özellikler

- 🔐 **JWT tabanlı kimlik doğrulama**
- 👥 **Kullanıcı ve rol tabanlı yetkilendirme**
- 🔄 **Token yenileme mekanizması**
- 🛡️ **Spring Security ile tam entegrasyon**
- 📝 **Swagger/OpenAPI dokümantasyonu**
- 🐳 **Docker desteği**
- 🎯 **RESTful API mimarisi**
- ✅ **Girdi doğrulama ve hata yönetimi**
- 🗃️ **MySQL veritabanı entegrasyonu**

## Gereksinimler

- **Java 21+**
- **Docker & Docker Compose**
- **Maven 3.6+**
- **MySQL 8.0+**

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
   ```

3. **Docker kullanarak MySQL veritabanını başlatın:**
   ```bash
   docker-compose up -d
   ```

4. **Uygulamayı derleyin:**
   ```bash
   ./mvnw clean install
   ```

5. **Uygulamayı çalıştırın:**
   ```bash
   ./mvnw spring-boot:run
   ```

📌 **Servis çalıştığında:** `http://localhost:8080` adresinde kullanıma hazır olacaktır.

## API Kullanımı

### Endpointler

- **POST** `/api/v1/auth/register` - Yeni kullanıcı kaydı
- **POST** `/api/v1/auth/login` - Kullanıcı girişi ve JWT token alma
- **POST** `/api/v1/auth/refresh` - Token yenileme
- **POST** `/api/v1/auth/logout` - Kullanıcı çıkışı

🔹 **API dokümantasyonu:**

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Örnek API İsteği

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password123"
  }'
```

## Yapılandırma

Uygulama, `application.yml` ve ortam değişkenleri ile esnek bir şekilde yapılandırılabilir.

### Temel Yapılandırma Parametreleri

- `server.port`: **Uygulama portu** (Varsayılan: 8080)
- `spring.datasource.url`: **Veritabanı bağlantı URL'i**
- `app.jwt.expiration-ms`: **JWT token geçerlilik süreci**
- `app.jwt.refresh-expiration-ms`: **Yenileme token geçerlilik süreci**

## Katkıda Bulunma

Projeye katkı sağlamak için:

1. Projeyi **fork** edin
2. Yeni bir **branch** oluşturun (`git checkout -b feature/yeni-ozellik`)
3. Değişiklikleri **commit** edin (`git commit -m 'Yeni özellik eklendi'`)
4. Branch'inizi **push** edin (`git push origin feature/yeni-ozellik`)
5. Bir **Pull Request** oluşturun

🔹 PR'nızın aşağıdaki şartlara uygun olmasına dikkat edin:

- Kod stiline uygunluk
- Dokümantasyon eklenmesi
- Yeterli test kapsamı

## Lisans

Bu proje **MIT Lisansı** ile lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakabilirsiniz.

## Teşekkürler

- [Spring Boot](https://spring.io/projects/spring-boot)
- [JSON Web Tokens](https://jwt.io/)
- [Spring Security](https://spring.io/projects/spring-security)
- [MySQL](https://www.mysql.com/)
- [Docker](https://www.docker.com/)
- [Swagger/OpenAPI](https://swagger.io/)

## Destek

Sorularınız ve geri bildirimleriniz için GitHub'da bir **issue** oluşturabilirsiniz.

---
💙 **Modern kimlik doğrulama sisteminizi güvenli hale getirin!**