t# Sprint 3 Implementation Checklist (Day-by-Day)

**Project:** E-Shop Lite  
**Sprint:** Sprint 3 (Payment, Compensation, Notifications)  
**Duration:** 10 working days  
**Date:** 2026-03-26

---

## 1) Sprint Goal
Complete checkout finalization with payment authorization, compensation on payment failure, notification dispatch, and resilience hardening with full observability.

Related artifacts:
- `eshop-lite-functional-pack/SPRINT-3-PLAN.md`
- `eshop-lite-functional-pack/docs/USER-STORIES-S3.md`
- `eshop-lite-functional-pack/docs/TEST-PLAN-S3.md`
- `eshop-lite-functional-pack/SPRINT-3-TRACEABILITY.md`
- `eshop-lite-functional-pack/US-11.md` to `eshop-lite-functional-pack/US-15.md`

---

## 2) Day-by-Day Execution Sheet

### Day 1 - Foundations and Module Scaffolding
**Objective:** Bootstrap Sprint 3 service foundations and configs.

- [ ] Create `payment-service` module skeleton (app, pom, package layout)
- [ ] Create `notification-service` module skeleton
- [ ] Add service entries in parent build and config repo plan
- [ ] Draft initial DB migrations for payment and notification tables
- [ ] Add gateway route placeholders for `/payments/**` and optional `/notifications/**`

**Validation**
- [ ] Modules compile locally
- [ ] Config bootstrapping resolves without missing keys

---

### Day 2 - Payment Core Domain and API (US-11)
**Objective:** Implement payment authorization baseline.

- [ ] Implement `POST /payments/authorize`
- [ ] Add DTO validation and Problem Details mapping
- [ ] Persist payment transaction state (`INITIATED`, `AUTHORIZED`, `FAILED`)
- [ ] Add idempotency key handling

**Validation**
- [ ] `TC-S3-P-01` (success path) partial pass
- [ ] Negative validation path returns expected Problem Details

---

### Day 3 - Payment Events and Outbox (US-11)
**Objective:** Make payment results event-driven and reliable.

- [ ] Write outbox entry in same transaction as payment state update
- [ ] Implement outbox dispatcher for payment events
- [ ] Emit `payment.authorized.v1` and `payment.failed.v1`
- [ ] Add event schema conformance checks against `EVENT-CONTRACTS-S3.md`

**Validation**
- [ ] Event payload shape verified
- [ ] Outbox publish smoke test passes

---

### Day 4 - Compensation Orchestration (US-12)
**Objective:** Implement failed-payment compensation workflow.

- [ ] Consume `payment.failed.v1` in order workflow
- [ ] Trigger inventory release action/event
- [ ] Transition order to `CANCELLED` after compensation completion
- [ ] Add idempotency guard for repeated failure events

**Validation**
- [ ] `TC-S3-C-01` baseline pass
- [ ] Duplicate compensation invocation is safe (`TC-S3-C-02` partial)

---

### Day 5 - Final Order Projection (US-13)
**Objective:** Expose finalized order + payment status.

- [ ] Extend `GET /orders/{id}` projection with payment summary
- [ ] Enforce owner/admin authorization checks
- [ ] Ensure stable response shape for `CONFIRMED` and `CANCELLED`

**Validation**
- [ ] `TC-S3-O-01` pass
- [ ] `TC-S3-O-02` pass

---

### Day 6 - Notification Consumer and Storage (US-14)
**Objective:** Create notification flow for terminal order events.

- [ ] Implement `order.finalized.v1` consumer in `notification-service`
- [ ] Persist notification records with status tracking
- [ ] Add processed-events idempotency table/guard
- [ ] Add mock notification adapter

**Validation**
- [ ] `TC-S3-N-01` baseline pass
- [ ] Duplicate event path safe (`TC-S3-N-02` partial)

---

### Day 7 - Notification Retry and Operational Hardening (US-14/US-15)
**Objective:** Add retry behavior and failure handling.

- [ ] Implement retry/backoff for transient notification failures
- [ ] Add DLQ strategy for irrecoverable messages
- [ ] Add monitoring logs for retry attempts and DLQ handoff

**Validation**
- [ ] `TC-S3-N-03` pass
- [ ] Retry and DLQ log evidence captured

---

### Day 8 - Resilience Policies for S2S (US-15)
**Objective:** Apply Feign + Resilience4j protections end-to-end.

- [ ] Configure timeout/retry/circuit-breaker for payment/notification dependencies
- [ ] Add fallback handlers for recoverable workflow state
- [ ] Add header propagation (`Authorization`, `X-Request-Id`, `traceparent`)

**Validation**
- [ ] `TC-S3-R-01` pass
- [ ] `TC-S3-R-02` pass
- [ ] `TC-S3-R-03` pass

---

### Day 9 - Observability, Traceability, and Test Completion
**Objective:** Complete quality evidence for sign-off.

- [ ] Validate Prometheus metrics for payment/notification/resilience
- [ ] Validate structured logs with correlation IDs
- [ ] Validate Jaeger end-to-end traces
- [ ] Complete remaining integration and async tests

**Validation**
- [ ] `TC-S3-OBS-01` pass
- [ ] `TC-S3-OBS-02` pass
- [ ] `TC-S3-OBS-03` pass

---

### Day 10 - Sprint Hardening, Demo Prep, and Gate Review
**Objective:** Finalize deliverables and sprint evidence.

- [ ] Final regression + smoke run through gateway
- [ ] Update OpenAPI and event contract docs
- [ ] Update traceability evidence and checklist status
- [ ] Prepare demo script and artifacts

**Validation**
- [ ] All critical `TC-S3-*` green
- [ ] Sprint 3 readiness gate checklist completed

---

## 3) Cross-Day Dependency and Risk Tracker

### Dependencies
- [ ] Sprint 2 baseline flow available (`orders` + inventory reserve/release)
- [ ] RabbitMQ and Postgres running in local/test environments
- [ ] Service discovery and gateway route wiring in place

### Risks and Mitigations
- [ ] **Duplicate side effects risk** -> enforce idempotency (`eventId + consumerName`)
- [ ] **Dependency instability risk** -> timeout/retry/circuit-breaker + fallback
- [ ] **State divergence risk** -> compensation reconciliation checks
- [ ] **Observability gaps risk** -> verify metrics/logs/traces before gate

---

## 4) End-of-Sprint Readiness Gate

### Functional Gate
- [ ] US-11 payment authorization complete
- [ ] US-12 compensation complete
- [ ] US-13 final status projection complete
- [ ] US-14 notification dispatch complete
- [ ] US-15 resilience hardening complete

### Quality Gate
- [ ] Critical tests from `TEST-PLAN-S3.md` passed
- [ ] Problem Details behavior validated on negative paths
- [ ] Performance and resilience behavior acceptable for dev target

### Operational Gate
- [ ] Metrics/logs/traces verified end-to-end
- [ ] OpenAPI and `EVENT-CONTRACTS-S3.md` updated
- [ ] `SPRINT-3-TRACEABILITY.md` evidence current

### Release Readiness
- [ ] Demo scenario runs start-to-finish (success and failure-compensation)
- [ ] Known issues documented with owners and next actions

