# Sprint 1 Traceability Matrix

**Project:** E-Shop Lite  
**Sprint:** Sprint 1 (Identity and Catalog)  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Purpose and Scope
This document provides end-to-end traceability for Sprint 1 across:
- Functional requirements (`FR-01` to `FR-06`)
- User stories (`US-01` to `US-05`)
- API contracts
- Test coverage (core `TC-*` and story-level `USxx-*` cases)
- Architecture components and operational evidence

Source references:
- `eshop-lite-functional-pack/docs/FRD.md`
- `eshop-lite-functional-pack/docs/USER-STORIES-S1.md`
- `eshop-lite-functional-pack/docs/TEST-PLAN.md`
- `eshop-lite-functional-pack/US-1.md`
- `eshop-lite-functional-pack/US-2.md`
- `eshop-lite-functional-pack/US-3.md`
- `eshop-lite-functional-pack/US-4.md`
- `eshop-lite-functional-pack/US-5.md`
- `eshop-lite-sprint-details/docs/ARCHITECTURE.md`
- `eshop-lite-sprint-details/docs/PLAN.md`

---

## 2. Requirement to User Story Mapping

| FR ID | Requirement Summary | Mapped User Story | Primary Endpoint(s) |
|---|---|---|---|
| FR-01 | Registration with unique email, strong password, sanitized response | US-01 Register User | `POST /users` |
| FR-02 | Login with JWT (`sub`, `role`, `iat`, `exp=15m`), invalid -> 401 | US-02 Login and JWT | `POST /auth/login`, `GET /users/me` |
| FR-03 | Product create by ADMIN, unique SKU, price > 0, default INR | US-03 Create Product (ADMIN) | `POST /products` |
| FR-04 | Catalog list/search with filters, pagination, default sort | US-04 List and Search Products | `GET /products` |
| FR-05 | Standard Problem Details style error model | US-01..US-05 (cross-cutting) | All Sprint 1 endpoints |
| FR-06 | Gateway enforcement, trace propagation, rate limiting | US-02, US-03 (+platform for all routes) | Gateway routes for `/users/**`, `/auth/**`, `/products/**` |

---

## 3. Story Traceability Matrix

| US ID | Actor(s) | Outcome / Acceptance Intent | FR Link(s) | API Contract | Architecture Components | Core Test-Plan IDs | Story-Level QA IDs |
|---|---|---|---|---|---|---|---|
| US-01 Register User | Visitor | Create account with unique email, role `USER`, no password in response, duplicate -> 409 | FR-01, FR-05 | `POST /users` | API Gateway -> `USER-SERVICE` -> Postgres (`users_db`) | `TC-U-01`, `TC-U-02`, `TC-U-03` | Refer `US-1.md` QA section |
| US-02 Login and JWT | User | Valid credentials return bearer token (`expiresIn=900`), invalid -> 401; token accesses protected route | FR-02, FR-05, FR-06 | `POST /auth/login`, `GET /users/me` | API Gateway, `USER-SERVICE`, JWT, Spring Security | `TC-A-01`, `TC-A-02`, `TC-G-01`, `TC-G-02` | `US02-P1..P3`, `US02-N1..N5`, `US02-S1..S3`, `US02-O1..O3`, `US02-Perf1` |
| US-03 Create Product (ADMIN) | Admin | ADMIN can create product; USER forbidden; duplicate SKU -> 409 | FR-03, FR-05, FR-06 | `POST /products` | API Gateway -> `CATALOG-SERVICE` -> Postgres (`catalog_db`) + RBAC | `TC-C-01`, `TC-C-02`, `TC-G-01`, `TC-G-02` | `US03-P1..P3`, `US03-N1..N4`, `US03-S1..S3`, `US03-O1..O3`, `US03-Perf1` |
| US-04 List and Search Products | Visitor, User | Browse/search with filters; paginated response; default sort `createdAt,DESC` | FR-04, FR-05 | `GET /products` | API Gateway -> `CATALOG-SERVICE`, pageable query path | `TC-C-03` | `US04-P1..P5`, `US04-N1..N5`, `US04-S1..S2`, `US04-O1..O3`, `US04-Perf1` |
| US-05 Get Product by ID | Visitor, User | Retrieve product details by UUID; not found -> 404; invalid UUID -> 400 | FR-04, FR-05 | `GET /products/{id}` | API Gateway -> `CATALOG-SERVICE`, repository lookup by UUID | `TC-C-04` | `US05-P1..P2`, `US05-N1..N2`, `US05-S1..S2`, `US05-O1..O3`, `US05-Perf1` |

