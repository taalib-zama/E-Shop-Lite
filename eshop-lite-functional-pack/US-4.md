Building `US-4.md` in the same implementation-ready format as US-2 and US-3, aligned to your current catalog-service skeleton.

***

## FRD - User Story 4: List and Search Products

**ID:** US-04\
**Service:** `catalog-service` (port 8082, name `CATALOG-SERVICE`)\
**Gateway route:** `GET /products` -> `lb://CATALOG-SERVICE/products`\
**Status:** Ready for implementation

### 1) User Story

> As a visitor or user, I want to browse and search products with filters and pagination so that I can quickly find relevant items.

### 2) Business Rules

* **BR-01 Public read access:** Product listing is available without authentication in Sprint 1.
* **BR-02 Search scope:** `query` matches product `name` and `description` (case-insensitive).
* **BR-03 Filter behavior:** `category`, `minPrice`, and `maxPrice` are optional and composable.
* **BR-04 Pagination defaults:** Default `page=0`, `size=20`.
* **BR-05 Sorting default:** Default sort is `createdAt,DESC`.
* **BR-06 Stable contract:** Response must always include pagination metadata.

### 3) Functional Requirements

#### 3.1 Endpoint - List/Search

`GET /products` (public)

**Query Parameters**

* `query` (optional, string): case-insensitive match on `name` or `description`
* `category` (optional, string): exact match filter (case-insensitive recommended)
* `minPrice` (optional, decimal): minimum product price
* `maxPrice` (optional, decimal): maximum product price
* `page` (optional, integer, default `0`, min `0`)
* `size` (optional, integer, default `20`, min `1`, max `100`)
* `sort` (optional, string, default `createdAt,DESC`)

**Example Request**

```http
GET /products?query=anc&category=Electronics&minPrice=5000&maxPrice=20000&page=0&size=20&sort=createdAt,DESC
```

**Validation Rules**

* Reject if `minPrice < 0` or `maxPrice < 0`.
* Reject if both are present and `minPrice > maxPrice`.
* Reject if `size > 100`.
* Reject unsupported sort fields/directions.

**Processing**

1. Parse and validate query parameters.
2. Build pageable object with requested/default sort.
3. Apply filters:
   * `query` on `name`/`description` (ILIKE-style behavior in Postgres).
   * `category` match.
   * price range using `minPrice` and `maxPrice`.
4. Execute query via repository.
5. Return paged response.
6. Emit read-path metrics and traceable logs.

**Success (200 OK)**

```json
{
  "content": [
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
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  },
  "sort": "createdAt,DESC"
}
```

**Errors (Problem Details style)**

* 400 Invalid query parameters
* 500 Unexpected

Example 400:

```json
{
  "type": "https://eshop-lite/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "Invalid pagination or filter parameters",
  "traceId": "a1b2c3",
  "errors": [
    { "field": "minPrice", "message": "must be less than or equal to maxPrice" }
  ]
}
```

#### 3.2 Data Model Impact

No schema changes required for Sprint 1 list/search implementation.

Uses existing `products` table fields:

* `id`
* `name`
* `sku`
* `price`
* `currency`
* `description`
* `category`
* `created_at`
* `updated_at`

Recommended index considerations for performance:

* existing unique index on `sku`
* add index on `category`
* consider trigram/full-text index for `name`/`description` in later optimization

#### 3.3 Security

* `GET /products/**` remains public (`permitAll`) in Sprint 1.
* No token required for browse/search path.
* Do not expose internal-only fields if entity evolves later.

#### 3.4 Observability

* **Metrics:**
  * `catalog_products_list_requests_total`
  * `catalog_products_list_duration_seconds`
  * `catalog_products_list_result_count`
* **Logs:** structured JSON with `traceId`, requested filters, page/size, result count.
* **Tracing:** request trace visible in Jaeger for gateway -> catalog-service.

#### 3.5 Non-functional

* p95 list/search latency `< 250ms` for normal dev dataset.
* Endpoint should support at least 100 RPS in local/dev target constraints.
* Pagination must prevent unbounded reads (`size` capped).

***

## Tech Lead Guidance - How to Implement in the Skeleton

You already have a baseline list endpoint in `services/catalog-service`. Apply these deltas.

### A) Extend `ProductController.list` parameters

Current method accepts filters but returns `findAll(PageRequest.of(page, size))`.

Add:

* `sort` request param with default `createdAt,DESC`
* validation for `page`, `size`, price range
* fallback strategy for invalid sort fields

### B) Add repository query support for filters

Current `ProductRepository` has only `findBySku` and `existsBySku`.

Recommended options:

1. Add `JpaSpecificationExecutor<Product>` and build dynamic specs.
2. Or define custom `@Query` methods with optional parameters.

Specification approach is preferable for combining optional filters cleanly.

### C) Apply default sort requirement from FRD

Use default `Sort.by(Sort.Direction.DESC, "createdAt")` when `sort` is absent.

Whitelist supported sort fields (recommended):

* `createdAt`
* `price`
* `name`

### D) Normalize response and error contracts

Current endpoint returns `Page<Product>` directly. Recommended:

