# E-Shop Lite

> A production-style microservices e-commerce backend — built entirely with free and open-source tooling for learning and portfolio demonstration.

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-6DB33F?logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-6DB33F?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## Table of Contents

- [What is E-Shop Lite?](#what-is-e-shop-lite)
- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Services and APIs](#services-and-apis)
- [Data Model](#data-model)
- [Security Model](#security-model)
- [Observability Stack](#observability-stack)
- [Prerequisites](#prerequisites)
- [Quickstart](#quickstart)
- [Port Reference](#port-reference)
- [Configuration](#configuration)
- [Roadmap](#roadmap)
- [Contributing](#contributing)

---

## What is E-Shop Lite?

E-Shop Lite is a **production-style microservices backend** designed to demonstrate real-world engineering patterns using only free and open-source tools. It is not a toy app — it is structured like a real system with proper separation of concerns, security controls, observability, and a planned delivery roadmap across multiple sprints.

**Goals:**
- Learn and demonstrate microservices architecture in Java
- Showcase Spring Cloud patterns: Gateway, Eureka, Config Server, Feign
- Practice production habits: JWT auth, BCrypt, structured logging, distributed tracing, CI/CD
- Build a portfolio-ready, runnable backend from scratch

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Client (Postman / UI)                │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              API Gateway  :8080                             │
│  JWT validation · Routing · Correlation ID · Rate limit     │
└───────┬──────────────────────────┬──────────────────────────┘
        │                          │
        ▼                          ▼
┌───────────────┐          ┌───────────────────┐
│ User Service  │          │  Catalog Service  │
│    :8081      │          │      :8082        │
│ register      │          │ create product    │
│ login (JWT)   │          │ list / search     │
│ /users/me     │          │ get by id         │
└──────┬────────┘          └────────┬──────────┘
       │ JPA                        │ JPA
       ▼                            ▼
┌─────────────┐             ┌──────────────┐
│  users_db   │             │  catalog_db  │
│ (Postgres)  │             │  (Postgres)  │
└─────────────┘             └──────────────┘

        ┌───────────────────┐   ┌──────────────────┐
        │  Config Server    │   │ Service Registry │
        │     :8888         │   │  Eureka  :8761   │
        │ Git-backed config │   │ Dynamic discovery│
        └───────────────────┘   └──────────────────┘

        ┌──────────────────────────────────────────┐
        │           Observability Stack            │
        │  Prometheus :9090 → Grafana :3000        │
        │  Loki :3100  ·  Jaeger :16686            │
        └──────────────────────────────────────────┘
```

All gateway routes use **service discovery** via `lb://USER-SERVICE` and `lb://CATALOG-SERVICE` — no hardcoded IPs.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Cloud Platform | Spring Cloud 2023.0.3 (Gateway, Eureka, Config, Feign) |
| Security | Spring Security + JJWT 0.12.5 (HMAC JWT) + BCrypt |
| Persistence | PostgreSQL 16 · Spring Data JPA · Flyway migrations |
| Messaging (planned) | RabbitMQ |
| Observability | Micrometer · Prometheus · Grafana · Loki · Promtail · Jaeger (OTel) |
| Testing | JUnit 5 · Mockito · Testcontainers |
| Build | Maven (multi-module) |
| Local Infra | Docker Compose |
| Container Registry | GitHub Container Registry (GHCR) |
| Deployment (planned) | Kubernetes (Kind) · Kustomize · Fly.io · Linkerd |

---

## Project Structure

```
E-Shop-Lite/
├── eshop-lite/                        # Runnable multi-module Maven project
│   ├── platform/
│   │   ├── api-gateway/               # Spring Cloud Gateway
│   │   ├── config-server/             # Centralised configuration server
│   │   └── service-registry/          # Eureka discovery server
│   ├── services/
│   │   ├── user-service/              # Registration, login, JWT
│   │   └── catalog-service/           # Product management
│   ├── config-repo/                   # Per-service application.yml configs
│   ├── infra/
│   │   ├── docker-compose.yml         # Full local infra stack
│   │   ├── prometheus.yml             # Prometheus scrape config
│   │   └── initdb/                    # Postgres DB initialisation scripts
│   └── postman/                       # Postman smoke collection
│
├── eshop-lite-system-design/          # Architecture and design documents
├── eshop-lite-sprint-details/         # Sprint architecture and plan notes
└── eshop-lite-functional-pack/        # FRD, user stories, test plans, traceability
    ├── US-1.md … US-25.md             # Implementation-ready story docs
    ├── SPRINT-1-TRACEABILITY.md … SPRINT-5-TRACEABILITY.md
    └── docs/
        ├── FRD.md
        ├── USER-STORIES-S1.md … USER-STORIES-S5.md
        └── TEST-PLAN.md … TEST-PLAN-S5.md
```

---

## Services and APIs

### API Gateway — `:8080`
Single entry point for all client traffic.

| Responsibility | Detail |
|---|---|
| Routing | `lb://USER-SERVICE`, `lb://CATALOG-SERVICE` via Eureka |
| Security | JWT validation on protected routes |
| Observability | Correlation ID injection, trace propagation |
| Rate limiting | In-memory token bucket (dev only) |

---

### User Service — `:8081`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/users` | Public | Register new account |
| `POST` | `/auth/login` | Public | Login and receive JWT |
| `GET` | `/users/me` | Bearer token | Get current user profile |

**Register request:**
```json
{
  "name": "Taalib Z",
  "email": "taalib@example.com",
  "password": "S3cureP@ss!"
}
```

**Login response:**
```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

---

### Catalog Service — `:8082`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/products` | ADMIN token | Create product |
| `GET` | `/products` | Public | List/search with filters |
| `GET` | `/products/{id}` | Public | Get product by UUID |

**List/search query parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `query` | string | — | Search name and description |
| `category` | string | — | Filter by category |
| `minPrice` | decimal | — | Minimum price filter |
| `maxPrice` | decimal | — | Maximum price filter |
| `page` | int | `0` | Page number |
| `size` | int | `20` | Page size (max 100) |
| `sort` | string | `createdAt,DESC` | Sort field and direction |

---

### Error Response Schema (Problem Details)

All services return a consistent error shape:

```json
{
  "type": "https://eshop-lite/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "Field constraints violated",
  "traceId": "a1b2c3",
  "errors": [
    { "field": "email", "message": "must be a valid email" }
  ]
}
```

---

## Data Model

### `users_db.users`

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `name` | VARCHAR(100) | Required |
| `email` | VARCHAR(255) | Unique |
| `password_hash` | VARCHAR(255) | BCrypt |
| `role` | VARCHAR(20) | `USER` or `ADMIN` |
| `status` | VARCHAR(20) | Default `ACTIVE` |
| `created_at` | TIMESTAMP | Auto-set |
| `updated_at` | TIMESTAMP | Auto-updated |

### `catalog_db.products`

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `name` | VARCHAR(255) | Required |
| `sku` | VARCHAR(64) | Unique |
| `price` | DECIMAL(12,2) | Must be > 0 |
| `currency` | VARCHAR(3) | Default `INR` |
| `description` | TEXT | Optional |
| `category` | VARCHAR(100) | Optional |
| `attributes` | JSONB | Optional metadata |
| `created_at` | TIMESTAMP | Auto-set |
| `updated_at` | TIMESTAMP | Auto-updated |

> Migrations are managed by **Flyway** — `V1__init.sql` runs automatically on first startup.

---

## Security Model

```
1. Client  →  POST /auth/login  →  user-service
2. user-service validates credentials (BCrypt), issues JWT (HMAC-signed, 15m TTL)
3. Client sends: Authorization: Bearer <token>
4. API Gateway validates JWT and forwards request with role info
5. Services also validate JWT independently (defense-in-depth)
6. ROLE_ADMIN required for product creation
```

**Key security properties:**
- Passwords hashed with BCrypt (strength 10+)
- JWT claims: `sub`, `role`, `iat`, `exp`
- Tokens expire in 15 minutes
- Secrets sourced from environment variables — never committed
- Consistent `401/403` error payloads with `traceId` for correlation

---

## Observability Stack

| Concern | Tool | How |
|---|---|---|
| **Metrics** | Micrometer → Prometheus → Grafana | `/actuator/prometheus` scraped every 15s |
| **Logs** | Logback JSON → Promtail → Loki | Structured JSON with `traceId` field |
| **Traces** | Micrometer Tracing (OTel) → Jaeger | Spans exported to `localhost:4318` |
| **Health** | Spring Actuator | `/actuator/health` on each service |

All observability infrastructure runs via **Docker Compose** in the `infra/` folder.

Access dashboards locally:
- **Grafana:** http://localhost:3000
- **Prometheus:** http://localhost:9090
- **Jaeger UI:** http://localhost:16686

---

## Prerequisites

| Tool | Minimum Version |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| Docker | 24+ with Compose v2 |
| Postman | Any (for smoke tests) |

---

## Quickstart

### 1. Start infrastructure

```bash
cd eshop-lite/infra
docker compose up -d
```

This starts: **PostgreSQL**, **Jaeger**, **Prometheus**, **Grafana**, **Loki**, **Promtail**.

### 2. Build all modules

```bash
cd eshop-lite
mvn clean package -DskipTests
```

### 3. Start services in order

> Services must start in dependency order — Config Server first.

```bash
# Terminal 1 — Config Server (must be first)
cd platform/config-server
mvn spring-boot:run

# Terminal 2 — Service Registry (Eureka)
cd platform/service-registry
mvn spring-boot:run

# Terminal 3 — API Gateway
cd platform/api-gateway
mvn spring-boot:run

# Terminal 4 — User Service
cd services/user-service
mvn spring-boot:run

# Terminal 5 — Catalog Service
cd services/catalog-service
mvn spring-boot:run
```

### 4. Verify the stack

```bash
# Check gateway health
curl http://localhost:8080/actuator/health

# Register a user
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Taalib","email":"taalib@example.com","password":"S3cureP@ss!"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"taalib@example.com","password":"S3cureP@ss!"}'
```

### 5. Run smoke tests

Import `eshop-lite/postman/E-Shop-Lite.postman_collection.json` into Postman and run the collection against `http://localhost:8080`.

---

## Port Reference

| Service | Port | Description |
|---|---|---|
| API Gateway | `8080` | Primary entry point |
| User Service | `8081` | Identity and auth |
| Catalog Service | `8082` | Product management |
| Config Server | `8888` | Centralised config |
| Service Registry | `8761` | Eureka dashboard |
| PostgreSQL | `5432` | Shared DB host |
| Prometheus | `9090` | Metrics scraper |
| Grafana | `3000` | Dashboards |
| Loki | `3100` | Log aggregation |
| Jaeger UI | `16686` | Distributed traces |

---

## Configuration

Service configuration is centralised in `eshop-lite/config-repo/`.

| File | Service |
|---|---|
| `api-gateway.yml` | API Gateway routes, JWT config |
| `user-service.yml` | DB, JWT, actuator settings |
| `catalog-service.yml` | DB, actuator settings |
| `service-registry.yml` | Eureka server settings |

**Key environment variables:**

| Variable | Description | Default |
|---|---|---|
| `JWT_SECRET` | HMAC signing secret | `dev-secret` (change in production) |
| `SPRING_DATASOURCE_URL` | Postgres connection URL | Configured per service |
| `SPRING_DATASOURCE_USERNAME` | DB username | `eshop` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `eshop` |

> ⚠️ **Never commit real secrets.** Always override `JWT_SECRET` via environment variable in any non-local environment.

---

## Roadmap

| Sprint | Theme | Status |
|---|---|---|
| Sprint 0 | Platform foundations (Gateway, Config, Eureka, Infra) | ✅ Done |
| Sprint 1 | Identity and Catalog (register, login, products) | ✅ Done |
| Sprint 2 | Orders, Inventory, Feign S2S, RabbitMQ, Outbox | 📋 Planned |
| Sprint 3 | Payment, Compensation, Notifications | 📋 Planned |
| Sprint 4 | Hardening, Observability, Reconciliation, CI Gates | 📋 Planned |
| Sprint 5 | Docker, Kubernetes, CD Pipeline, Linkerd mTLS | 📋 Planned |

Full story documentation, FRDs, test plans, and traceability matrices are available in `eshop-lite-functional-pack/`.

---

## Contributing

This is a portfolio/learning project. Feedback and suggestions are welcome.

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/your-feature`
3. Commit using conventional commits: `feat(service): description`
4. Open a pull request with a brief description

---

## License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">
  Built with ☕ Java, 🌱 Spring, and 🐘 PostgreSQL
</div>
