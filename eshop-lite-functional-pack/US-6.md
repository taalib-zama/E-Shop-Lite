
## FRD - User Story 6: Place Order

**ID:** US-06\
**Service:** `order-service` (planned, name `ORDER-SERVICE`)\
**Gateway route:** `POST /orders` -> `lb://ORDER-SERVICE/orders`\
**Status:** Ready for implementation

### 1) User Story

> As a user, I want to place an order so that I can purchase selected products.

### 2) Business Rules

- **BR-01 Auth required:** Only authenticated users can place orders.
- **BR-02 Valid line items:** Order must include at least one item with positive quantity.
- **BR-03 Price integrity:** Unit price must be validated server-side (trusted catalog pricing policy).
- **BR-04 Initial state:** New order is created as `PENDING_INVENTORY`.
- **BR-05 Event trigger:** Order creation must enqueue outbox event for inventory workflow.
- **BR-06 Safe failures:** Validation errors return `400`; auth errors `401`; downstream dependency failures should keep order in recoverable state.

### 3) Functional Requirements

#### 3.1 Endpoint - Create Order

`POST /orders` (protected)

**Headers**

- `Authorization: Bearer <jwt>`
- `X-Request-Id: <uuid>` (recommended)

**Request (JSON)**

```json
{
  "items": [
    { "sku": "WH-ANC-1001", "quantity": 1 },
    { "sku": "KB-MECH-2002", "quantity": 2 }
  ],
  "currency": "INR",
  "shippingAddress": {
    "line1": "221B Baker Street",
    "city": "London",
    "country": "UK",
    "postalCode": "NW16XE"
  }
}
```

**Validation**

- `items`: required, min size 1
- `items[].sku`: required, non-blank, max 64
- `items[].quantity`: integer, min 1
- `currency`: optional, default `INR`
- address fields: required for line1/city/country/postalCode

**Processing**

1. Validate JWT and derive `userId` from token subject.
2. Validate request payload.
3. Resolve authoritative pricing for each SKU (catalog read path or local snapshot strategy).
4. Create order + order items in single transaction.
5. Set status = `PENDING_INVENTORY`.
6. Write outbox event `order.created` in same transaction.
7. Return order summary response.

**Success (201 Created)**

```json
{
  "orderId": "2f5d5485-9180-4e1f-9a4f-74f43dc8c718",
  "userId": "a6c5a0de-2b0f-4a1d-9c5d-1f02e0a5e9e2",
  "status": "PENDING_INVENTORY",
  "totalAmount": 16997.00,
  "currency": "INR",
  "createdAt": "2026-03-26T12:00:00Z"
}
```

**Errors (Problem Details style)**

- 400 Validation error
- 401 Missing/invalid token
- 404 SKU not found (if strict SKU validation enabled)
- 500 Unexpected

Example 400:

```json
{
  "type": "https://eshop-lite/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "Field constraints violated",
  "traceId": "a1b2c3",
  "errors": [
    { "field": "items[0].quantity", "message": "must be greater than or equal to 1" }
  ]
}
```

#### 3.2 Data Model Impact

Add `orders_db` schema (planned):

- `orders`
  - `id UUID PK`
  - `user_id UUID`
  - `status VARCHAR(32)`
  - `total_amount DECIMAL(12,2)`
  - `currency VARCHAR(3)`
  - `created_at TIMESTAMP`
  - `updated_at TIMESTAMP`
- `order_items`
  - `id UUID PK`
  - `order_id UUID FK`
  - `sku VARCHAR(64)`
  - `quantity INT`
  - `unit_price DECIMAL(12,2)`
  - `line_total DECIMAL(12,2)`

#### 3.3 Security

- JWT required for `POST /orders`.
- `userId` derived from token; do not trust client-provided user identity.
- Propagate `Authorization`, `X-Request-Id`, and `traceparent` across service boundaries.

#### 3.4 Observability

- **Metrics:**
  - `order_create_requests_total{outcome="success|failure"}`
  - `order_create_duration_seconds`
  - `order_create_pending_inventory_total`
- **Logs:** structured JSON with `traceId`, `orderId`, `userId`, `status`.
- **Tracing:** gateway -> order-service span continuity.

#### 3.5 Non-functional

- p95 create latency `< 500ms` under dev load.
- Must remain available during transient inventory dependency issues by using recoverable initial status.

***

## Tech Lead Guidance - How to Implement in the Skeleton

Current repo does not yet include `order-service`; implement these deltas.

