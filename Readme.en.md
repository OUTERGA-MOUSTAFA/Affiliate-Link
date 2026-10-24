<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=spring" alt="Spring Boot 4.1.1">
  <img src="https://img.shields.io/badge/PostgreSQL-18-blue?logo=postgresql" alt="PostgreSQL 18">
  <img src="https://img.shields.io/badge/Flyway-12-red?logo=flyway" alt="Flyway">
  <img src="https://img.shields.io/badge/license-MIT-lightgrey" alt="License MIT">
</p>

<h1 align="center">AffiliateLink — Profile Module</h1>

<p align="center">
  <a href="./README.md">🇫🇷 Version française</a>
</p>

**Profile** module of the AffiliateLink affiliate platform. It manages the identity of every actor on the platform (publishers and advertisers) and their validation lifecycle, handled by an administrator. No offer or affiliation can exist without an account whose profile has been validated.

## Table of contents

- [Features](#features)
- [Business rules](#business-rules)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Data model](#data-model)
- [Quick start](#quick-start)
- [Configuration](#configuration)
- [API](#api)
- [Troubleshooting](#troubleshooting)
- [Tests](#tests)

## Features

- Create a **publisher** account (email, first name, last name, niche, bio, country)
- Create an **advertiser** account (email, company name, sector, website)
- Retrieve a profile by its identifier
- List all publishers / advertisers
- Validate, suspend and reactivate an account as an administrator

## Business rules

| Rule | Description |
|---|---|
| Unique email | The same email cannot be used by both a publisher and an advertiser |
| Available niches | `BEAUTY`, `TECH`, `GAMING`, `TRAVEL`, `LIFESTYLE`, `FOOD`, `SPORT` |
| Default status | Every new account is created with status `PENDING` |
| Deletion | An account is never deleted, only suspended |

### Allowed status transitions

```
PENDING ──activate()──▶ ACTIVE ──suspend()──▶ SUSPENDED
                           ▲                      │
                           └──────reactivate()────┘
```

Any other transition throws an `InvalidStatusTransitionException` (HTTP 409). This logic lives in the `Profile` entity, not in the controllers.

## Tech stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.1 (Spring Framework 7) |
| Persistence | Spring Data JPA / Hibernate ORM 7 |
| Database | PostgreSQL 18 |
| Migrations | Flyway (`spring-boot-starter-flyway` + `flyway-database-postgresql`) |
| Validation | Bean Validation (Jakarta) |
| Local environment | Docker Compose (started automatically by Spring Boot) |
| Build | Maven (wrapper included) |

## Architecture

Organized **by domain**, not by technical layer:

```
com.affiliateLink
├── common
│   ├── ErrorResponse
│   ├── GlobalExceptionHandler        (@RestControllerAdvice)
│   ├── ResourceNotFoundException     → 404
│   ├── DuplicateEmailException       → 409
│   └── InvalidStatusTransitionException → 409
└── profile
    ├── controller
    │   ├── PublisherController
    │   ├── AdvertiserController
    │   └── AdminProfileController    (🚧 in progress)
    ├── dto
    │   ├── PublisherCreateRequest / PublisherResponse
    │   ├── AdvertiserCreateRequest / AdvertiserResponse
    │   └── StatusUpdateRequest
    ├── entity
    │   ├── Profile (abstract)
    │   ├── Publisher
    │   ├── Advertiser
    │   ├── ProfileStatus (enum)
    │   └── Niche (enum)
    ├── repository
    │   ├── ProfileRepository
    │   ├── PublisherRepository
    │   └── AdvertiserRepository
    └── service
        ├── PublisherService
        └── AdvertiserService
```

Each layer has a single responsibility:

- **Controller**: exposes the REST API, delegates everything to the service
- **Service**: orchestrates business logic (email uniqueness, transition orchestration)
- **Entity**: owns the invariant business rules (default status, transitions)
- **DTO**: the only objects exposed by the API — JPA entities are never serialized directly

## Data model

JPA inheritance in **JOINED** strategy: one shared table, one table per profile type.

```
profile_profiles (id, email UNIQUE, status, created_at)
        │
        ├── profile_publishers (id FK, first_name, last_name, niche, bio, country)
        └── profile_advertisers (id FK, company_name, sector, website)
```

Tables are created and versioned through Flyway (`src/main/resources/db/migration`), with Hibernate in `ddl-auto: validate` mode — no schema change is ever made at runtime.

## Quick start

### Prerequisites

- JDK 17+
- Docker Desktop (running)

### Run the application

```bash
./mvnw spring-boot:run
```

Spring Boot automatically detects `docker-compose.yml` and starts PostgreSQL before launching the application. Flyway applies the migrations on startup.

The API is available at `http://localhost:8080`.

### Inspect the database

```bash
docker exec -it affiliatelink-postgres-1 psql -U postgres -d affiliatelink
```

```sql
\dt
```

## Configuration

### `docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:18
    environment:
      POSTGRES_DB: affiliatelink
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: admin
    ports:
      - "5433:5432"
    volumes:
      - affiliatelink-data:/var/lib/postgresql

volumes:
  affiliatelink-data:
```

### `application.yml`

```yaml
spring:
  docker:
    compose:
      enabled: true
      lifecycle-management: start-and-stop
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
```

## API

| Method | Endpoint | Responses |
|---|---|---|
| `POST` | `/api/publishers` | `201` / `400` / `409` |
| `GET` | `/api/publishers` | `200` |
| `GET` | `/api/publishers/{id}` | `200` / `404` |
| `POST` | `/api/advertisers` | `201` / `400` / `409` |
| `GET` | `/api/advertisers` | `200` |
| `GET` | `/api/advertisers/{id}` | `200` / `404` |
| `PATCH` | `/api/admin/profiles/{id}/status` | `204` / `404` / `409` 🚧 |

### Example — create a publisher

```http
POST /api/publishers
Content-Type: application/json

{
  "email": "ahmed.tech@mail.com",
  "firstName": "Ahmed",
  "lastName": "Benali",
  "niche": "TECH",
  "bio": "Tech content creator",
  "country": "Morocco"
}
```

`201 Created` response:

```json
{
  "id": 1,
  "email": "ahmed.tech@mail.com",
  "status": "PENDING",
  "createdAt": "2026-10-21T17:12:43",
  "firstName": "Ahmed",
  "lastName": "Benali",
  "niche": "TECH",
  "bio": "Tech content creator",
  "country": "Morocco"
}
```

### Error format

```json
{
  "message": "An account already exists with email: ahmed.tech@mail.com"
}
```

## Troubleshooting

**`PostgreSQL Database directory appears to contain a database; Skipping initialization`**
An existing Docker volume still holds data from a previous attempt. Remove the volume (`docker volume rm affiliatelink_affiliatelink-data`) to start from a clean state.

**`Unsupported Database: PostgreSQL X.X`**
Check that the `flyway-database-postgresql` dependency is present in `pom.xml`, alongside `spring-boot-starter-flyway`.

**PostgreSQL 18 and the data volume**
Starting with the `postgres:18` image, mount the volume on `/var/lib/postgresql` (not `/var/lib/postgresql/data`) — the image manages the versioned subdirectory itself.

## Tests

```bash
./mvnw test
```
---

<p align="center"><sub>Sprint 3 — Brief 2 · AffiliateLink</sub></p>