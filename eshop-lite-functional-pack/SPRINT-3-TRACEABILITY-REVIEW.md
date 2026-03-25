# Sprint 3 Traceability - Review One-Pager

**Project:** E-Shop Lite  
**Sprint:** Sprint 3 (Payment, Compensation, Notifications)  
**Date:** 2026-03-26

## 1) Sprint Scope at a Glance
- **Stories:** `US-11` to `US-15`
- **Services:** `order-service`, `payment-service`, `notification-service`, `inventory-service`
- **Patterns:** Outbox, idempotent consumers, Feign + Resilience4j, trace propagation
- **Outcome:** Complete checkout finalization with compensation and notifications

## 2) FR to US Mapping (Condensed)

| FR | Requirement | Story | API / Flow |
|---|---|---|---|
| `FR-S3-01` | Payment authorization and order confirm | `US-11` | `POST /payments/authorize` + order update |
| `FR-S3-02` | Compensation on payment failure | `US-12` | `payment.failed.v1` -> release -> cancel |
| `FR-S3-03` | Final order/payment status query | `US-13` | `GET /orders/{id}` |
| `FR-S3-04` | Terminal-state notification dispatch | `US-14` | `order.finalized.v1` -> notification consumer |
| `FR-S3-05` | Resilience hardening | `US-15` | retry/CB/fallback/outbox retry |
| `FR-S3-06` | Cross-cutting consistency and observability | `US-11..US-15` | all APIs + async paths |

## 3) Story Coverage Snapshot

| Story | Intent | Primary Tests |
|---|---|---|
| `US-11` | Authorize payment and emit result event | `TC-S3-P-01`, `TC-S3-P-03` |
| `US-12` | Compensate failed payment idempotently | `TC-S3-P-02`, `TC-S3-C-01`, `TC-S3-C-02` |
| `US-13` | Show final order/payment projection | `TC-S3-O-01`, `TC-S3-O-02` |
| `US-14` | Dispatch one notification per terminal event | `TC-S3-N-01`, `TC-S3-N-02`, `TC-S3-N-03` |
| `US-15` | Keep workflows stable under failures | `TC-S3-R-01`, `TC-S3-R-02`, `TC-S3-R-03` |

## 4) Architecture Route and Event Traceability

| Path / Event | Target | Sprint Rule |
|---|---|---|
| `/payments/**` | `lb://PAYMENT-SERVICE` | protected/internal authorization APIs |
| `/orders/**` | `lb://ORDER-SERVICE` | protected query and workflow endpoints |
| `payment.authorized.v1` | order consumer | confirm order flow |
| `payment.failed.v1` | order consumer | compensation trigger |
| `order.finalized.v1` | notification consumer | final notification dispatch |

## 5) Critical Quality Evidence
- **Security:** protected routes + header propagation (`TC-S3-R-03`)
- **Compensation:** cancel/release consistency (`TC-S3-C-01`, `TC-S3-C-02`)
- **Reliability:** retries and idempotency (`TC-S3-N-02`, `TC-S3-R-01`)
- **Observability:** metrics/logs/traces (`TC-S3-OBS-01..03`)

## 6) DoD Status Snapshot

| DoD Item | Status |
|---|---|
| Payment success/failure paths validated | Planned/In Progress |
| Compensation and idempotency validated | Planned/In Progress |
| Notification reliability validated | Planned/In Progress |
| OpenAPI + event contracts updated | Planned/In Progress |
| End-to-end observability verified | Planned/In Progress |

## 7) Top Risks and Next Actions
1. Finalize event schema governance and backward compatibility checks.
2. Ensure compensation replay cannot create duplicate side effects.
3. Wire dashboard alerts for circuit-breaker/open retry backlogs.
4. Publish automated `TC-S3-*` results in CI reports.

