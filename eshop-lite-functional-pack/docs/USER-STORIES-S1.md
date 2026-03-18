# User Stories — Sprint 1 (Identity & Catalog)

**Project:** E‑Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-03

---

## Epic Alignment
- EP‑01 Platform Foundations (Gateway, Config, Discovery)
- EP‑02 Identity & Access
- EP‑03 Catalog

## Definitions
- **DoR**: API draft, validations defined, dependencies identified, test data available, estimated.  
- **DoD**: Code + tests; OpenAPI updated; Actuator exposed; structured logs; traces; CI green; Postman smoke green; README updated.

---

## US‑01 Register User (5 pts)
**As a** Visitor **I want** to create an account **so that** I can log in.

**Acceptance Criteria (Gherkin)**
```gherkin
Given I provide a valid name, unique email, and strong password
When I POST /users
Then I get 201 with id & email (no password)
And the user is persisted with role USER and status ACTIVE
And duplicate email returns 409
```

**Tasks**
- Controller + Request/Response DTOs with Bean Validation
- Service: hash password (BCrypt), persist user
- Repo: unique email check
- Mapping + exception handling (Problem schema)
- Tests: unit + integration (Testcontainers Postgres)

---

## US‑02 Login & JWT (5 pts)
**As a** User **I want** to log in **so that** I can access protected APIs.

**Acceptance Criteria**
```gherkin
Given valid credentials
When I POST /auth/login
Then I receive a Bearer JWT with exp 15 minutes
And I can call secured endpoints with the token
And invalid credentials return 401
```

**Tasks**
- Auth endpoint, JWT utility
- Security config (password encoder, auth manager)
- Gateway pass‑through + validation filter (later finalized)
- Tests: unit + integration

---

## US‑03 Create Product (ADMIN) (5 pts)
**As an** Admin **I want** to create products **so that** they are available to users.

**Acceptance Criteria**
```gherkin
Given I am authenticated as ADMIN
When I POST /products with valid name, sku, price
Then I receive 201 with product details
And SKU uniqueness is enforced
And USER role receives 403
```

**Tasks**
- Product entity + repository (unique SKU)
- Controller + DTOs + validation
- Method security (ROLE_ADMIN)
- Tests: unit + integration

---

## US‑04 List/Search Products (3 pts)
**As a** User/Visitor **I want** to browse/search products **so that** I can explore inventory.

**Acceptance Criteria**
```gherkin
Given products exist
When I GET /products with filters and pagination
Then I receive paginated results
And default sort is createdAt desc
```

**Tasks**
- Query params & pagination
- ILIKE search by name/description
- Optional caching (later)
- Tests

---

## US‑05 Get Product by ID (2 pts)
**As a** User/Visitor **I want** product detail **so that** I can view info.

**Acceptance Criteria**
```gherkin
When I GET /products/{id}
Then I receive 200 with product or 404 if not found
```

**Tasks**
- Controller + repo findById
- Error mapping to standard schema
- Tests

---

## Sprint 1 Definition of Done (recap)
- Unit coverage ≥ 70% on core logic
- Integration tests for critical paths
- OpenAPI specs updated & committed
- Actuator metrics exposed; Prometheus scrape verified
- Logs include `traceId`; Jaeger traces visible
- Postman smoke suite passes