### A) Create new module and service skeleton

- Add `services/order-service` with Spring Boot app.
- Register as `ORDER-SERVICE` with discovery.
- Add `order-service.yml` in `config-repo`.

### B) Define API and domain

- `OrderController` with `POST /orders`.
- DTOs: `CreateOrderRequest`, `CreateOrderItemRequest`, `OrderResponse`.
- Entities: `Order`, `OrderItem` with Flyway migration.

### C) Transaction and outbox write

- Persist order and outbox message in same transaction.
- Outbox payload should include order ID, user ID, item list, and request correlation fields.

### D) Error contract and validation

- Implement global exception handler using Problem Details shape used in Sprint 1.

### E) Gateway and route

- Extend gateway routes to include `/orders/**` -> `lb://ORDER-SERVICE`.

***

## QA - Test Plan for US-06

### A) API Functional Tests (positive)

1. **US06-P1** Valid authenticated order returns `201` with `PENDING_INVENTORY`.
2. **US06-P2** Total amount calculation matches line items.
3. **US06-P3** Outbox row created for successful order.

### B) Validation and Negative

4. **US06-N1** Empty items list -> `400`.
5. **US06-N2** Quantity `0` -> `400`.
6. **US06-N3** Missing address fields -> `400`.

### C) Security

7. **US06-S1** Missing token -> `401`.
8. **US06-S2** Invalid token -> `401`.

### D) Observability

9. **US06-O1** create metrics increment.
10. **US06-O2** logs contain `traceId` and `orderId`.
11. **US06-O3** Jaeger trace visible through gateway.

***

## Example RestAssured Tests (integration)

```java
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class OrderCreateIT {
  static { RestAssured.baseURI = "http://localhost:8080"; }

  @Test
  void shouldCreateOrder201() {
    String userToken = "<inject-user-token-fixture>";

    RestAssured.given()
      .header("Authorization", "Bearer " + userToken)
      .header("X-Request-Id", "a1b2c3d4-e5f6-4a5a-9c1c-001122334455")
      .contentType("application/json")
      .body("{" +
          "\"items\":[{\"sku\":\"WH-ANC-1001\",\"quantity\":1}]," +
          "\"currency\":\"INR\"," +
          "\"shippingAddress\":{\"line1\":\"221B Baker Street\",\"city\":\"London\",\"country\":\"UK\",\"postalCode\":\"NW16XE\"}" +
          "}")
      .post("/orders")
      .then().statusCode(201)
      .body("orderId", not(blankOrNullString()))
      .body("status", equalTo("PENDING_INVENTORY"));
  }

  @Test
  void shouldRejectInvalidPayload400() {
    String userToken = "<inject-user-token-fixture>";

    RestAssured.given()
      .header("Authorization", "Bearer " + userToken)
      .contentType("application/json")
      .body("{\"items\":[]}")
      .post("/orders")
      .then().statusCode(400);
  }
}
```

***

## OpenAPI (drop into `docs/openapi/order-service.yaml`)

```yaml
paths:
  /orders:
    post:
      summary: Place order
      tags: [Orders]
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [items, shippingAddress]
              properties:
                items:
                  type: array
                  minItems: 1
                  items:
                    type: object
                    required: [sku, quantity]
                    properties:
                      sku: { type: string, maxLength: 64 }
                      quantity: { type: integer, minimum: 1 }
                currency: { type: string, minLength: 3, maxLength: 3, example: INR }
                shippingAddress:
                  type: object
                  required: [line1, city, country, postalCode]
      responses:
        '201':
          description: Order created
        '400':
          description: Validation error
        '401':
          description: Unauthorized
```

***

## Definition of Done (US-06)

- [ ] `POST /orders` creates order and returns `201` with `PENDING_INVENTORY`.
- [ ] Order + order items persisted transactionally.
- [ ] Outbox event persisted for order creation.
- [ ] Problem Details responses for `400/401/500`.
- [ ] Metrics/logs/traces visible for create path.
- [ ] Integration tests pass.
- [ ] OpenAPI updated.

***

## Implementation Checklist (short)

1. Create `order-service` module and config.
2. Add Flyway migrations for orders and order_items.
3. Implement create API and DTO validation.
4. Add transactional outbox writer.
5. Add exception handler and observability instrumentation.
6. Add tests for success, validation, and auth paths.
7. Update gateway route and OpenAPI.
8. Commit: `feat(order): place order flow (US-06)`.

