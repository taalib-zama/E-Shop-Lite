# Sprint 3 Traceability Matrix

**Project:** E-Shop Lite  
**Sprint:** Sprint 3 (Payment, Compensation, Notifications)  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Purpose and Scope
This document provides end-to-end traceability for Sprint 3 across:
- Sprint 3 functional requirements (`FR-S3-01` to `FR-S3-06`)
- User stories (`US-11` to `US-15`)
- API and async contracts
- Test coverage (`TC-S3-*` and story-level `USxx-*`)
- Architecture and DoD evidence

Source references:
- `eshop-lite-functional-pack/docs/USER-STORIES-S3.md`
- `eshop-lite-functional-pack/docs/TEST-PLAN-S3.md`
- `eshop-lite-functional-pack/SPRINT-3-PLAN.md`

---

## 2. Requirement to User Story Mapping

| FR ID | Requirement Summary | Mapped User Story | Primary Endpoint(s) / Flow |
|---|---|---|---|
| FR-S3-01 | Authorize payment for reserved orders and confirm order on success | US-11 Payment Authorization | Payment authorize flow + order update |
| FR-S3-02 | Compensate payment failure by releasing inventory and cancelling order | US-12 Payment Failure Compensation | payment failed -> compensation -> inventory release |
| FR-S3-03 | Show final order and payment status to user | US-13 Order Finalization Status | `GET /orders/{id}` |
| FR-S3-04 | Dispatch terminal-state notifications idempotently | US-14 Notification Dispatch | `order.finalized` event -> notification handler |
| FR-S3-05 | Apply resilience controls to payment and notification dependencies | US-15 Reliability and Resilience Hardening | Retry/circuit-breaker/outbox retry paths |
| FR-S3-06 | Cross-cutting consistency: Problem Details, trace propagation, observability | US-11..US-15 | All Sprint 3 APIs and async flows |

---

## 3. Story Traceability Matrix

| US ID | Actor(s) | Outcome / Acceptance Intent | FR Link(s) | API Contract / Flow | Core Test IDs |
|---|---|---|---|---|---|
| US-11 | System | Payment success authorizes transaction and confirms order | FR-S3-01, FR-S3-06 | payment authorize + order state transition | `TC-S3-P-01`, `TC-S3-P-03` |
| US-12 | Platform | Payment failure triggers idempotent compensation | FR-S3-02, FR-S3-06 | failed payment -> release inventory -> cancel order | `TC-S3-P-02`, `TC-S3-C-01`, `TC-S3-C-02` |
| US-13 | User | Query final order/payment status projection | FR-S3-03, FR-S3-06 | `GET /orders/{id}` | `TC-S3-O-01`, `TC-S3-O-02` |
| US-14 | User | Terminal order event creates one notification | FR-S3-04, FR-S3-06 | `order.finalized` event -> notification consumer | `TC-S3-N-01`, `TC-S3-N-02`, `TC-S3-N-03` |
| US-15 | Platform | Retries/CB/fallback protect workflows under failures | FR-S3-05, FR-S3-06 | resilience policies and fallback paths | `TC-S3-R-01`, `TC-S3-R-02`, `TC-S3-R-03` |

---

## 4. Cross-Cutting NFR and Observability Traceability

| Area | Expected Evidence | Validation IDs |
|---|---|---|
| Security | Protected payment/order routes and header propagation | `TC-S3-R-03` |
| Error Model | Problem Details on `400/401/403/404/409` | negative tests per story |
| Async Reliability | Outbox and idempotent consumer behavior | `TC-S3-N-02`, compensation tests |
| Resilience | Retry and circuit-breaker metrics/state | `TC-S3-R-01`, `TC-S3-R-02` |
| Metrics/Logs/Traces | Prometheus counters, correlated logs, Jaeger continuity | `TC-S3-OBS-01`, `TC-S3-OBS-02`, `TC-S3-OBS-03` |

---

## 5. Sprint 3 Definition of Done Evidence Map

| DoD Item | Evidence Source | Status |
|---|---|---|
| Payment success/failure paths validated | `TC-S3-P-*`, `TC-S3-C-*` | Planned/In Progress |
| Notifications idempotent and retry-safe | `TC-S3-N-*` | Planned/In Progress |
| OpenAPI and event contracts updated | sprint artifacts and API docs | Planned/In Progress |
| Observability verified end-to-end | `TC-S3-OBS-*` | Planned/In Progress |
| Sprint 3 smoke checks green | smoke run output | Planned/In Progress |

---

## 6. Sign-Off Checklist

- [ ] Critical `TC-S3-*` tests pass
- [ ] Payment compensation and inventory release verified
- [ ] Notification idempotency verified
- [ ] Resilience metrics and traces reviewed
- [ ] OpenAPI/contracts committed

