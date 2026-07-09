# Dogster

A mobile-focused pet sitting application that connects pet owners with pet sitters. Includes a Spring Boot backend and a Flutter mobile client.

## Features

- User registration and email verification
- Pet profile creation with photo upload
- Sitting post creation, listing, and matching
- Location-based nearby post search
- WebSocket messaging between matched users

## Tech Stack

**Backend:** Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL, Liquibase, WebSocket/STOMP

**Mobile:** Flutter

**Infrastructure:** Docker Compose

## Requirements

- JDK 21
- Maven
- Docker
- Flutter SDK (for the mobile client)

## Setup

Start PostgreSQL:

```bash
docker compose up -d
```

Run the backend:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs on `http://localhost:8080` by default.

## Mobile client

```bash
cd mobile
flutter run
```

On the Android emulator, the backend is reachable at `http://10.0.2.2:8080`.

For a physical device or a custom URL:

```bash
flutter run --dart-define=DOGSTER_API_BASE_URL=http://<host>:8080 --dart-define=DOGSTER_WS_URL=ws://<host>:8080/ws
```

## Tests

```bash
./mvnw test
```

PostgreSQL integration test (requires Docker):

```bash
./mvnw -Dtest=PostgresLiquibaseIT test
```

## Project structure

```text
.
├── src/           # Spring Boot backend
├── mobile/        # Flutter application
└── docker-compose.yml
```