* Introduce response DTO (`ProductSummaryResponse`) for output stability.
* Add/extend global exception handler for `400` Problem Details payloads.

### E) Add metrics and request diagnostics

Instrument list/search path with timer + counters and include filter metadata in logs.

Keep logs concise and safe (avoid logging very long query values unbounded).

### F) Gateway and discovery alignment

Route already exists in gateway: `/products/**` -> `lb://CATALOG-SERVICE`.

Verify runtime path:

* service registers with Eureka as `CATALOG-SERVICE`
* gateway resolves via discovery
* traces exported to Jaeger

### G) Optional optimization path (later sprint)

For higher read traffic, add service-level caching (`@Cacheable`) for common search pages.

Keep cache optional and disabled by default in Sprint 1.

***

## QA - Test Plan for US-04

### A) API Functional Tests (positive)

1. **US04-P1** List products without filters -> `200`, paged response.
2. **US04-P2** Search with `query=anc` returns name/description matches.
3. **US04-P3** Filter by `category=Electronics` returns only matching category.
4. **US04-P4** Price range filter returns items inside range.
5. **US04-P5** Sorting by `createdAt,DESC` returns latest first.

### B) Validation and Negative

6. **US04-N1** `size=0` -> `400`.
7. **US04-N2** `size=1000` -> `400`.
8. **US04-N3** `page=-1` -> `400`.
9. **US04-N4** `minPrice > maxPrice` -> `400`.
10. **US04-N5** Unsupported `sort` field -> `400`.

### C) Security

11. **US04-S1** Request without token -> `200` (public route).
12. **US04-S2** Request with valid token -> `200` (same behavior).

### D) Observability

13. **US04-O1** list/search counters increment.
14. **US04-O2** trace visible in Jaeger (gateway -> catalog-service).
15. **US04-O3** logs include `traceId`, filters, and result count.

### E) Performance

16. **US04-Perf1** p95 under 250ms for representative seeded dataset.

***

## Example RestAssured Tests (integration)

```java
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class ProductListSearchIT {
  static { RestAssured.baseURI = "http://localhost:8080"; }

  @Test
  void shouldListProductsWithDefaults200() {
    RestAssured.given()
      .get("/products")
      .then().statusCode(200)
      .body("content", notNullValue())
      .body("page.number", equalTo(0))
      .body("page.size", equalTo(20));
  }

  @Test
  void shouldFilterByCategoryAndPrice200() {
    RestAssured.given()
      .queryParam("category", "Electronics")
      .queryParam("minPrice", 5000)
      .queryParam("maxPrice", 20000)
      .queryParam("page", 0)
      .queryParam("size", 20)
      .queryParam("sort", "createdAt,DESC")
      .get("/products")
      .then().statusCode(200)
      .body("content", notNullValue());
  }

  @Test
  void shouldRejectInvalidRange400() {
    RestAssured.given()
      .queryParam("minPrice", 30000)
      .queryParam("maxPrice", 1000)
      .get("/products")
      .then().statusCode(400);
  }
}
```

***

## OpenAPI (drop into `docs/openapi/catalog-service.yaml`)

```yaml
paths:
  /products:
    get:
      summary: List and search products
      tags: [Catalog]
      parameters:
        - in: query
          name: query
          schema: { type: string }
          required: false
          description: Search text for name and description
        - in: query
          name: category
          schema: { type: string }
          required: false
        - in: query
          name: minPrice
          schema: { type: number, format: double, minimum: 0 }
          required: false
        - in: query
          name: maxPrice
          schema: { type: number, format: double, minimum: 0 }
          required: false
        - in: query
          name: page
          schema: { type: integer, minimum: 0, default: 0 }
          required: false
        - in: query
          name: size
          schema: { type: integer, minimum: 1, maximum: 100, default: 20 }
          required: false
        - in: query
          name: sort
          schema: { type: string, example: createdAt,DESC }
          required: false
      responses:
        '200':
          description: Paged product list
        '400':
          description: Invalid filter or pagination parameters
```

***

## Definition of Done (US-04)

* [ ] `GET /products` supports query, category, price range, pagination, and sort.
* [ ] Default behavior is `page=0`, `size=20`, `sort=createdAt,DESC`.
* [ ] Invalid query parameters return `400` with Problem Details style payload.
* [ ] Endpoint remains publicly accessible and stable via gateway route.
* [ ] Metrics, logs, and traces are visible for list/search path.
* [ ] Integration tests and Postman checks pass.
* [ ] OpenAPI contract updated.

***

## Implementation Checklist (short)

1. Extend `ProductController.list` with `sort` and validation.
2. Add repository filter support (Specifications or custom query).
3. Enforce default sort and whitelist fields.
4. Add/align Problem Details error handling for invalid params.
5. Add list/search counters and timer metrics.
6. Add structured logs including `traceId` and filter context.
7. Add/update integration tests for default, filtered, and invalid paths.
8. Update OpenAPI and Postman collection.
9. Commit: `feat(catalog): list and search products (US-04)`.

***

When you are ready, I can generate `US-5.md` next in the same style for Product Detail (`GET /products/{id}`).

