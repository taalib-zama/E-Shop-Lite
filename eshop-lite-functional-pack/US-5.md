Creating `US-5.md` in the same implementation-ready style as US-2, US-3, and US-4, aligned to your current catalog-service skeleton.

***

## FRD - User Story 5: Get Product by ID

**ID:** US-05\
**Service:** `catalog-service` (port 8082, name `CATALOG-SERVICE`)\
**Gateway route:** `GET /products/{id}` -> `lb://CATALOG-SERVICE/products/{id}`\
**Status:** Ready for implementation

### 1) User Story

> As a visitor or user, I want to view a specific product by ID so that I can see full product details.

### 2) Business Rules

* **BR-01 Public detail access:** Product detail is publicly readable in Sprint 1.
* **BR-02 Canonical lookup key:** Product retrieval uses UUID `id`.
* **BR-03 Not found handling:** Unknown IDs must return `404` with Problem Details style response.
* **BR-04 Input validation:** Invalid UUID path value should return `400`.
* **BR-05 Stable contract:** Detail response returns consistent product fields for client display.
* **BR-06 Traceable requests:** Product-detail lookups must be observable via logs/metrics/traces.

### 3) Functional Requirements

#### 3.1 Endpoint - Product Detail

`GET /products/{id}` (public)

**Path Parameters**

* `id` (required, UUID): unique product identifier

**Example Request**

```http
GET /products/f8a5b4b2-6d67-4eb6-89e2-340f73f7fc67
```

**Validation Rules**

* Reject malformed UUID as `400`.
* If UUID is valid but not present in DB, return `404`.

**Processing**

1. Parse and validate path variable `id`.
2. Query repository by `id`.
3. If found, map to response DTO and return `200`.
4. If not found, return `404` Problem Details payload.
5. Emit read-path metrics and traceable logs.

**Success (200 OK)**

```json
{
  "id": "f8a5b4b2-6d67-4eb6-89e2-340f73f7fc67",
  "name": "Wireless ANC Headphones",
  "sku": "WH-ANC-1001",
  "price": 12999.00,
  "currency": "INR",
  "description": "Bluetooth 5.3, 40h battery",
  "category": "Electronics",
  "createdAt": "2026-03-20T10:15:00Z",
  "updatedAt": "2026-03-20T10:15:00Z"
}
```

**Errors (Problem Details style)**

* 400 Invalid UUID
* 404 Product not found
* 500 Unexpected

Example 404:

```json
{
  "type": "https://eshop-lite/errors/not-found",
  "title": "Product not found",
  "status": 404,
  "detail": "No product found for id: f8a5b4b2-6d67-4eb6-89e2-340f73f7fc68",
  "traceId": "a1b2c3",
  "errors": []
}
```

Example 400:

```json
{
  "type": "https://eshop-lite/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "Invalid product id format",
  "traceId": "a1b2c3",
  "errors": [
    { "field": "id", "message": "must be a valid UUID" }
  ]
}
```

#### 3.2 Data Model Impact

No schema change required for product-detail retrieval.

Reads from existing `products` fields:

* `id`
* `name`
* `sku`
* `price`
* `currency`
* `description`
* `category`
* `created_at`
* `updated_at`

#### 3.3 Security

* `GET /products/**` remains public (`permitAll`) in Sprint 1.
* No token required for product detail.
* Keep response scoped to safe public fields.

#### 3.4 Observability

* **Metrics:**
  * `catalog_product_get_requests_total{outcome="success|not_found|invalid"}`
  * `catalog_product_get_duration_seconds`
* **Logs:** structured JSON with `traceId`, `productId`, `outcome`.
* **Tracing:** request traces visible in Jaeger for gateway -> catalog-service.

#### 3.5 Non-functional

* p95 detail latency `< 200ms` for normal dev dataset.
* Not-found lookups should return quickly (`< 100ms` typical local target).
* Endpoint must remain lightweight and cache-friendly for future read optimization.

***

## Tech Lead Guidance - How to Implement in the Skeleton

You already have a baseline detail endpoint in `services/catalog-service`:

* `ProductController.get(@PathVariable UUID id)`
* repository lookup via `repo.findById(id)`

Apply these deltas for contract consistency.

### A) Normalize not-found response reason

Current code throws `new ResponseStatusException(HttpStatus.NOT_FOUND)`.

