<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=spring" alt="Spring Boot 4.1.1">
  <img src="https://img.shields.io/badge/PostgreSQL-18-blue?logo=postgresql" alt="PostgreSQL 18">
  <img src="https://img.shields.io/badge/Flyway-12-red?logo=flyway" alt="Flyway">
  <img src="https://img.shields.io/badge/license-MIT-lightgrey" alt="License MIT">
</p>

<h1 align="center">AffiliateLink — Module Profile</h1>

<p align="center">
  <a href="Readme.en.md">🇬🇧 English version</a>
</p>

Module **profile** de la plateforme d'affiliation AffiliateLink. Il gère l'identité de tous les acteurs de la plateforme (publishers et advertisers) et leur cycle de validation par un administrateur. Aucune offre ni aucune affiliation ne peut exister sans un compte dont le profil a été validé.

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Règles de gestion](#règles-de-gestion)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Modèle de données](#modèle-de-données)
- [Démarrage rapide](#démarrage-rapide)
- [Configuration](#configuration)
- [API](#api)
- [Dépannage](#dépannage)
- [Tests](#tests)

## Fonctionnalités

- Création de compte **publisher** (email, prénom, nom, niche, bio, pays)
- Création de compte **advertiser** (email, raison sociale, secteur, site web)
- Consultation d'un profil par identifiant
- Consultation de la liste des publishers / advertisers
- Validation, suspension et réactivation d'un compte par un administrateur

## Règles de gestion

| Règle | Description |
|---|---|
| Email unique | Un même email ne peut pas servir à un publisher **et** à un advertiser |
| Niches disponibles | `BEAUTY`, `TECH`, `GAMING`, `TRAVEL`, `LIFESTYLE`, `FOOD`, `SPORT` |
| Statut par défaut | Tout nouveau compte est créé avec le statut `PENDING` |
| Suppression | Un compte n'est jamais supprimé, uniquement suspendu |

### Transitions de statut autorisées

```
PENDING ──activate()──▶ ACTIVE ──suspend()──▶ SUSPENDED
                           ▲                      │
                           └──────reactivate()────┘
```

Toute autre transition lève une `InvalidStatusTransitionException` (HTTP 409). Cette logique est portée par l'entité `Profile`, pas par les contrôleurs.

## Stack technique

| Composant | Technologie |
|---|---|
| Langage | Java 17 |
| Framework | Spring Boot 4.1.1 (Spring Framework 7) |
| Persistance | Spring Data JPA / Hibernate ORM 7 |
| Base de données | PostgreSQL 18 |
| Migrations | Flyway (`spring-boot-starter-flyway` + `flyway-database-postgresql`) |
| Validation | Bean Validation (Jakarta) |
| Environnement local | Docker Compose (démarré automatiquement par Spring Boot) |
| Build | Maven (wrapper inclus) |

## Architecture

Organisation **par domaine**, et non par couche technique :

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
    │   └── AdminProfileController    (🚧 en cours)
    ├── dto
    │   ├── PublisherCreateRequest / PublisherResponse
    │   ├── AdvertiserCreateRequest / AdvertiserResponse
    │   └── StatusUpdateRequest
    ├── entity
    │   ├── Profile (abstraite)
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

Chaque couche a une responsabilité précise :

- **Controller** : expose l'API REST, délègue tout au service
- **Service** : orchestre la logique métier (unicité de l'email, orchestration des transitions)
- **Entity** : porte les règles métier invariantes (statut par défaut, transitions)
- **DTO** : seuls objets exposés par l'API — les entités JPA ne sont jamais sérialisées directement

## Modèle de données

Héritage JPA en stratégie **JOINED** : une table commune, une table par type de profil.

```
profile_profiles (id, email UNIQUE, status, created_at)
        │
        ├── profile_publishers (id FK, first_name, last_name, niche, bio, country)
        └── profile_advertisers (id FK, company_name, sector, website)
```

Les tables sont créées et versionnées via Flyway (`src/main/resources/db/migration`), avec Hibernate en mode `ddl-auto: validate` — aucune modification de schéma n'est faite au runtime.

## Démarrage rapide

### Prérequis

- JDK 17+
- Docker Desktop (lancé)

### Lancer l'application

```bash
./mvnw spring-boot:run
```

Spring Boot détecte automatiquement `docker-compose.yml` et démarre PostgreSQL avant de lancer l'application. Flyway applique les migrations au démarrage.

L'API est accessible sur `http://localhost:8080`.

### Vérifier la base de données

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

| Méthode | Endpoint | Réponses |
|---|---|---|
| `POST` | `/api/publishers` | `201` / `400` / `409` |
| `GET` | `/api/publishers` | `200` |
| `GET` | `/api/publishers/{id}` | `200` / `404` |
| `POST` | `/api/advertisers` | `201` / `400` / `409` |
| `GET` | `/api/advertisers` | `200` |
| `GET` | `/api/advertisers/{id}` | `200` / `404` |
| `PATCH` | `/api/admin/profiles/{id}/status` | `204` / `404` / `409` 🚧 |

### Exemple — créer un publisher

```http
POST /api/publishers
Content-Type: application/json

{
  "email": "ahmed.tech@mail.com",
  "firstName": "Ahmed",
  "lastName": "Benali",
  "niche": "TECH",
  "bio": "Créateur de contenu tech",
  "country": "Maroc"
}
```

Réponse `201 Created` :

```json
{
  "id": 1,
  "email": "ahmed.tech@mail.com",
  "status": "PENDING",
  "createdAt": "2026-10-21T17:12:43",
  "firstName": "Ahmed",
  "lastName": "Benali",
  "niche": "TECH",
  "bio": "Créateur de contenu tech",
  "country": "Maroc"
}
```

### Format des erreurs

```json
{
  "message": "Un compte existe déjà avec l'email : ahmed.tech@mail.com"
}
```

## Dépannage

**`PostgreSQL Database directory appears to contain a database; Skipping initialization`**
Un volume Docker existant contient des données d'une tentative précédente. Supprimer le volume (`docker volume rm affiliatelink_affiliatelink-data`) pour repartir d'un état propre.

**`Unsupported Database: PostgreSQL X.X`**
Vérifier la présence de la dépendance `flyway-database-postgresql` dans `pom.xml`, en plus de `spring-boot-starter-flyway`.

**PostgreSQL 18 et le volume de données**
Depuis l'image `postgres:18`, monter le volume sur `/var/lib/postgresql` (et non `/var/lib/postgresql/data`) — l'image gère elle-même le sous-dossier versionné.

## Tests

```bash
./mvnw test
```
---

<p align="center"><sub>Sprint 3 — Brief 2 · AffiliateLink ·  OUTERGA MOUSTAFA</sub></p>
