# Dogster

Pet owner ve pet sitter'ları buluşturan mobil odaklı bir pet sitting uygulaması. Spring Boot backend ve Flutter mobil client içerir.

## Özellikler

- Kullanıcı kaydı ve e-posta doğrulama
- Pet profili ve fotoğraf yükleme
- Sitting post oluşturma, listeleme ve eşleşme
- Konuma göre yakın ilan arama
- Eşleşen kullanıcılar arasında WebSocket mesajlaşma

## Teknolojiler

**Backend:** Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL, Liquibase, WebSocket/STOMP

**Mobile:** Flutter

**Altyapı:** Docker Compose

## Gereksinimler

- JDK 21
- Maven
- Docker
- Flutter SDK (mobil client için)

## Kurulum

PostgreSQL'i başlatın:

```bash
docker compose up -d
```

Backend'i çalıştırın:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

API varsayılan olarak `http://localhost:8080` adresinde çalışır.

## Mobil client

```bash
cd mobile
flutter run
```

Android emülatörde backend adresi: `http://10.0.2.2:8080`

Fiziksel cihaz veya özel URL için:

```bash
flutter run --dart-define=DOGSTER_API_BASE_URL=http://<host>:8080 --dart-define=DOGSTER_WS_URL=ws://<host>:8080/ws
```

## Testler

```bash
./mvnw test
```

PostgreSQL ile entegrasyon testi (Docker gerekir):

```bash
./mvnw -Dtest=PostgresLiquibaseIT test
```

## Proje yapısı

```text
.
├── src/           # Spring Boot backend
├── mobile/        # Flutter uygulaması
└── docker-compose.yml
```
