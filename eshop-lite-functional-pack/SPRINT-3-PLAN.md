# Sprint 3 Plan - Payment, Compensation, and Notifications

**Project:** E-Shop Lite  
**Sprint:** Sprint 3  
**Date:** 2026-03-26

---

## 1. Sprint Goal
Complete checkout flow beyond reservation by introducing payment authorization, compensation on failures, and customer notifications with reliability controls.

## 2. Scope
### In Scope
- `payment-service` and `notification-service` foundations
- Payment authorization flow integrated with order lifecycle
- Compensation path to release inventory and cancel order on payment failure
- Notification dispatch for terminal order states
- Reliability hardening: outbox retries, idempotent consumers, resilience policies

### Out of Scope
- Real external payment gateway integration (use adapter mock/stub)
- Real email/SMS providers (use mock notifier adapter)
- Refunds, chargebacks, partial captures
- Advanced notification preferences/templates

## 3. Story Set and Sizing
- US-11 Payment Authorization (8)
- US-12 Payment Failure Compensation (5)
- US-13 Order Finalization Status (3)
- US-14 Notification Dispatch (5)
- US-15 Reliability and Resilience Hardening (8)

**Total:** 29 story points

## 4. Architecture Deltas
- Add services:
  - `services/payment-service`
  - `services/notification-service`
- Add config files:
  - `config-repo/payment-service.yml`
  - `config-repo/notification-service.yml`
- Extend gateway routes:
  - `/payments/**` -> `lb://PAYMENT-SERVICE`
  - `/notifications/**` -> `lb://NOTIFICATION-SERVICE` (if exposed)
- Add event contracts:
  - `payment.authorized.v1`
  - `payment.failed.v1`
  - `order.finalized.v1`
- Keep outbox + idempotent consumer pattern from Sprint 2

## 5. Execution Plan (10 working days)

### Week 1
- **Day 1-2**: Service skeletons, schema/migrations, config wiring
- **Day 3-4**: Payment authorize API + order update integration
- **Day 5**: Compensation flow and inventory release path

### Week 2
- **Day 6-7**: Notification service consumer + idempotency
- **Day 8-9**: Resilience hardening + integration and async tests
- **Day 10**: Documentation, traceability, smoke demo prep

## 6. Definition of Done (Sprint 3)
- Payment success and failure-compensation flows implemented and verified
- Notification dispatch is idempotent and retry-safe
- OpenAPI and event contracts updated
- Observability checks pass for metrics/logs/traces
- Sprint 3 smoke suite passes via gateway and async workflow checks

## 7. Risks and Mitigations
- **Risk:** Event duplication causing double side effects  
  **Mitigation:** idempotency keys + processed-event tracking.

- **Risk:** Payment dependency instability  
  **Mitigation:** timeout/retry/circuit-breaker + fallback states.

- **Risk:** Cross-service state divergence  
  **Mitigation:** explicit compensation workflow and final-state reconciliation checks.

## 8. Deliverables
- Story docs: `US-11.md` to `US-15.md`
- Test plan: `docs/TEST-PLAN-S3.md`
- Story index: `docs/USER-STORIES-S3.md`
- Sprint traceability: `SPRINT-3-TRACEABILITY.md`
- OpenAPI and event contract updates

