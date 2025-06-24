## 🔐 Vault Setup Script

Bu script, **Liflow** uygulamasının ihtiyaç duyduğu tüm **secret** verilerini HashiCorp Vault üzerine güvenli şekilde yükler.
RSA key çifti otomatik olarak üretilir, Vault’a yazılır ve yerel diskten silinir.

---

## 🚀 Kullanım

### 1. Ortam Değişkenlerinin Yüklenme Sırası

Script konfigürasyon değerlerini şu öncelik sırasına göre okur:

1. `ENV_FILE` ortam değişkeni ile belirtilen dosya (örn: `./dev.env`)
2. Varsayılan `.env` dosyası (`liflow-app/.env`)
3. Tanımsız değişkenler varsa script çalışmayı durdurur.

---

### 2. `.env` Dosyası Örneği

Proje kök dizininde (`liflow-app/`) aşağıdaki gibi bir `.env` dosyası oluşturun:

```dotenv
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

> 🔐 **Güvenlik:** `.env` dosyasını `.gitignore` içinde tuttuğunuzdan emin olun.

---

### 3. Script’i Çalıştırma

```bash
# 1. Varsayılan .env dosyası ile çalıştırma
./scripts/setup-vault-secrets.sh

# 2. Bash ile doğrudan çalıştırma
bash scripts/setup-vault-secrets.sh

# 3. Özel bir .env dosyası ile çalıştırma
ENV_FILE=./secrets/dev.env ./scripts/setup-vault-secrets.sh
```

---

## ✅ Özellikler

* Ortam değişkenlerini `.env` veya özel dosyadan okur
* RSA private & public key çifti üretir
* RSA key’leri Vault’a kaydeder, sonra yerelden siler
* Vault bağlantısını doğrular
* Tüm secret’ları `secret/liflow-app` altında merkezi olarak yazar
* Eksik konfigürasyonları yakalar ve kullanıcıyı uyarır

---

## ⚠️ Güvenlik Uyarıları

* `VAULT_TOKEN` gibi hassas değerleri terminal geçmişinizde tutmayın
* `.env` dosyasını `git` ile paylaşmayın (`.gitignore` içinde olmalı)
* Üretimde Vault Token yerine **AppRole, AWS IAM Auth veya Kubernetes Auth** kullanın
* RSA key’leri sadece Vault içinde tutun, **diskte bırakmayın**
* RSA key üretimi sadece dev/local ortam içindir — prod ortamlar için **external key manager** kullanın

---

## 📁 Dosya Yapısı

```
liflow-app/
├── scripts/
│   └── setup-vault-secrets.sh   # → Tüm secret’ları Vault’a yazar
│   └── README.md                # → Bu dokümantasyon
├── .env                         # → Ortam değişkenleri (gizli)
```