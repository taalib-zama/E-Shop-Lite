
## FRD - User Story 8: Order Status Projection

**ID:** US-08  
**Service:** `order-service` (`ORDER-SERVICE`)  
**Gateway:** `GET /orders/{id}` -> `lb://ORDER-SERVICE/orders/{id}`  
**Status:** Ready

### 1) User Story
As a user, I want to view order status so that I can track progress.

### 2) Business Rules
- Only owner (or admin) can access order details.
- Unknown order ID returns `404`.
- Response includes status timeline-friendly fields (`status`, timestamps, item summary).
- Status values are controlled enum (`PENDING_INVENTORY`, `RESERVED`, `CONFIRMED`, `FAILED_INVENTORY`, `CANCELLED`).

### 3) Functional Requirements
`GET /orders/{id}` (protected)

Success `200`:
```json
{
  "orderId": "2f5d5485-9180-4e1f-9a4f-74f43dc8c718",
  "userId": "a6c5a0de-2b0f-4a1d-9c5d-1f02e0a5e9e2",
  "status": "PENDING_INVENTORY",
  "totalAmount": 16997.00,
  "currency": "INR",
  "items": [
    { "sku": "WH-ANC-1001", "quantity": 1, "unitPrice": 12999.00 }
  ],
  "createdAt": "2026-03-26T12:00:00Z",
  "updatedAt": "2026-03-26T12:00:00Z"
}
```

Error `404` example:
```json
{
  "type": "https://eshop-lite/errors/not-found",
  "title": "Order not found",
  "status": 404,
  "detail": "No order found for id",
  "traceId": "a1b2c3",
  "errors": []
}
```

### 4) Data Model
Uses `orders` + `order_items`; optional `order_status_history` table for timeline expansion.

### 5) Security, Observability, NFR
- JWT required; ownership check in service layer.
- Metrics: `order_get_requests_total`, `order_get_duration_seconds`.
- Logs: include `traceId`, `orderId`, `status`, `outcome`.
- p95 read latency target `< 250ms`.

### 6) Tech Lead Guidance
- Add `OrderQueryController#getById`.
- Add ownership check using JWT subject.
- Map entities to response DTO, avoid exposing internal fields.
- Reuse Problem Details handler.

### 7) QA Matrix
- **US08-P1** owner gets `200`.
- **US08-P2** response contains status + items.
- **US08-N1** unknown ID -> `404`.
- **US08-S1** missing token -> `401`.
- **US08-S2** different user access -> `403`.
- **US08-O1** metrics/logs/traces visible.

### 8) RestAssured Example
```java
RestAssured.given()
  .header("Authorization", "Bearer <user-token>")
  .get("/orders/{id}", "2f5d5485-9180-4e1f-9a4f-74f43dc8c718")
  .then().statusCode(200)
  .body("status", notNullValue());
```

### 9) OpenAPI Snippet
```yaml
paths:
  /orders/{id}:
    get:
      summary: Get order status by id
      security:
        - bearerAuth: []
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: string, format: uuid }
      responses:
        '200': { description: Order found }
        '401': { description: Unauthorized }
        '403': { description: Forbidden }
        '404': { description: Not found }
```

### 10) DoD and Checklist
- [ ] `GET /orders/{id}` implemented with ownership checks.
- [ ] Status and items returned in stable DTO.
- [ ] `404/401/403` mapped with Problem Details.
- [ ] Metrics/logs/traces verified.
- [ ] OpenAPI and tests updated.

Checklist:
1. Add query endpoint.
2. Add authorization/ownership guard.
3. Add mapping + error handler.
4. Add integration tests.
5. Commit: `feat(order): order status query (US-08)`.