Recommended:

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
```

This gives predictable error title/detail mapping.

### B) Add/align global exception handler

Catalog service currently needs consistent Problem Details responses similar to user-service docs.

Cover at least:

* `MethodArgumentTypeMismatchException` (invalid UUID) -> `400`
* `ResponseStatusException` -> mapped `4xx/5xx`
* generic fallback -> `500`

Include `type`, `title`, `status`, `detail`, `traceId`, `errors`.

### C) Return DTO instead of entity (recommended)

Current controller returns `Product` entity directly.

Recommended:

* introduce `ProductDetailResponse`
* map entity fields explicitly
* avoid leaking future internal fields

### D) Add detail metrics and structured logs

Instrument get-by-id path with timer and outcome counters.

Log request outcome with `traceId` and `productId`.

### E) Gateway and discovery alignment

Route already exists: `/products/**` -> `lb://CATALOG-SERVICE`.

Verify runtime path:

* catalog service registers in Eureka as `CATALOG-SERVICE`
* gateway resolves route via discovery
* trace context propagates to Jaeger

### F) Future optimization note

If read traffic grows, consider caching product detail by `id` (`@Cacheable`) with short TTL.

Keep disabled by default in Sprint 1.

***

## QA - Test Plan for US-05

### A) API Functional Tests (positive)

1. **US05-P1** Get existing product by valid ID -> `200`.
2. **US05-P2** Response includes expected detail fields (`id`, `name`, `sku`, `price`, `currency`, `description`, `category`).

### B) Validation and Negative

3. **US05-N1** Random valid UUID not in DB -> `404`.
4. **US05-N2** Malformed UUID path -> `400`.

### C) Security

5. **US05-S1** Request without token -> `200` for existing product.
6. **US05-S2** Request with valid token -> `200` (same behavior).

### D) Observability

7. **US05-O1** get-by-id counters increment for success/not-found/invalid.
8. **US05-O2** trace visible in Jaeger (gateway -> catalog-service).
9. **US05-O3** logs include `traceId`, `productId`, and outcome.

### E) Performance

10. **US05-Perf1** p95 below 200ms for seeded dataset.

***

## Example RestAssured Tests (integration)

```java
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class ProductDetailIT {
  static { RestAssured.baseURI = "http://localhost:8080"; }

  @Test
  void shouldGetExistingProduct200() {
    String existingId = "f8a5b4b2-6d67-4eb6-89e2-340f73f7fc67";

    RestAssured.given()
      .get("/products/{id}", existingId)
      .then().statusCode(200)
      .body("id", equalTo(existingId))
      .body("name", not(blankOrNullString()))
      .body("sku", not(blankOrNullString()));
  }

  @Test
  void shouldReturn404WhenProductNotFound() {
    String missingId = "11111111-2222-3333-4444-555555555555";

    RestAssured.given()
      .get("/products/{id}", missingId)
      .then().statusCode(404);
  }

  @Test
  void shouldReturn400ForInvalidUuid() {
    RestAssured.given()
      .get("/products/{id}", "not-a-uuid")
      .then().statusCode(400);
  }
}
```

***

## OpenAPI (drop into `docs/openapi/catalog-service.yaml`)

```yaml
paths:
  /products/{id}:
    get:
      summary: Get product by ID
      tags: [Catalog]
      parameters:
        - in: path
          name: id
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Product detail
          content:
            application/json:
              schema:
                type: object
                properties:
                  id: { type: string, format: uuid }
                  name: { type: string }
                  sku: { type: string }
                  price: { type: number, format: double }
                  currency: { type: string }
                  description: { type: string }
                  category: { type: string }
                  createdAt: { type: string, format: date-time }
                  updatedAt: { type: string, format: date-time }
        '400':
          description: Invalid ID format
        '404':
          description: Product not found
```

***

## Definition of Done (US-05)

* [ ] `GET /products/{id}` returns `200` with product detail for existing ID.
* [ ] Missing product returns `404` with Problem Details style payload.
* [ ] Invalid UUID returns `400` with validation-style payload.
* [ ] Endpoint remains publicly accessible through gateway route.
* [ ] Metrics, logs, and traces are visible for get-by-id path.
* [ ] Integration tests and Postman checks pass.
* [ ] OpenAPI contract updated.

***

## Implementation Checklist (short)

1. Keep `ProductController.get` and set explicit not-found reason.
2. Add/align catalog global exception handling for `400/404/500` Problem Details.
3. Introduce response DTO for detail output (recommended).
4. Add get-by-id counters and timer metrics.
5. Add structured logs with `traceId`, `productId`, and outcome.
6. Add/update integration tests for success, missing, and invalid-id scenarios.
7. Update OpenAPI and Postman collection.
8. Commit: `feat(catalog): get product by id (US-05)`.

***

When you are ready, I can also generate a Sprint 1 traceability matrix file mapping US-01..US-05 to FR IDs and test cases.

