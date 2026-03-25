# Sprint 2 Traceability Matrix

**Project:** E-Shop Lite  
**Sprint:** Sprint 2 (Orders, Inventory, Reliability)  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Purpose and Scope
This document provides end-to-end traceability for Sprint 2 across:
- Sprint 2 functional requirements (`FR-S2-01` to `FR-S2-06`)
- User stories (`US-06` to `US-10`)
- API and async contracts
- Test coverage (`TC-S2-*` and story-level `USxx-*` cases)
- Architecture components and operational evidence

Source references:
- `eshop-lite-functional-pack/docs/USER-STORIES-S2.md`
- `eshop-lite-functional-pack/docs/TEST-PLAN-S2.md`
- `eshop-lite-functional-pack/SPRINT-2-PLAN.md`
- `eshop-lite-functional-pack/US-6.md`
- `eshop-lite-functional-pack/US-7.md`
- `eshop-lite-functional-pack/US-8.md`
- `eshop-lite-functional-pack/US-9.md`
- `eshop-lite-functional-pack/US-10.md`
- `eshop-lite-sprint-details/docs/ARCHITECTURE.md`
- `eshop-lite-sprint-details/docs/PLAN.md`

---

## 2. Requirement to User Story Mapping

| FR ID | Requirement Summary | Mapped User Story | Primary Endpoint(s) / Flow |
|---|---|---|---|
| FR-S2-01 | Place order with authenticated user, valid items, initial `PENDING_INVENTORY` status | US-06 Place Order | `POST /orders` |
| FR-S2-02 | Atomic inventory reserve/release with idempotency and conflict handling | US-07 Reserve and Release Inventory | `POST /inventory/reservations`, `POST /inventory/reservations/{id}/release` |
| FR-S2-03 | Retrieve order status projection by order ID | US-08 Order Status Projection | `GET /orders/{id}` |
| FR-S2-04 | Reliable event publication using outbox and RabbitMQ retries | US-09 Reliable Event Publish (Outbox) | `order.created` outbox -> dispatcher -> broker -> consumer |
| FR-S2-05 | Resilient S2S via OpenFeign + timeout/retry/circuit breaker + fallback | US-10 Resilient S2S Failure Handling | `ORDER-SERVICE` -> `INVENTORY-SERVICE` |
| FR-S2-06 | Cross-cutting consistency: Problem Details, trace propagation, observability evidence | US-06..US-10 (cross-cutting) | All Sprint 2 APIs and async paths |

---

## 3. Story Traceability Matrix

| US ID | Actor(s) | Outcome / Acceptance Intent | FR Link(s) | API Contract / Flow | Architecture Components | Core Test-Plan IDs | Story-Level QA IDs |
|---|---|---|---|---|---|---|---|
| US-06 Place Order | User | Place valid order, return `201` with `PENDING_INVENTORY`, trigger reservation workflow | FR-S2-01, FR-S2-04, FR-S2-06 | `POST /orders` | API Gateway -> `ORDER-SERVICE` -> Postgres (`orders_db`) + outbox | `TC-S2-O-01`, `TC-S2-O-02`, `TC-S2-G-01`, `TC-S2-G-02` | `US06-P1..P3`, `US06-N1..N3`, `US06-S1..S2`, `US06-O1..O3` |
| US-07 Reserve and Release Inventory | System | Reserve atomically, reject insufficient stock, release restores quantity, idempotency-safe behavior | FR-S2-02, FR-S2-06 | `POST /inventory/reservations`, `POST /inventory/reservations/{id}/release` | API Gateway/internal caller -> `INVENTORY-SERVICE` -> Postgres (`inventory_db`) | `TC-S2-I-01`, `TC-S2-I-02`, `TC-S2-I-03`, `TC-S2-I-04` | `US07-P1..P3`, `US07-N1..N3`, `US07-S1..S2`, `US07-O1..O3` |
| US-08 Order Status Projection | User | Query order by ID with current status and items; unknown order returns `404` | FR-S2-03, FR-S2-06 | `GET /orders/{id}` | API Gateway -> `ORDER-SERVICE` query path | `TC-S2-O-03`, `TC-S2-O-04` | `US08-P1..P2`, `US08-N1`, `US08-S1..S2`, `US08-O1` |
| US-09 Outbox Reliability | Platform | Outbox events published reliably; broker failures retried; consumer idempotent | FR-S2-04, FR-S2-06 | DB tx + outbox poller + RabbitMQ + consumer | `ORDER-SERVICE` outbox + RabbitMQ + `INVENTORY-SERVICE` consumer | `TC-S2-A-01`, `TC-S2-A-02`, `TC-S2-A-03` | `US09-P1..P2`, `US09-N1..N2`, `US09-O1` |
| US-10 Resilient S2S | Platform | Timeout/retry/CB applied; fallback keeps order recoverable | FR-S2-05, FR-S2-06 | Feign reserve call + fallback strategy | `ORDER-SERVICE` Feign + Resilience4j -> `INVENTORY-SERVICE` | `TC-S2-R-01`, `TC-S2-R-02`, `TC-S2-R-03` | `US10-P1`, `US10-N1..N3`, `US10-O1..O2` |

