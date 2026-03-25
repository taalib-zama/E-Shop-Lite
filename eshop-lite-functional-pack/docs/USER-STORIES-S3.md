# User Stories - Sprint 3 (Payment, Compensation, Notifications)

**Project:** E-Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## Epic Alignment
- EP-07 Payment and Checkout Completion
- EP-08 Compensation and Consistency
- EP-09 Customer Notifications

## Definitions
- **DoR**: API/event contracts drafted, failure modes identified, dependencies listed, estimated.
- **DoD**: Code + tests, OpenAPI updated, outbox/retry/idempotency verified, metrics/logs/traces verified, CI green, smoke checks green.

---

## US-11 Payment Authorization (8 pts)
**As a** System **I want** to authorize payment for reserved orders **so that** orders can be confirmed.

**Acceptance Criteria (Gherkin)**
```gherkin
Given an order is PENDING_INVENTORY and stock is reserved
When payment authorization is requested
Then payment is marked AUTHORIZED on success
And order transitions to CONFIRMED
And authorization failure returns a recoverable failure outcome
```

**Tasks**
- Add `payment-service` authorize endpoint
- Persist payment transaction state (`INITIATED`, `AUTHORIZED`, `FAILED`)
- Emit payment result event via outbox
- Update order state on payment success
- Tests: unit + integration

---

## US-12 Payment Failure Compensation (5 pts)
**As a** Platform **I want** compensation when payment fails **so that** inventory and order state stay consistent.

**Acceptance Criteria**
```gherkin
Given payment authorization fails or times out permanently
When compensation workflow executes
Then inventory reservation is released
And order transitions to CANCELLED
And compensation is idempotent
```

**Tasks**
- Add compensation handler in order workflow
- Trigger inventory release command/event
- Ensure idempotent compensation execution
- Tests for duplicate and retry scenarios

---

## US-13 Order Finalization Status (3 pts)
**As a** User **I want** final order/payment status **so that** I can see the checkout result.

**Acceptance Criteria**
```gherkin
Given payment result is processed
When I GET /orders/{id}
Then I see final status (CONFIRMED or CANCELLED)
And payment summary fields
And unknown order returns 404
```

**Tasks**
- Extend order query DTO with payment summary
- Ensure state transitions are reflected consistently
- Tests for confirmed/cancelled projections

---

## US-14 Notification Dispatch (5 pts)
**As a** User **I want** checkout result notifications **so that** I get confirmation or failure updates.

**Acceptance Criteria**
```gherkin
Given an order reaches terminal state
When notification event is consumed
Then one notification record is created and dispatched
And duplicate events do not create duplicate notifications
```

**Tasks**
- Add `notification-service` consumer endpoint/handler
- Persist notification record with idempotency key
- Implement simple email/SMS mock adapter
- Tests for success + duplicate event handling

---

## US-15 Reliability and Resilience Hardening (8 pts)
**As a** Platform **I want** resilient payment/notification workflows **so that** transient failures do not break checkout.

**Acceptance Criteria**
```gherkin
Given payment or notification dependencies are slow or unavailable
When workflows execute
Then retries and circuit breakers apply
And outbox retries eventually publish
And trace/log correlation is preserved end-to-end
```

**Tasks**
- Resilience4j config for payment and notifier clients
- Retry/backoff for outbox dispatchers
- Dead-letter strategy for irrecoverable messages
- Metrics and dashboards for retries/circuit states
- End-to-end resilience tests

---

## Sprint 3 Definition of Done (recap)
- Unit coverage >= 70% for payment/notification core logic
- Integration tests for payment success/failure and compensation
- Async tests for outbox -> broker -> consumers with idempotency
- OpenAPI specs updated for payment/notification/order changes
- Retry/circuit-breaker metrics visible in Prometheus/Grafana
- Logs include `traceId`; traces visible across gateway -> order -> payment -> inventory/notification
- Sprint 3 smoke suite passes

