# Tesis Yorum

Kullanıcıların tesisler hakkında yorum yapabildiği, yorumlara belirli tip ve boyutlarda dosya ekleyebildiği, yorumların onay sürecinden geçerek yayınlandığı bir backend uygulaması.

## 🚀 Proje Kurulumu

### Gereksinimler
- Java 17 veya üzeri
- Maven 3.6+
- Git

### Kurulum Adımları

1. **Projeyi klonlayın:**
```bash
git clone [your-repo-url]
cd tesis_yorum
```

2. **Bağımlılıkları yükleyin:**
```bash
./mvnw clean install
```

3. **Uygulamayı başlatın:**
```bash
./mvnw spring-boot:run
```

4. **Uygulama şu adreste çalışır:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API Docs: http://localhost:8080/v3/api-docs
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sas`
  - Password: 123

## 📚 API Dokümantasyonu

### 🔗 Swagger UI
Tüm API endpoint'lerini interaktif olarak test edebilirsiniz:
**http://localhost:8080/swagger-ui/index.html**

### 📋 OpenAPI Specification
JSON formatında API dokümantasyonu:
**http://localhost:8080/v3/api-docs**

## 📚 API Uç Noktaları

### Kullanıcı İşlemleri
```
GET    /api/users                    # Tüm kullanıcıları listele
POST   /api/users                    # Yeni kullanıcı oluştur
GET    /api/users/{id}               # Kullanıcı detayı
PUT    /api/users/{id}               # Kullanıcı güncelle
DELETE /api/users/{id}               # Kullanıcı sil
```

### Tesis İşlemleri
```
GET    /api/facilities               # Tüm tesisleri listele
POST   /api/facilities               # Yeni tesis oluştur
GET    /api/facilities/{id}          # Tesis detayı
GET    /api/facilities/type/{type}   # Türe göre tesisler
GET    /api/facilities/search?q=...  # Tesis arama
```

### Yorum İşlemleri
```
POST   /api/reviews                  # Yeni yorum (dosya ile)
POST   /api/reviews/simple           # Yeni yorum (sadece JSON)
GET    /api/reviews                  # Onaylı yorumları listele
GET    /api/reviews/{id}             # Yorum detayı
GET    /api/reviews/facility/{id}    # Tesise ait yorumlar
GET    /api/reviews/user/{id}        # Kullanıcının yorumları
PUT    /api/reviews/{id}             # Yorum güncelle
DELETE /api/reviews/{id}             # Yorum sil
```

### Admin İşlemleri
```
GET    /api/admin/reviews/pending    # Bekleyen yorumlar
POST   /api/admin/reviews/{id}/approve  # Yorumu onayla
POST   /api/admin/reviews/{id}/reject   # Yorumu reddet
GET    /api/admin/dashboard/stats    # Dashboard istatistikleri
```

### Dosya İşlemleri
```
GET    /api/files/{filename}         # Dosya görüntüle
GET    /api/files/{filename}/download # Dosya indir
GET    /api/files/review/{reviewId}  # Yorumun dosyaları
```

## Dosya Yükleme Kuralları

### ✅ İzin Verilen Formatlar
- **JPEG** (.jpg, .jpeg)
- **PNG** (.png)

### Boyut Limitleri
- **Maksimum dosya boyutu:** 10 MB
- **Content-Type kontrolü:** Dosya uzantısı ile eşleşmeli

### ükleme Örnekleri

**Multipart Form ile Yorum + Dosya:**
```bash
curl -X POST http://localhost:8080/api/reviews \
  -F "userId=1" \
  -F "facilityId=1" \
  -F "content=Harika bir yer!" \
  -F "rating=5" \
  -F "files=@image1.jpg" \
  -F "files=@image2.png"
```

**JSON ile Sadece Yorum:**
```bash
curl -X POST http://localhost:8080/api/reviews/simple \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "facilityId": 1,
    "content": "Çok güzel bir deneyimdi",
    "rating": 4
  }'
```

## Yorum Onay Süreci

1. **Kullanıcı yorum oluşturur** → Status: `PENDING`
2. **Admin yorumu inceler:**
   - Onaylar → Status: `APPROVED` (görünür olur)
   - Reddeder → Status: `REJECTED` (admin notu ile)

## Veritabanı Şeması

### Ana Tablolar
- **users** - Kullanıcı bilgileri
- **facilities** - Tesis bilgileri  
- **reviews** - Yorumlar
- **file_attachments** - Dosya ekleri

### Test Verileri
Uygulama başladığında otomatik olarak şunlar oluşturulur:
- 2 kullanıcı (1 normal, 1 admin)
- 2 tesis (otel ve restoran)
- 2 örnek yorum

## Kullanım Senaryoları

> **💡 İpucu:** Tüm API endpoint'lerini Swagger UI'de interaktif olarak test edebilirsiniz: http://localhost:8080/swagger-ui/index.html

### 1. Yeni Kullanıcı Kaydı
```json
POST /api/users
{
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "USER"
}
```

### 2. Tesis Arama
```
GET /api/facilities/search?q=hotel
GET /api/facilities/type/RESTAURANT
GET /api/facilities/city/Istanbul
```

### 3. Dosyalı Yorum Oluşturma
- Multipart form kullanarak
- Maksimum 10MB JPEG/PNG dosyalar
- Otomatik `PENDING` status

### 4. Admin Onay İşlemleri
```json
POST /api/admin/reviews/1/approve?adminId=2

POST /api/admin/reviews/2/reject?adminId=2
{
  "adminNotes": "Uygunsuz içerik"
}
```

## 🛠️ Geliştirme Notları

### Teknoloji Stack
- **Backend:** Spring Boot 3.x, Java 17
- **Veritabanı:** H2 (in-memory)
- **ORM:** Spring Data JPA + Hibernate
- **Validation:** Bean Validation
- **File Upload:** MultipartFile
- **API Docs:** Swagger/OpenAPI 3


### Test Etmek İçin
1. Uygulamayı başlat
2. **Swagger UI'yi ziyaret et:** http://localhost:8080/swagger-ui/index.html
3. H2 console'dan test verilerini kontrol et: http://localhost:8080/h2-console
4. Swagger UI'de API endpoint'lerini interaktif olarak test et
5. Dosya yükleme işlemlerini `/api/reviews` endpoint'i ile dene


## 🚦 Durum Kodları

- **200** - Başarılı
- **201** - Oluşturuldu
- **400** - Geçersiz istek/dosya formatı
- **403** - Yetkisiz erişim
- **404** - Bulunamadı
- **409** - Çakışma (duplicate)
- **500** - Sunucu hatası
