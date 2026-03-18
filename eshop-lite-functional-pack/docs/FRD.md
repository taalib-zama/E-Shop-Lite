# Functional Requirements Document (FRD)

**Project:** E‑Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-03

---

## 1. Product Overview
E‑Shop Lite is a production‑style, cloud‑ready **microservices backend** designed for learning and portfolio building using **free & open‑source** tooling. The MVP focuses on Identity & Access and Product Catalog while establishing the platform foundations (Gateway, Config, Discovery, Observability).

## 2. Scope
### 2.1 In Scope (MVP)
- Identity & Access: Registration, Login (JWT), roles (`USER`, `ADMIN`).
- Catalog: Create (ADMIN), List/Search, Get‑by‑ID.
- Platform: Spring Cloud Gateway (routing, JWT, rate limit), Eureka, Config Server.
- Observability: Prometheus metrics, Loki logs, Jaeger traces.
- Persistence: PostgreSQL per service; JPA/Hibernate; Flyway.

### 2.2 Out of Scope (MVP)
- Orders, Inventory, Payments, Notifications.  
- Email verification & password reset (stubs later).  
- Product Update/Delete (will come after Sprint 1).

## 3. Actors
- **Visitor** (unauthenticated)  
- **User** (authenticated)  
- **Admin** (authenticated, elevated)  
- **System** (Gateway, Config Server, Service Registry)

## 4. Business Processes
1. **Signup**: Visitor → `POST /users` → user created (role `USER`).
2. **Login**: User → `POST /auth/login` → short‑lived JWT issued (15m).
3. **Create Product**: Admin → `POST /products` → persisted with unique SKU.
4. **Browse/Search**: Visitor/User → `GET /products` with filters/pagination.
5. **Product Detail**: Visitor/User → `GET /products/{id}`.

## 5. Functional Requirements
- **FR‑01 Registration**: Unique email; strong password (min 8; upper/lower/number/special). On success return 201 with sanitized user payload (no secret fields).
- **FR‑02 Login**: On valid credentials, return JWT with claims `sub`, `role`, `iat`, `exp` (=15m). On invalid, 401.
- **FR‑03 Product Create**: Require `name`, `sku` (unique), `price` (> 0); default `currency=INR`.
- **FR‑04 Catalog List/Search**: Query on `name`/`description` (case‑insensitive), filters (`category`, `minPrice`, `maxPrice`), pagination (`page`,`size`), sorting (`sort`, default `createdAt,DESC`).
- **FR‑05 Error Model**: Standard Problem‑Details‑style schema across services.
- **FR‑06 Gateway**: Enforce JWT on protected routes; inject/propagate `traceId`; apply basic rate limiting (per‑IP, dev only).

## 6. Non‑Functional Requirements
- **Performance**: P95 read < 250 ms; write < 500 ms (dev, ≤100 RPS).
- **Availability**: 99.9% target (single region, dev)
- **Scalability**: Stateless services; horizontal‑scale ready.
- **Security**: BCrypt hashing; JWT validation at gateway & service; input validation.
- **Observability**: Actuator; JSON logs with `traceId`; Micrometer metrics; OTel traces to Jaeger.
- **Data**: ACID per service; UUID PKs; audit timestamps.

## 7. API Contracts (References)
- `docs/openapi/user-service.yaml`  
- `docs/openapi/catalog-service.yaml`

### 7.1 Error Schema (standard)
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

## 8. Data Model
### 8.1 users_db.users
- `id UUID PK`, `name VARCHAR(100)`, `email VARCHAR(255) UNIQUE`, `password_hash VARCHAR(255)`, `role VARCHAR(20) DEFAULT 'USER'`, `status VARCHAR(20) DEFAULT 'ACTIVE'`, `created_at TIMESTAMP`, `updated_at TIMESTAMP`.

### 8.2 catalog_db.products
- `id UUID PK`, `name VARCHAR(255)`, `sku VARCHAR(64) UNIQUE`, `price DECIMAL(12,2) CHECK(price>0)`, `currency VARCHAR(3) DEFAULT 'INR'`, `description TEXT`, `category VARCHAR(100)`, `attributes JSONB`, `created_at TIMESTAMP`, `updated_at TIMESTAMP`.

## 9. Security
- Spring Security; JWT (HMAC for dev) with 15‑minute expiry.  
- BCrypt (≥ 10 rounds); do not log secrets.  
- Bean Validation on requests.  
- CORS restricted via Config Server per environment.

## 10. Observability
- **Metrics**: `/actuator/prometheus` scraped by Prometheus → Grafana dashboards.  
- **Logs**: JSON logs → Promtail → Loki (include `traceId`).  
- **Tracing**: Micrometer/OTel exporters → Jaeger; propagate `traceparent`.

## 11. Assumptions, Risks, Dependencies
- **Assumptions**: Docker available; Java 21 & Maven installed.  
- **Risks**: Local resource usage; logging differences by OS.  
- **Dependencies**: Docker images (Postgres, RabbitMQ, Prometheus, Grafana, Loki, Promtail, Jaeger); Spring deps via Maven.

## 12. Acceptance Criteria Summary
See **USER‑STORIES‑S1.md** for Gherkin AC and **TEST‑PLAN.md** for detailed test cases.

## 13. RACI (MVP)
- Requirements & Architecture: Functional Lead (me) with you.  
- Implementation & Tests: You.  
- CI/Infra: Me (seed), You (own).
