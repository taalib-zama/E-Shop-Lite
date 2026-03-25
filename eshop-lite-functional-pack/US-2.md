Great, Taalib - here is the full User Story 2 pack in the same implementation-ready format as US-1.

***

## FRD - User Story 2: Login and JWT

**ID:** US-02\
**Service:** `user-service` (port 8081, name `USER-SERVICE`)\
**Gateway route:** `POST /auth/login` -> `lb://USER-SERVICE/auth/login`\
**Related protected route:** `GET /users/me` -> `lb://USER-SERVICE/users/me`\
**Status:** Ready for implementation

### 1) User Story

> As a registered user, I want to log in with email and password so that I can receive a JWT and access protected endpoints.

### 2) Business Rules

* **BR-01 Credential validation:** Login must validate email/password against stored user credentials.
* **BR-02 Account state:** Only `ACTIVE` users can authenticate.
* **BR-03 Token contract:** JWT must include `sub`, `role`, `iat`, `exp` with 15-minute expiry.
* **BR-04 Safe error behavior:** Invalid credentials and unknown users return the same `401` response to avoid user enumeration.
* **BR-05 Security hygiene:** Never log plaintext passwords or JWT secrets.
* **BR-06 Auditable:** Emit structured audit logs for login success/failure with `traceId`.

### 3) Functional Requirements

#### 3.1 Endpoint - Login

`POST /auth/login` (public)

**Request (JSON)**

```json
{
  "email": "taalib@example.com",
  "password": "S3cureP@ss!"
}
```

**Validation**

* `email`: required, valid email
* `password`: required, non-blank

**Processing**

1. Validate request DTO.
2. Find user by email.
3. Verify password via BCrypt match against `password_hash`.
4. Validate `status == ACTIVE`.
5. Generate signed JWT (HMAC) with 15-minute expiry.
6. Return token response.
7. Emit audit log event `USER_LOGIN_SUCCEEDED` or `USER_LOGIN_FAILED`.

**Success (200 OK)**

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Errors (Problem Details style)**

* 400 Validation error
* 401 Invalid credentials or inactive account
* 500 Unexpected

Example 401:

```json
{
  "type": "https://eshop-lite/errors/auth",
  "title": "Invalid credentials",
  "status": 401,
  "detail": "Authentication failed",
  "traceId": "a1b2c3",
  "errors": []
}
```

#### 3.2 Endpoint - Current User

`GET /users/me` (protected)

**Headers**

* `Authorization: Bearer <jwt>`

**Success (200 OK)**

```json
{
  "id": "764e4d23-8a83-4b4e-9ad1-0538a7b2e6e5",
  "name": "Taalib Z",
  "email": "taalib@example.com",
  "role": "USER"
}
```

**Errors**

* 401 Missing/invalid/expired token

#### 3.3 Data Model Impact

No new table required for MVP login flow.

Uses existing `users` table fields:

* `email`
* `password_hash`
* `role`
* `status`

Optional later enhancement:

* Add `last_login_at` and `failed_login_count` for lockout policy.

#### 3.4 Security

* BCrypt password verification only; do not compare plaintext.
* JWT secret must come from env/config (`JWT_SECRET`) and be strong.
* JWT validated in gateway and service (defense in depth).
* `POST /auth/login` remains public; `/users/me` requires authentication.
* Keep response and logs free of password/token secrets.

#### 3.5 Observability

* **Metrics:**
  * `user_login_requests_total{outcome="success|failure"}`
  * `user_login_duration_seconds` (timer)
  * `user_login_invalid_credentials_total`
* **Logs:** structured JSON with `traceId`, `event`, `emailHash` (not raw email if possible), `outcome`.
* **Tracing:** spans visible in Jaeger for gateway -> user-service login call.

#### 3.6 Non-functional

* Login p95 latency `< 250ms` (excluding cold start).
* Error rate `< 1%` excluding invalid user input.
* Token issuance must be deterministic and clock-skew tolerant (+/- 30s in validation path where applicable).

***

## Tech Lead Guidance - How to Implement in the Skeleton

You already have core login in `services/user-service`. Implement these deltas to align with FRD and sprint architecture.

### A) Harden `LoginRequest` validation

Current DTO already has `@NotBlank` + `@Email` for `email` and `@NotBlank` for `password`.

Recommended refinement:

```java
// src/main/java/com/eshoplite/user/api/LoginRequest.java
public record LoginRequest(
  @NotBlank @Email String email,
  @NotBlank @Size(min = 8, max = 64) String password
) {}
```

### B) Enforce ACTIVE status in login flow

In `UserController.login`, after password verification, reject non-active users:

```java
if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) {
  throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
}
```

Keep same generic message to avoid account enumeration.

### C) Harden JWT generation details

`JwtService` already emits `sub`, `role`, `iat`, and `exp` using configured `expMinutes`.

Recommended additions:

* Validate secret length at startup (>= 32 bytes for HMAC key quality).
* Add issuer claim (for example `eshop-lite-user-service`).
* Keep expiration at 15 minutes per FRD.

### D) Normalize Problem Details payloads

`GlobalExceptionHandler` is already present; extend shape to include:

* `detail`
* `traceId`
* consistent `type` URLs (`validation`, `auth`, `internal`)

This keeps `400/401/500` contracts stable across services and gateway.

### E) Add login metrics/timer

Instrument login success/failure counters and duration timer in controller/service:

* success: increment `user_login_requests_total{outcome="success"}`
* failure: increment `user_login_requests_total{outcome="failure"}`
* timer: `user_login_duration_seconds`

