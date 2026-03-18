# System Design — E‑Shop Lite

**Version:** 1.0.0  
**Date:** 2026-03-03

---

## 1. Goals & Constraints

### 1.1 Goals
- Production‑style microservices backend using **free & open‑source** stack.
- Clean separation (gateway, config, discovery, services), **ready for orders/inventory** later.
- **Security** and **observability** from day one.

### 1.2 Constraints & Assumptions
- Local‑first dev with Docker Compose; later lift to K8s if needed.
- Per‑service database (no cross‑service joins).
- No paid managed cloud services.

### 1.3 Quality Attributes (prioritized)
1) Modularity/Evolvability  2) Security  3) Observability  4) Performance  5) Reliability

---

## 2. High‑Level Architecture (C4: System + Containers)

**Stack:** Java 21 · Spring Boot 3 · Spring Cloud (Gateway, Eureka, Config) · PostgreSQL · JPA/Hibernate · Flyway · RabbitMQ · Prometheus · Grafana · Loki/Promtail · Jaeger

**MVP services:** `api-gateway`, `config-server`, `service-registry`, `user-service`, `catalog-service`

```mermaid
flowchart LR
  Client[Client (Postman/UI)] -->|HTTP| Gateway[API Gateway]
  subgraph Platform
    Gateway --> Eureka[(Service Registry)]
    Gateway --> Config[Config Server]
  end

  Gateway --> U[User Service]
  Gateway --> C[Catalog Service]

  U -->|JPA| PG[(PostgreSQL users_db)]
  C -->|JPA| PG2[(PostgreSQL catalog_db)]

  subgraph Observability
    Prom[Prometheus]:::obs --> Graf[Grafana]:::obs
    Loki[Loki]:::obs
    Jaeg[Jaeger]:::obs
  end

  U -.metrics.-> Prom
  C -.metrics.-> Prom
  Gateway -.metrics.-> Prom
  U -.logs.-> Loki
  C -.logs.-> Loki
  Gateway -.logs.-> Loki
  U -.traces.-> Jaeg
  C -.traces.-> Jaeg
  Gateway -.traces.-> Jaeg

  classDef obs fill:#eef,stroke:#88f,stroke-width:1px;
```

---

## 3. Service Responsibilities

### 3.1 API Gateway
- Routing, JWT validation, correlation ID injection, basic rate limiting (dev).
- Expose `/actuator/*` for health/metrics.

### 3.2 Config Server
- Centralized configuration (Git‑backed local repo).
- Environment‑specific profiles, secrets via env vars.

### 3.3 Service Registry (Eureka)
- Dynamic discovery; Gateway uses service IDs for routing.

### 3.4 User Service
- Register, Login (JWT), `/users/me`.
- BCrypt password hashing, email uniqueness.

### 3.5 Catalog Service
- Product create (ADMIN), list/search, get by id.
- Query on `name`/`description`, filters (category/price), pagination/sort.

---

## 4. API Design & Contracts

**Naming & versioning:** `/api/v1/*` via Gateway.

**User Service**
- `POST /users` → 201
- `POST /auth/login` → 200 { JWT }
- `GET /users/me` → 200 (secured)

**Catalog Service**
- `POST /products` (ADMIN) → 201
- `GET /products` → 200 (filters/pagination)
- `GET /products/{id}` → 200/404

**Error Schema (standard)**
```json
{
  "type": "https://eshop-lite/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "Field constraints violated",
  "traceId": "a1b2c3",
  "errors": [{"field":"email","message":"must be a valid email"}]
}
```

OpenAPI specs live in `docs/openapi/`.

---

## 5. Data Model & Storage

**users_db.users**: `id UUID PK`, `name`, `email UNIQUE`, `password_hash`, `role`, `status`, `created_at`, `updated_at`.

**catalog_db.products**: `id UUID PK`, `name`, `sku UNIQUE`, `price DECIMAL(12,2)`, `currency`, `description`, `category`, `attributes JSONB`, `created_at`, `updated_at`.

**Indexes**: `users.email`, `products.sku`, `products.category` (+ optional GIN on attributes later).

**Migrations**: Flyway per service; `V1__init.sql` committed.

---

## 6. Messaging & Eventing (Future)

- Broker: RabbitMQ.
- When Orders arrive: Outbox pattern to publish `order.created`, `payment.succeeded/failed`.
- DLQ for poison messages; JSON events with versioning.

---

## 7. Security Architecture

- **AuthN**: JWT (HMAC secret in dev), TTL 15m.
- **AuthZ**: Role‑based at Gateway and service.
- **Validation**: Bean Validation; Problem Details error model.
- **Secrets**: Env vars; never commit secrets.
- **CORS**: Restrict to dev origins via Config Server.
- **Rate limiting**: In‑memory token bucket at Gateway (dev).

---

## 8. Observability

- **Metrics**: Micrometer → Prometheus → Grafana.
- **Logs**: JSON via Logback → Promtail → Loki (include `traceId`).
- **Tracing**: Micrometer Tracing/OTel → Jaeger; propagate `traceparent`.
- **Health**: `/actuator/health`, `/actuator/prometheus`.

---

## 9. Resilience & Performance

- Timeouts at clients (1s connect, 3s read), small retries at Gateway.
- Circuit breaker (dev‑level).
- DB pool per service; JVM heap 512–768 MB dev.
- Targets: p95 reads < 250 ms; writes < 500 ms.

---

## 10. Deployment Topology & Environments

- **Local**: Docker Compose for infra; services via `mvn spring-boot:run`.
- **Free Cloud (optional later)**: Render/Railway/Fly.io; same Config Server pattern.
- **Ports**: Gateway 8080 · User 8081 · Catalog 8082 · Prometheus 9090 · Grafana 3000 · Loki 3100 · Jaeger 16686.

---

## 11. Delivery Plan

- **Sprint 0**: Infra up, platform scaffolds, CI seed, error model.
- **Sprint 1**: User register/login, Catalog create/list/get, Gateway security, observability wiring.
- **Sprint 2 (preview)**: Orders/Inventory start, Outbox, RabbitMQ events; optional AI modules.

---

## 12. Optional AI Modules (Non‑blocking for MVP)

- **Semantic Search (vector) — search-service + Qdrant**
- **AI Product Descriptions — content-service + local LLM**
- **Chat‑Shopping Assistant — assistant-service (RAG)**

Implementation can be gated by feature flags and introduced in Sprint 2+.
