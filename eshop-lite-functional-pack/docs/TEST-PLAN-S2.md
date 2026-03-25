# Test Plan and Test Cases - Sprint 2

**Project:** E-Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Test Strategy
- **Unit Tests**: JUnit 5, Mockito for service logic, validators, mappers.
- **Integration Tests**: Spring Boot Test with Testcontainers (Postgres + RabbitMQ).
- **Contract/API Tests**: RestAssured/Postman for order and inventory endpoints via gateway.
- **Resilience Tests**: verify timeout, retry, circuit-breaker and fallback states.
- **Async Tests**: outbox dispatch, message publish, idempotent consume, retry handling.
- **Observability Tests**: metrics, structured logs with `traceId`, Jaeger spans across services.

## 2. Test Environments and Data
- **Local**: Docker Compose with Postgres, RabbitMQ, Prometheus, Grafana, Loki, Promtail, Jaeger.
- **Seed Data**:
  - User token fixtures (`USER`, `ADMIN`)
  - Inventory stock fixtures for success and conflict scenarios
  - Pre-seeded product IDs/SKUs for order items

## 3. Entry and Exit Criteria
- **Entry**: `order-service` and `inventory-service` compile, DB migrations applied, RabbitMQ running, OpenAPI draft ready.
- **Exit**: critical tests green, smoke suite green via gateway, resilience and observability checks verified.

## 4. Test Cases

### 4.1 Order API
- **TC-S2-O-01** Place order success  
  **Steps**: POST `/orders` with valid token and payload  
  **Expected**: 201, status `PENDING_INVENTORY`, outbox row created.

- **TC-S2-O-02** Place order invalid payload  
  **Expected**: 400 Problem Details payload.

- **TC-S2-O-03** Get order by id success  
  **Steps**: GET `/orders/{id}`  
  **Expected**: 200 with status and items.

- **TC-S2-O-04** Get order by id not found  
  **Expected**: 404 Problem Details payload.

### 4.2 Inventory API
- **TC-S2-I-01** Reserve stock success  
  **Expected**: 200 and stock decremented.

- **TC-S2-I-02** Reserve stock insufficient quantity  
  **Expected**: 409 Problem Details payload.

- **TC-S2-I-03** Release reservation success  
  **Expected**: 200 and stock restored.

- **TC-S2-I-04** Idempotent reserve with same key  
  **Expected**: duplicate request does not double-decrement stock.

### 4.3 Async and Outbox
- **TC-S2-A-01** Outbox publish success  
  **Expected**: pending outbox events published and marked sent.

- **TC-S2-A-02** Broker transient failure retry  
  **Expected**: retry attempts occur; message eventually published.

- **TC-S2-A-03** Consumer idempotency  
  **Expected**: duplicate event does not apply side-effects twice.

### 4.4 Resilience and S2S
- **TC-S2-R-01** Inventory timeout triggers retry  
  **Expected**: configured retries attempted.

- **TC-S2-R-02** Circuit breaker opens on repeated failures  
  **Expected**: fallback path used, status remains recoverable.

- **TC-S2-R-03** Header propagation  
  **Expected**: `Authorization`, `X-Request-Id`, and `traceparent` propagated to downstream call.

### 4.5 Security and Gateway
- **TC-S2-G-01** Missing token on protected order endpoint  
  **Expected**: 401.

- **TC-S2-G-02** Invalid token on protected order endpoint  
  **Expected**: 401.

- **TC-S2-G-03** Non-privileged request on admin-only inventory admin path (if enabled)  
  **Expected**: 403.

### 4.6 Observability
- **TC-S2-OBS-01** Metrics exposed for new services  
  **Expected**: `/actuator/prometheus` includes order/inventory + resilience metrics.

- **TC-S2-OBS-02** Logs include `traceId` and event ids  
  **Expected**: correlated logs across gateway, order, inventory.

- **TC-S2-OBS-03** Jaeger trace continuity  
  **Expected**: gateway -> order-service -> inventory-service spans visible.

## 5. Reporting
- CI publishes Surefire/Failsafe reports for order and inventory services.
- CI publishes test summary for async/resilience suite.
- Postman Sprint 2 smoke run can be integrated as a pipeline stage.

