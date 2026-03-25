Great, Taalib - here is the full User Story 3 pack in the same implementation-ready style as US-1 and US-2.

***

## FRD - User Story 3: Create Product (ADMIN)

**ID:** US-03\
**Service:** `catalog-service` (port 8082, name `CATALOG-SERVICE`)\
**Gateway route:** `POST /products` -> `lb://CATALOG-SERVICE/products`\
**Status:** Ready for implementation

### 1) User Story

> As an admin, I want to create products with valid details so that they become available for browsing in the catalog.

### 2) Business Rules

* **BR-01 Role restriction:** Only authenticated users with role `ADMIN` can create products.
* **BR-02 SKU uniqueness:** Product `sku` must be unique across catalog.
* **BR-03 Price rule:** `price` must be greater than zero.
* **BR-04 Currency default:** If currency is not provided, default to `INR`.
* **BR-05 Safe errors:** Return structured errors (Problem Details style) for validation and conflict cases.
* **BR-06 Auditable:** Product creation should be traceable via logs and traces using `traceId`.

### 3) Functional Requirements

#### 3.1 Endpoint - Create Product

`POST /products` (protected, ADMIN only)

**Headers**

* `Authorization: Bearer <jwt>`

**Request (JSON)**

```json
{
  "name": "Wireless ANC Headphones",
  "sku": "WH-ANC-1001",
  "price": 12999.00,
  "currency": "INR",
  "description": "Bluetooth 5.3, 40h battery",
  "category": "Electronics"
}
```

**Validation**

* `name`: required, non-blank
* `sku`: required, non-blank, unique
* `price`: required, decimal, `>= 0.01`
* `currency`: optional; when null/blank, set to `INR`
* `description`: optional
* `category`: optional

**Processing**

1. Validate DTO fields.
2. Enforce JWT authentication and role `ADMIN`.
3. Check `sku` uniqueness (`existsBySku`).
4. Build product entity and apply defaults.
5. Persist product.
6. Return created product response.
7. Emit audit log event `PRODUCT_CREATED` with `traceId` and `productId`.

**Success (201 Created)**

```json
{
  "id": "f8a5b4b2-6d67-4eb6-89e2-340f73f7fc67",
  "name": "Wireless ANC Headphones",
  "sku": "WH-ANC-1001",
  "price": 12999.00,
  "currency": "INR",
  "description": "Bluetooth 5.3, 40h battery",
  "category": "Electronics",
  "createdAt": "2026-03-26T10:15:00Z",
  "updatedAt": "2026-03-26T10:15:00Z"
}
```

**Errors (Problem Details style)**

* 400 Validation error
* 401 Missing/invalid/expired token
* 403 Authenticated but non-admin user
* 409 Duplicate SKU
* 500 Unexpected

Example 409:

```json
{
  "type": "https://eshop-lite/errors/conflict",
  "title": "Duplicate SKU",
  "status": 409,
  "detail": "Product with same SKU already exists",
  "traceId": "a1b2c3",
  "errors": []
}
```

#### 3.2 Data Model Impact

No schema changes required for MVP create flow.

Uses existing `products` table fields:

* `id UUID PK`
* `name`
* `sku UNIQUE`
* `price DECIMAL(12,2)`
* `currency` (default `INR`)
* `description`
* `category`
* `created_at`
* `updated_at`

Optional later enhancement:

* Add `status` (`ACTIVE`, `DRAFT`, `ARCHIVED`) to support product lifecycle.

#### 3.3 Security

* Route protected by JWT and role-based authorization.
* Service-side rule: `POST /products` requires `ROLE_ADMIN`.
* Keep defense in depth: gateway forwarding plus service authorization.
* Never log JWT secrets or sensitive headers.

#### 3.4 Observability

* **Metrics:**
  * `catalog_product_create_requests_total{outcome="success|failure"}`
  * `catalog_product_create_duration_seconds`
  * `catalog_product_create_conflicts_total`
* **Logs:** structured JSON with `traceId`, `event=PRODUCT_CREATED`, `productId`, `sku`, `actor`.
* **Tracing:** span chain visible in Jaeger for gateway -> catalog-service.

#### 3.5 Non-functional

* Product create p95 latency `< 500ms` under dev load.
* Conflict/validation failures should return in `< 250ms`.
* Endpoint should remain idempotency-safe by business key (`sku`) via conflict check.

***

## Tech Lead Guidance - How to Implement in the Skeleton

You already have create-product flow in `services/catalog-service`. Implement these deltas for production-ready alignment.

### A) Keep and tighten DTO validation

Current DTO already has required fields and min price.

Recommended refinement:

```java
// src/main/java/com/eshoplite/catalog/api/CreateProductRequest.java
public record CreateProductRequest(
  @NotBlank @Size(max = 255) String name,
  @NotBlank @Size(max = 64) String sku,
  @NotNull @DecimalMin("0.01") BigDecimal price,
  @Size(min = 3, max = 3) String currency,
  String description,
  @Size(max = 100) String category
) {}
```

### B) Role enforcement path

Service security already enforces:

* `GET /products/**` -> permit all
* `POST /products` -> `hasRole("ADMIN")`

Keep this behavior and verify gateway forwards `Authorization` unchanged.

### C) Normalize create response shape

Current controller returns JPA entity directly. Recommended:

* Introduce `ProductResponse` DTO to avoid exposing internals.
* Return stable API fields and timestamp fields explicitly.

### D) Problem Details consistency

Create/extend exception handler in catalog service to align with user service contract:

* validation -> `400`
* auth -> `401/403`
* duplicate sku -> `409`
* internal -> `500`

Include `detail` and `traceId` consistently.

