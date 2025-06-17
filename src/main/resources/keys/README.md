# RSA Key Dosyaları

Bu dizin RSA private ve public key dosyalarını içerir.

## Key Dosyaları Oluşturma

RSA key çifti oluşturmak için aşağıdaki komutları kullanabilirsiniz:

```bash
# Private key oluşturma (PKCS#8 formatında)
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048

# Public key oluşturma
openssl rsa -pubout -in private.pem -out public.pem
```

## Dosya Formatı

- `private.pem`: PKCS#8 formatında RSA private key
- `public.pem`: X.509 formatında RSA public key

## Güvenlik

- Private key dosyaları güvenli bir şekilde saklanmalı ve versiyon kontrolüne eklenmemelidir
- Production ortamında key'ler HashiCorp Vault veya benzeri güvenli bir key management sisteminde saklanmalıdır

## Kullanım

Eğer bu dosyalar mevcut değilse, uygulama otomatik olarak runtime'da yeni bir RSA key çifti oluşturacaktır.

Key dosyalarını kullanmak için application.yml'de:

```yaml
app:
  jwt:
    rsa:
      private-key: classpath:keys/private.pem
      public-key: classpath:keys/public.pem
```
