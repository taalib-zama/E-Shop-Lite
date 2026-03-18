# Test Plan & Test Cases — MVP

**Project:** E‑Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-03

---

## 1. Test Strategy
- **Unit Tests**: JUnit 5, Mockito — services, validators, repositories.
- **Integration Tests**: Spring Boot Test — controllers + DB with **Testcontainers (Postgres)**.
- **API Smoke**: Postman/RestAssured — registration, login, product create/list/get.
- **Security**: 401/403 coverage; JWT validation paths through Gateway.
- **Observability**: Actuator health/metrics, logs JSON with `traceId`, Jaeger spans visible.

## 2. Test Environments & Data
- **Local**: Docker Compose (Postgres, RabbitMQ, Prometheus, Grafana, Loki, Promtail, Jaeger).
- **Seed Data**: Admin user (manual/seed), a few products for list/search tests.

## 3. Entry/Exit Criteria
- **Entry**: Services compile; DB running; OpenAPI ready.  
- **Exit**: All critical tests pass; smoke suite green; metrics/logs/traces verified.

## 4. Test Cases

### 4.1 Registration
- **TC‑U‑01** Register valid user  
  **Steps**: POST `/users` valid payload  
  **Expected**: 201; response omits password; DB row created.

- **TC‑U‑02** Duplicate email  
  **Steps**: POST `/users` with existing email  
  **Expected**: 409; Problem schema.

- **TC‑U‑03** Weak password  
  **Expected**: 400; field error for password.

### 4.2 Login
- **TC‑A‑01** Login success  
  **Steps**: POST `/auth/login` valid credentials  
  **Expected**: 200; JWT present; `exp≈900` seconds; signature valid.

- **TC‑A‑02** Login wrong password  
  **Expected**: 401.

### 4.3 Catalog
- **TC‑C‑01** Create product as ADMIN  
  **Pre**: Obtain ADMIN token  
  **Steps**: POST `/products` valid payload  
  **Expected**: 201; SKU unique; persisted.

- **TC‑C‑02** Create product as USER  
  **Expected**: 403.

- **TC‑C‑03** List products with filters  
  **Steps**: GET `/products?query=anc&category=Electronics&minPrice=5000&maxPrice=20000&page=0&size=20&sort=createdAt,DESC`  
  **Expected**: 200; filtered/paginated; default sort respected.

- **TC‑C‑04** Get product by id not found  
  **Steps**: GET `/products/{id}` with random UUID  
  **Expected**: 404; Problem schema.

### 4.4 Gateway/Security
- **TC‑G‑01** Missing token on secured path  
  **Expected**: 401.

- **TC‑G‑02** Invalid/expired token  
  **Expected**: 401.

- **TC‑G‑03** Rate limit exceeded  
  **Expected**: 429 (dev‑only limiter).

### 4.5 Observability
- **TC‑OBS‑01** Metrics exposed  
  **Steps**: GET `/actuator/prometheus`  
  **Expected**: 200; basic metrics visible.

- **TC‑OBS‑02** JSON logs with traceId  
  **Steps**: Make a request; inspect logs via Loki  
  **Expected**: presence of `traceId` field.

- **TC‑OBS‑03** Jaeger traces  
  **Steps**: Make a call; open Jaeger UI  
  **Expected**: trace shows Gateway → Service spans.

## 5. Reporting
- CI publishes unit & integration test reports (Surefire/Failsafe).  
- Postman collection run can be added to CI later.
