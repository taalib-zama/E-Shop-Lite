
## FRD - User Story 9: Reliable Event Publish (Outbox)

**ID:** US-09  
**Service:** `order-service` (publisher) + `inventory-service` (consumer)  
**Infra:** RabbitMQ  
**Status:** Ready

### 1) User Story
As a platform, I want reliable event publication so async flows do not lose messages.

### 2) Business Rules
- Outbox record is written in same DB transaction as order state change.
- Dispatcher publishes pending events and marks them sent atomically.
- Failed publish attempts are retried with backoff.
- Consumers must be idempotent by event ID.
- Event schema is versioned.

### 3) Functional Requirements
#### 3.1 Event Flow
1. `POST /orders` transaction saves order + outbox row (`status=PENDING`).
2. Scheduler polls outbox and publishes `order.created.v1`.
3. On broker ack, mark outbox row `SENT`.
4. On failure, increment retry count and reschedule.

#### 3.2 Event Envelope Example
```json
{
  "eventId": "5f7a33f1-1ed3-47d0-b3a1-e262fd5d1f11",
  "eventType": "order.created.v1",
  "occurredAt": "2026-03-26T12:00:02Z",
  "traceId": "a1b2c3",
  "payload": {
    "orderId": "2f5d5485-9180-4e1f-9a4f-74f43dc8c718",
    "userId": "a6c5a0de-2b0f-4a1d-9c5d-1f02e0a5e9e2",
    "items": [{"sku":"WH-ANC-1001","quantity":1}]
  }
}
```

### 4) Data Model
- `outbox_events(id, aggregate_type, aggregate_id, event_type, payload_json, status, retry_count, next_attempt_at, created_at, sent_at)`
- `processed_events(event_id, consumer_name, processed_at)` in consumer service for idempotency.

### 5) Security, Observability, NFR
- Do not include secrets in event payload.
- Metrics:
  - `outbox_pending_events_total`
  - `outbox_publish_success_total`
  - `outbox_publish_failures_total`
  - `outbox_dispatch_duration_seconds`
- Logs include `traceId`, `eventId`, `aggregateId`, `attempt`.
- At-least-once delivery with exactly-once-effect via idempotent consumer.

### 6) Tech Lead Guidance
- Add Flyway migration for outbox table in `order-service`.
- Add `OutboxWriter` in same order transaction.
- Add scheduled `OutboxDispatcher` with retry/backoff.
- Configure RabbitMQ exchange/queue/routing key.
- Add idempotent consumer logic in `inventory-service`.

### 7) QA Matrix
- **US09-P1** order create writes outbox row.
- **US09-P2** dispatcher publishes and marks event sent.
- **US09-N1** broker failure increments retry count.
- **US09-N2** duplicate event does not double-apply consumer side effects.
- **US09-O1** outbox metrics/logs/traces visible.

### 8) Integration Test Example
```java
@Test
void shouldPublishOutboxEventAndMarkSent() {
  // Create order -> verify outbox PENDING
  // Run dispatcher tick -> assert message consumed
  // Assert outbox row status becomes SENT
}
```

### 9) OpenAPI/Contract Notes
- API surface remains in `US-06`; this story adds async contract docs.
- Add event contract file: `docs/EVENT-CONTRACTS.md` (recommended).

### 10) DoD and Checklist
- [ ] Outbox table and writer implemented.
- [ ] Dispatcher publishes reliably with retries.
- [ ] Consumer idempotency implemented.
- [ ] Metrics/logs/traces for outbox flow verified.
- [ ] Async integration tests pass.

Checklist:
1. Add migration + entity/repo for outbox.
2. Add transactional writer and scheduler dispatcher.
3. Configure RabbitMQ topology.
4. Add idempotent consumer guards.
5. Add failure-path tests.
6. Commit: `feat(order): outbox reliable publish (US-09)`.

