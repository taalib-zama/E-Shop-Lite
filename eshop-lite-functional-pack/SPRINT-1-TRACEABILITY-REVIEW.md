# Sprint 1 Traceability - Review One-Pager

**Project:** E-Shop Lite  
**Sprint:** Sprint 1 (Identity and Catalog)  
**Date:** 2026-03-26

## 1) Sprint Scope at a Glance
- **Stories:** `US-01` to `US-05` (register, login/JWT, create/list/get products)
- **Services:** `user-service`, `catalog-service`, `api-gateway`
- **Platform:** Eureka discovery, Config Server config strategy, Postgres per service
- **Cross-cutting:** Problem Details errors, JWT security, metrics/logs/traces

## 2) FR to US Mapping (Condensed)

| FR | Requirement | Story | Endpoint(s) |
|---|---|---|---|
| `FR-01` | User registration | `US-01` | `POST /users` |
| `FR-02` | Login + JWT 15m | `US-02` | `POST /auth/login`, `GET /users/me` |
| `FR-03` | Admin product create | `US-03` | `POST /products` |
| `FR-04` | Catalog list/search/detail | `US-04`, `US-05` | `GET /products`, `GET /products/{id}` |
| `FR-05` | Standard error schema | `US-01..US-05` | All Sprint 1 APIs |
| `FR-06` | Gateway security + trace propagation + rate limit | `US-02`, `US-03` (+platform) | `/users/**`, `/auth/**`, `/products/**` |

## 3) Story Coverage Snapshot

| Story | Actor | Acceptance Intent | Security | Primary Tests |
|---|---|---|---|---|
| `US-01` | Visitor | Register user, unique email, safe response | Public register | `TC-U-01..03` |
| `US-02` | User | Valid login -> JWT; invalid -> 401 | Public login, protected `/users/me` | `TC-A-01..02`, `TC-G-01..02` |
| `US-03` | Admin | Create product, SKU unique, USER blocked | `POST /products` requires `ADMIN` | `TC-C-01..02`, `TC-G-01..02` |
| `US-04` | Visitor/User | Filtered, paged catalog list/search | Public read | `TC-C-03` |
| `US-05` | Visitor/User | Product detail by ID, 404 on missing | Public read | `TC-C-04` |

## 4) Architecture Route Traceability

| External Route | Gateway URI | Target Service | Sprint Rule |
|---|---|---|---|
| `/users/**` | `lb://USER-SERVICE` | `user-service` | `POST /users` public |
| `/auth/**` | `lb://USER-SERVICE` | `user-service` | `POST /auth/login` public |
| `/products/**` | `lb://CATALOG-SERVICE` | `catalog-service` | `POST` protected, `GET` public |

## 5) Critical Quality and Ops Evidence
- **Security:** JWT + role checks validated by `TC-G-01`, `TC-G-02`, `TC-C-02`
- **Errors:** Problem Details expected across all endpoints (`FR-05`)
- **Observability:**
  - Metrics: `TC-OBS-01`
  - Logs (`traceId`): `TC-OBS-02`
  - Traces (Gateway -> Service): `TC-OBS-03`
- **Performance targets:** Read p95 `< 250 ms`, write p95 `< 500 ms` (FRD)

## 6) DoD Status Snapshot

| DoD Item | Status |
|---|---|
| Critical test coverage in place | Planned/In Progress |
| OpenAPI updated (`user`, `catalog`) | Planned/In Progress |
| Gateway smoke via Postman | Planned/In Progress |
| Metrics/logs/traces verified | Planned/In Progress |
| Error schema consistency (`FR-05`) | Planned/In Progress |

## 7) Top Risks and Next Actions
1. Align `catalog-service` Problem Details payload with `user-service` shape.
2. Verify `GET /products` filter/sort implementation is fully `FR-04` compliant.
3. Enable and verify gateway rate-limit path for `TC-G-03` (`429`).
4. Publish story-level QA suites (`US02-*` to `US05-*`) in CI reporting.