---

## 4. API and Gateway Route Traceability

| External Route | Gateway URI | Backing Service | Story | Security Expectation |
|---|---|---|---|---|
| `/orders/**` | `lb://ORDER-SERVICE` | `order-service` | US-06, US-08 | Protected user routes; JWT required |
| `/inventory/**` | `lb://INVENTORY-SERVICE` | `inventory-service` | US-07 | Protected internal/service routes |
| Async event path | N/A (broker route) | RabbitMQ + consumers | US-09 | Producer/consumer reliability + idempotency |
| S2S call path | N/A (service discovery) | `ORDER-SERVICE` -> `INVENTORY-SERVICE` | US-10 | Header propagation + resilience policies |

Notes:
- Discovery for local/dev uses Eureka service IDs (`ORDER-SERVICE`, `INVENTORY-SERVICE`).
- Configuration remains externalized through Config Server strategy.

---

## 5. Cross-Cutting NFR and Observability Traceability

| Area | Requirement Source | Expected Evidence | Validation IDs |
|---|---|---|---|
| Performance | Sprint 2 plan + story NFRs | p95 write/read targets maintained under new flows | Story `US06/07/08` perf checks |
| Security | `USER-STORIES-S2.md`, US-06/07/08/10 | JWT checks, protected routes, propagated auth headers | `TC-S2-G-01`, `TC-S2-G-02`, `TC-S2-R-03` |
| Error Model | Sprint 2 story contracts | Problem Details style payloads on `400/401/403/404/409` | `TC-S2-O-02` + negative test cases |
| Async Reliability | US-09 | Outbox retries + idempotent consume | `TC-S2-A-01`, `TC-S2-A-02`, `TC-S2-A-03` |
| Resilience | US-10 | Retry and circuit breaker behavior observable | `TC-S2-R-01`, `TC-S2-R-02` |
| Metrics/Logs/Traces | `TEST-PLAN-S2.md` observability section | Prometheus metrics, correlated logs, Jaeger continuity | `TC-S2-OBS-01`, `TC-S2-OBS-02`, `TC-S2-OBS-03` |

---

## 6. Sprint 2 Definition of Done Evidence Map

| DoD Item | Evidence Source / Artifact | Status |
|---|---|---|
| Unit and integration coverage for new services | `TEST-PLAN-S2.md` + service test reports (`surefire`/`failsafe`) | Planned/In Progress |
| Async workflow reliability validated | Outbox and RabbitMQ integration tests (`TC-S2-A-*`) | Planned/In Progress |
| Resilience behavior validated | Resilience tests (`TC-S2-R-*`) + metrics evidence | Planned/In Progress |
| OpenAPI updated for order/inventory APIs | `docs/openapi/order-service.yaml`, `docs/openapi/inventory-service.yaml` (as planned in US docs) | Planned/In Progress |
| Gateway and route smoke green | Postman collection updates + smoke run output | Planned/In Progress |
| Trace/log/metric visibility across flows | Prometheus/Loki/Jaeger checks in `TEST-PLAN-S2.md` | Planned/In Progress |

---

## 7. Coverage Gaps and Follow-Ups

1. Create and baseline new modules (`order-service`, `inventory-service`) in multi-module build and config-repo.
2. Finalize standardized Problem Details payload implementation across new services.
3. Define and version shared event contracts (`order.created.v1`) in a dedicated contract doc.
4. Implement and verify gateway config and security rules for `/orders/**` and `/inventory/**`.
5. Automate all story-level QA suites (`US06-*`..`US10-*`) in CI reporting.
6. Confirm handling strategy for `TC-S2-G-03` based on whether admin-only inventory path is enabled in this sprint.

---

## 8. Sign-Off Checklist

- [ ] All critical `TC-S2-*` cases pass (order, inventory, async, resilience, gateway, observability)
- [ ] Story-level negative/security scenarios pass for `US-06` to `US-10`
- [ ] OpenAPI contracts reviewed and committed
- [ ] Async event reliability and idempotency evidence captured
- [ ] Resilience metrics and breaker behavior validated in Prometheus/Grafana
- [ ] End-to-end traces captured for gateway -> order -> inventory and fallback paths

---

## 9. Change Control
- Update this matrix whenever a Sprint 2 story, API contract, event contract, or test ID changes.
- Keep IDs stable (`FR-S2-*`, `US-*`, `TC-S2-*`) to preserve audit traceability across CI and documentation.