### F) Gateway and route alignment

Existing gateway route already covers `/auth/**` -> `lb://USER-SERVICE`.

Confirm security behavior:

* permit `POST /auth/login`
* protect `/users/me`
* preserve `traceparent` and request correlation headers

### G) Future-ready S2S note (Sprint 2)

When Feign clients are introduced (order/inventory), propagate:

* `Authorization`
* `X-Request-Id`

This matches architecture addendum and keeps end-to-end traceability.

***

## QA - Test Plan for US-02

### A) API Functional Tests (positive)

1. **US02-P1** Login with valid credentials -> `200`
   * Verify `accessToken`, `tokenType=Bearer`, `expiresIn=900`.
2. **US02-P2** Use returned token on `/users/me` -> `200`.
3. **US02-P3** ADMIN login returns token with role claim `ADMIN`.

### B) Validation and Negative

4. **US02-N1** Invalid email format -> `400`.
5. **US02-N2** Missing password -> `400`.
6. **US02-N3** Wrong password -> `401`.
7. **US02-N4** Unknown email -> `401` (same generic message).
8. **US02-N5** Inactive user login attempt -> `401`.

### C) Security

9. **US02-S1** JWT signature validation fails for tampered token -> `401` on protected endpoint.
10. **US02-S2** Expired token rejected -> `401`.
11. **US02-S3** No plaintext password in responses/logs.

### D) Observability

12. **US02-O1** `user_login_requests_total` increments on success/failure.
13. **US02-O2** Login traces visible in Jaeger (gateway -> user-service).
14. **US02-O3** Logs contain `traceId` and login event name.

### E) Performance

15. **US02-Perf1** Median < 150ms and p95 < 250ms for local single-user login.

***

## Example RestAssured Tests (integration)

```java
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class UserLoginIT {
  static { RestAssured.baseURI = "http://localhost:8080"; }

  @Test
  void shouldLogin200AndReturnJwt() {
    var email = "taalib.login@example.com";

    RestAssured.given().contentType("application/json")
      .body("{\"name\":\"Taalib\",\"email\":\"" + email + "\",\"password\":\"S3cureP@ss!\"}")
      .post("/users")
      .then().statusCode(anyOf(is(201), is(409)));

    RestAssured.given().contentType("application/json")
      .body("{\"email\":\"" + email + "\",\"password\":\"S3cureP@ss!\"}")
      .when().post("/auth/login")
      .then().statusCode(200)
      .body("tokenType", equalTo("Bearer"))
      .body("accessToken", not(blankOrNullString()))
      .body("expiresIn", equalTo(900));
  }

  @Test
  void shouldRejectWrongPassword401() {
    RestAssured.given().contentType("application/json")
      .body("{\"email\":\"taalib@example.com\",\"password\":\"WrongPass1!\"}")
      .post("/auth/login")
      .then().statusCode(401);
  }

  @Test
  void shouldAccessMeWithToken() {
    var email = "taalib.me@example.com";

    RestAssured.given().contentType("application/json")
      .body("{\"name\":\"Taalib\",\"email\":\"" + email + "\",\"password\":\"S3cureP@ss!\"}")
      .post("/users")
      .then().statusCode(anyOf(is(201), is(409)));

    var token = RestAssured.given().contentType("application/json")
      .body("{\"email\":\"" + email + "\",\"password\":\"S3cureP@ss!\"}")
      .post("/auth/login")
      .then().statusCode(200)
      .extract().path("accessToken");

    RestAssured.given()
      .header("Authorization", "Bearer " + token)
      .get("/users/me")
      .then().statusCode(200)
      .body("email", equalTo(email));
  }
}
```

***

## OpenAPI (drop into `docs/openapi/user-service.yaml`)

```yaml
paths:
  /auth/login:
    post:
      summary: Login
      tags: [Auth]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [email, password]
              properties:
                email: { type: string, format: email }
                password: { type: string, minLength: 8, maxLength: 64 }
      responses:
        '200':
          description: JWT issued
          content:
            application/json:
              schema:
                type: object
                properties:
                  accessToken: { type: string }
                  tokenType: { type: string, example: Bearer }
                  expiresIn: { type: integer, example: 900 }
        '400': { description: Validation error }
        '401': { description: Invalid credentials }

  /users/me:
    get:
      summary: Get current user profile
      tags: [User]
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Current authenticated user
        '401':
          description: Unauthorized
```

***

## Definition of Done (US-02)

* [ ] Valid login returns JWT with 15-minute expiry.
* [ ] Invalid credentials always return `401` generic auth error.
* [ ] `/users/me` protected and works with valid bearer token.
* [ ] Problem Details responses consistent for `400/401/500`.
* [ ] Login metrics and traces visible.
* [ ] Integration tests and Postman checks pass.
* [ ] OpenAPI updated.

***

## Implementation Checklist (short)

1. Tighten `LoginRequest` validation.
2. Enforce `ACTIVE` status check in login.
3. Normalize auth error payload shape in `GlobalExceptionHandler`.
4. Add login metrics counters/timer.
5. Verify gateway and service security rules for `/auth/login` and `/users/me`.
6. Add/update integration tests for login + me + invalid paths.
7. Update OpenAPI contract and Postman collection.
8. Commit: `feat(user): login and jwt (US-02)`.

***

When you are ready, I can also generate:

* login rate-limit policy for gateway,
* refresh-token strategy (secure rotation),
* Testcontainers-based integration suite for auth flows.

