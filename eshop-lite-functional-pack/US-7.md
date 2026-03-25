## FRD - User Story 7: Reserve and Release Inventory

**ID:** US-07  
**Service:** `inventory-service` (planned `INVENTORY-SERVICE`)  
**Gateway:** `POST /inventory/reservations` and `POST /inventory/reservations/{id}/release` -> `lb://INVENTORY-SERVICE`  
**Status:** Ready

### 1) User Story
As a system, I want inventory reserve/release to be atomic so stock stays consistent.

### 2) Business Rules
- Reserve and stock decrement happen in one transaction.
- Insufficient stock returns `409`.
- `Idempotency-Key` prevents duplicate reserve side effects.
- Release restores stock once only.

### 3) Functional Requirements
**Reserve Endpoint** `POST /inventory/reservations` (protected)

Request example:
```json
{
  "orderId": "2f5d5485-9180-4e1f-9a4f-74f43dc8c718",
  "items": [{"sku":"WH-ANC-1001","quantity":1}]
}
```

Success `200`:
```json
{
  "reservationId": "0d47c3d4-8b18-42aa-a25b-1f6c0357ee8a",
  "status": "RESERVED"
}
```

**Release Endpoint** `POST /inventory/reservations/{id}/release` (protected)

Success `200`:
```json
{
  "reservationId": "0d47c3d4-8b18-42aa-a25b-1f6c0357ee8a",
  "status": "RELEASED"
}
```

Error example `409`:
```json
{
  "type": "https://eshop-lite/errors/conflict",
  "title": "Insufficient stock",
  "status": 409,
  "detail": "Requested quantity exceeds available stock",
  "traceId": "a1b2c3",
  "errors": []
}
```

### 4) Data Model
- `inventory_stock(sku, available_quantity, reserved_quantity, updated_at)`
- `inventory_reservations(id, order_id, status, idempotency_key, created_at, updated_at)`
- `inventory_reservation_items(id, reservation_id, sku, quantity)`

### 5) Security, Observability, NFR
- JWT/service-token protected endpoints.
- Propagate `Authorization`, `X-Request-Id`, `traceparent`.
- Metrics: `inventory_reserve_requests_total`, `inventory_release_requests_total`, `inventory_reserve_duration_seconds`.
- p95 reserve latency target `< 300ms`.

### 6) Tech Lead Guidance
- Create `services/inventory-service` module with Flyway.
- Implement transactional reserve/release service with row locking.
- Add idempotency handling by unique `idempotency_key`.
- Add Problem Details exception handler.
- Add gateway route `/inventory/**` and config-repo file.

### 7) QA Matrix
- **US07-P1** reserve success -> `200`
- **US07-P2** release success -> `200`
- **US07-N1** insufficient stock -> `409`
- **US07-N2** invalid payload -> `400`
- **US07-S1** missing token -> `401`
- **US07-O1** metrics/logs/traces visible

### 8) RestAssured Example
```java
RestAssured.given()
  .header("Authorization", "Bearer <token>")
  .header("Idempotency-Key", "reserve-order-1")
  .contentType("application/json")
  .body("{\"orderId\":\"2f5d...\",\"items\":[{\"sku\":\"WH-ANC-1001\",\"quantity\":1}]}")
  .post("/inventory/reservations")
  .then().statusCode(200);
```

### 9) OpenAPI Snippet
```yaml
paths:
  /inventory/reservations:
    post:
      summary: Reserve inventory
      responses:
        '200': { description: Reserved }
        '409': { description: Insufficient stock }
  /inventory/reservations/{id}/release:
    post:
      summary: Release reservation
      responses:
        '200': { description: Released }
        '404': { description: Not found }
```

### 10) DoD and Checklist
- [ ] Reserve/release APIs implemented with atomic behavior.
- [ ] Idempotency enforced.
- [ ] Problem Details + tests for `400/401/404/409`.
- [ ] Observability validated.
- [ ] OpenAPI updated.

Checklist:
1. Create module and migrations.
2. Implement reserve/release service.
3. Add idempotency and error mapping.
4. Add tests and gateway route.
5. Commit: `feat(inventory): reserve release flow (US-07)`.

