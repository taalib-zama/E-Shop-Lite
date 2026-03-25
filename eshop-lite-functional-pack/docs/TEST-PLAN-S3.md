# Test Plan and Test Cases - Sprint 3

**Project:** E-Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Test Strategy
- **Unit Tests**: payment, compensation, notification handlers, idempotency guards.
- **Integration Tests**: Spring Boot + Testcontainers (Postgres + RabbitMQ).
- **Contract/API Tests**: order/payment endpoints via gateway.
- **Async Workflow Tests**: outbox publish, consumer idempotency, compensation flow.
- **Resilience Tests**: timeout/retry/circuit-breaker/fallback for payment and notification dependencies.
- **Observability Tests**: Prometheus metrics, structured logs, Jaeger traces.

## 2. Test Environments and Data
- **Local**: Docker Compose with Postgres, RabbitMQ, Prometheus, Grafana, Loki, Promtail, Jaeger.
- **Seed Data**:
  - User token fixtures
  - Reserved-order fixtures from Sprint 2 flow
  - Payment adapter mock responses (authorized/declined/timeout)

## 3. Entry and Exit Criteria
- **Entry**: order/inventory baseline available, payment/notification services compile, migrations applied, broker up.
- **Exit**: critical scenarios pass, compensation validated, resilience checks verified, smoke suite green.

## 4. Test Cases

### 4.1 Payment
- **TC-S3-P-01** Payment authorize success -> order `CONFIRMED`.
- **TC-S3-P-02** Payment declined -> compensation initiated.
- **TC-S3-P-03** Payment timeout with retries -> fallback/recoverable path.

### 4.2 Compensation
- **TC-S3-C-01** Failed payment releases inventory and cancels order.
- **TC-S3-C-02** Compensation idempotency on duplicate trigger.

### 4.3 Order Projection
- **TC-S3-O-01** `GET /orders/{id}` shows final status + payment summary.
- **TC-S3-O-02** Unknown order id -> `404` Problem Details.

### 4.4 Notifications
- **TC-S3-N-01** Terminal order event creates notification record.
- **TC-S3-N-02** Duplicate event does not duplicate notification.
- **TC-S3-N-03** Notification transient failure retries and eventually succeeds.

### 4.5 Resilience and S2S
- **TC-S3-R-01** Payment client timeout triggers retry.
- **TC-S3-R-02** Circuit breaker opens after repeated payment failures.
- **TC-S3-R-03** Header propagation (`Authorization`, `X-Request-Id`, `traceparent`) preserved.

### 4.6 Observability
- **TC-S3-OBS-01** Metrics include payment/notification/outbox and resilience counters.
- **TC-S3-OBS-02** Logs include `traceId`, `orderId`, `paymentId`, `eventId`.
- **TC-S3-OBS-03** Trace continuity visible across gateway -> order -> payment -> inventory/notification.

## 5. Reporting
- CI publishes Surefire/Failsafe reports for payment and notification modules.
- CI publishes async/reliability test summary.
- Postman Sprint 3 smoke run can be added as a pipeline stage.