---

## 4. API and Gateway Route Traceability

| External Route | Gateway URI | Backing Service | Story | Security Expectation |
|---|---|---|---|---|
| `/users/**` | `lb://USER-SERVICE` | `user-service` | US-01 | Public register path for `POST /users` |
| `/auth/**` | `lb://USER-SERVICE` | `user-service` | US-02 | Public `POST /auth/login`; JWT used for protected endpoints |
| `/products/**` | `lb://CATALOG-SERVICE` | `catalog-service` | US-03, US-04, US-05 | `POST /products` protected (`ADMIN`); `GET /products**` public in Sprint 1 |

Notes:
- Discovery for local/dev is via Eureka service IDs (`USER-SERVICE`, `CATALOG-SERVICE`).
- Config is externalized through Config Server strategy per architecture docs.

---

## 5. Cross-Cutting NFR and Observability Traceability

| Area | Requirement Source | Expected Evidence | Validation IDs |
|---|---|---|---|
| Performance | FRD NFR section | Read p95 < 250 ms; write p95 < 500 ms | `US02-Perf1`, `US03-Perf1`, `US04-Perf1`, `US05-Perf1` |
| Security | FR-02, FR-03, FR-06 | BCrypt, JWT validation, 401/403 behavior on protected paths | `TC-A-02`, `TC-C-02`, `TC-G-01`, `TC-G-02` |
| Error Model | FR-05 | Problem Details style payloads across services | `TC-U-02`, `TC-C-04` + story negative tests |
| Metrics | FRD Observability | `/actuator/prometheus` exposed | `TC-OBS-01` |
| Logs | FRD Observability | Structured logs include `traceId` | `TC-OBS-02` |
| Tracing | FRD Observability | Gateway -> Service spans in Jaeger | `TC-OBS-03` |

---

## 6. Sprint 1 Definition of Done Evidence Map

| DoD Item | Evidence Source / Artifact | Status |
|---|---|---|
| Unit and integration coverage for critical paths | `TEST-PLAN.md` strategy + service test reports (`surefire`/`failsafe`) | Planned/In Progress |
| OpenAPI updated for user and catalog APIs | `docs/openapi/user-service.yaml`, `docs/openapi/catalog-service.yaml` (as referenced in US docs) | Planned/In Progress |
| Gateway smoke paths green | Postman collection `postman/E-Shop-Lite.postman_collection.json` + smoke run output | Planned/In Progress |
| Metrics/logs/traces verified | Prometheus/Loki/Jaeger checks in `TEST-PLAN.md` | Planned/In Progress |
| Standardized errors across services | FR-05 + US story error contract sections | Planned/In Progress |

---

## 7. Coverage Gaps and Follow-Ups

1. Standardize Problem Details implementation in `catalog-service` to match `user-service` shape (`type`, `title`, `status`, `detail`, `traceId`, `errors`).
2. Confirm `GET /products` filtering/sorting implementation parity with FR-04 (current baseline may require repository/specification expansion).
3. Ensure 429 rate-limiting test path (`TC-G-03`) is wired in gateway config for dev verification.
4. Convert all story-level QA IDs (`US02-*`..`US05-*`) into automated integration suites and publish in CI reports.

---

## 8. Sign-Off Checklist

- [ ] All `TC-*` critical cases pass (identity, catalog, gateway, observability)
- [ ] Story-level negative/security scenarios pass for `US-02` to `US-05`
- [ ] OpenAPI contracts updated and reviewed
- [ ] Error schema consistency validated against FR-05
- [ ] Observability checks (metrics/logs/traces) completed and captured
- [ ] Sprint 1 demo evidence attached (API run + dashboards + traces)

---

## 9. Change Control
- Update this matrix whenever a Sprint 1 story, FR mapping, or test ID changes.
- Keep IDs stable (`FR-*`, `US-*`, `TC-*`) to preserve audit traceability across CI and documentation.

