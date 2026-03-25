# User Stories - Sprint 2 (Orders, Inventory, Reliability)

**Project:** E-Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## Epic Alignment
- EP-04 Order Management
- EP-05 Inventory Management
- EP-06 Reliability and Async Messaging

## Definitions
- **DoR**: API draft ready, event contract drafted, failure modes identified, dependencies documented, estimated.
- **DoD**: Code + tests; OpenAPI updated; resilience configs committed; metrics/logs/traces verified; CI green; Postman smoke green; docs updated.

---

## US-06 Place Order (8 pts)
**As a** User **I want** to place an order **so that** I can purchase available products.

**Acceptance Criteria (Gherkin)**
```gherkin
Given I am authenticated and submit a valid order request
When I POST /orders
Then I receive 201 with orderId and status PENDING_INVENTORY
And the system triggers inventory reservation workflow
And invalid input returns 400
```

**Tasks**
- `order-service` create endpoint and DTO validation
- Persist order + order items with initial status
- Publish order-created event via outbox
- Add trace and request id propagation
- Tests: unit + integration

---

## US-07 Reserve and Release Inventory (5 pts)
**As a** System **I want** inventory to be reserved/released atomically **so that** stock remains consistent.

**Acceptance Criteria**
```gherkin
Given stock exists for requested SKU
When reserve is requested
Then stock is decremented atomically and reservation recorded
And insufficient stock returns 409
When release is requested
Then stock is restored for the reservation
```

**Tasks**
- `inventory-service` reserve/release endpoints
- DB schema for stock and reservations
- Conflict handling for insufficient stock
- Idempotency key support for reserve operations
- Tests: unit + integration

---

## US-08 Order Status Projection (3 pts)
**As a** User **I want** to view order status **so that** I can track progress.

**Acceptance Criteria**
```gherkin
Given an order exists
When I GET /orders/{id}
Then I receive current status and item details
And unknown order id returns 404
```

**Tasks**
- Query endpoint in `order-service`
- Map domain model to response DTO
- Problem Details error mapping
- Tests

---

## US-09 Reliable Event Publish (Outbox) (8 pts)
**As a** Platform **I want** event publication to be reliable **so that** async flows do not lose messages.

**Acceptance Criteria**
```gherkin
Given order transaction commits successfully
When outbox dispatcher runs
Then pending outbox events are published to RabbitMQ
And published events are marked sent
And retries handle transient broker failures
```

**Tasks**
- Outbox table + migration in `order-service`
- Outbox writer in same DB transaction as order create
- Scheduled dispatcher and retry/backoff policy
- Idempotent consumer handling in `inventory-service`
- Tests: integration + failure scenarios

---

## US-10 Resilient S2S Failure Handling (5 pts)
**As a** Platform **I want** resilient service-to-service calls **so that** order flow handles dependency failures gracefully.

**Acceptance Criteria**
```gherkin
Given inventory-service is slow or unavailable
When order-service calls inventory through Feign
Then timeout and retry policies are applied
And circuit breaker opens after configured failures
And order remains in recoverable status PENDING_INVENTORY
```

**Tasks**
- OpenFeign client from order-service to inventory-service
- Resilience4j timeout/retry/circuit breaker config
- Header propagation (`Authorization`, `X-Request-Id`, `traceparent`)
- Metrics and logs for retries and breaker state
- Tests: resilience and fallback behavior

---

## Sprint 2 Definition of Done (recap)
- Unit coverage >= 70% for new order/inventory core logic
- Integration tests for order create, reserve/release, and not-found/conflict paths
- Async workflow tests for outbox -> RabbitMQ -> consumer
- OpenAPI specs updated and committed
- Retry/circuit-breaker metrics observable in Prometheus
- Logs include `traceId`; traces visible across gateway -> order -> inventory
- Postman Sprint 2 smoke suite passes