### E) Add metrics and audit logging

Instrument create path with:

* outcome counter
* request duration timer
* duplicate SKU counter

Log `PRODUCT_CREATED` and failure events with `traceId`.

### F) Gateway and discovery alignment

Existing gateway route already points `/products/**` to `lb://CATALOG-SERVICE`.

Confirm runtime behavior:

* catalog service registers in Eureka as `CATALOG-SERVICE`
* route is resolved through discovery
* traces are exported to Jaeger

### G) Future-ready S2S note (Sprint 2+)

When order/inventory Feign clients are introduced, keep propagating:

* `Authorization`
* `X-Request-Id`
* `traceparent`

This keeps security and tracing intact for downstream product availability checks.

***

## QA - Test Plan for US-03

### A) API Functional Tests (positive)

1. **US03-P1** Create product with valid ADMIN token -> `201`
   * Verify response includes `id`, provided fields, and default values.
2. **US03-P2** Currency omitted -> created with `currency=INR`.
3. **US03-P3** Created product is retrievable by `GET /products/{id}`.

### B) Validation and Negative

4. **US03-N1** Missing name -> `400`.
5. **US03-N2** Missing SKU -> `400`.
6. **US03-N3** Price `0` or negative -> `400`.
7. **US03-N4** Duplicate SKU -> `409`.

### C) Security

8. **US03-S1** No token on `POST /products` -> `401`.
9. **US03-S2** USER token on `POST /products` -> `403`.
10. **US03-S3** ADMIN token on `POST /products` -> `201`.

### D) Observability

11. **US03-O1** create counters increment for success/failure.
12. **US03-O2** create trace visible in Jaeger (gateway -> catalog-service).
13. **US03-O3** logs include `traceId` and `PRODUCT_CREATED` event.

### E) Performance

14. **US03-Perf1** p95 create latency under 500ms in local dev test.

***

## Example RestAssured Tests (integration)

```java
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class ProductCreateIT {
  static { RestAssured.baseURI = "http://localhost:8080"; }

  @Test
  void shouldCreateProductAsAdmin201() {
    String adminToken = "<inject-admin-token-fixture>";

    RestAssured.given()
      .header("Authorization", "Bearer " + adminToken)
      .contentType("application/json")
      .body("{" +
          "\"name\":\"Wireless ANC Headphones\"," +
          "\"sku\":\"WH-ANC-1001\"," +
          "\"price\":12999.00," +
          "\"description\":\"Bluetooth 5.3\"," +
          "\"category\":\"Electronics\"" +
          "}")
      .post("/products")
      .then().statusCode(201)
      .body("id", not(blankOrNullString()))
      .body("sku", equalTo("WH-ANC-1001"))
      .body("currency", equalTo("INR"));
  }

  @Test
  void shouldRejectUserRole403() {
    String userToken = "<inject-user-token-fixture>";

    RestAssured.given()
      .header("Authorization", "Bearer " + userToken)
      .contentType("application/json")
      .body("{\"name\":\"Mouse\",\"sku\":\"MS-100\",\"price\":999.00}")
      .post("/products")
      .then().statusCode(403);
  }

  @Test
  void shouldRejectDuplicateSku409() {
    String adminToken = "<inject-admin-token-fixture>";
    String payload = "{\"name\":\"Keyboard\",\"sku\":\"KB-101\",\"price\":1999.00}";

    RestAssured.given().header("Authorization", "Bearer " + adminToken)
      .contentType("application/json").body(payload)
      .post("/products").then().statusCode(anyOf(is(201), is(409)));

    RestAssured.given().header("Authorization", "Bearer " + adminToken)
      .contentType("application/json").body(payload)
      .post("/products").then().statusCode(409);
  }
}
```

***

## OpenAPI (drop into `docs/openapi/catalog-service.yaml`)

```yaml
paths:
  /products:
    post:
      summary: Create product (ADMIN)
      tags: [Catalog]
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [name, sku, price]
              properties:
                name: { type: string, maxLength: 255 }
                sku: { type: string, maxLength: 64 }
                price: { type: number, format: double, minimum: 0.01 }
                currency: { type: string, minLength: 3, maxLength: 3, example: INR }
                description: { type: string }
                category: { type: string }
      responses:
        '201':
          description: Product created
        '400':
          description: Validation error
        '401':
          description: Unauthorized
        '403':
          description: Forbidden (non-admin)
        '409':
          description: Duplicate SKU
```

***

## Definition of Done (US-03)

* [ ] `POST /products` requires ADMIN role and returns `201` on valid payload.
* [ ] Duplicate SKU returns `409` with Problem Details style payload.
* [ ] Invalid payload returns `400` with field-level errors.
* [ ] Missing/invalid token -> `401`, non-admin token -> `403`.
* [ ] Metrics, logs, and traces are visible for create path.
* [ ] Integration tests and Postman checks pass through gateway.
* [ ] OpenAPI contract updated.

***

## Implementation Checklist (short)

1. Keep/strengthen request validation in `CreateProductRequest`.
2. Ensure security rule `POST /products` -> `hasRole("ADMIN")` remains enforced.
3. Add/align global exception handler for Problem Details shape.
4. Add create metrics counters and timer.
5. Add structured audit logs for product create events.
6. Add/update integration tests for admin/user/no-token/duplicate cases.
7. Update OpenAPI and Postman collection.
8. Commit: `feat(catalog): create product admin flow (US-03)`.

***

When you are ready, I can also generate:

* `US-4.md` (List/Search Products),
* `US-5.md` (Get Product by ID),
* a combined Sprint 1 traceability matrix mapping US -> FR -> test cases.

