Awesome, Taalib — let’s start **User Story 1 (Register User)** the right way. Below is a **complete, professional pack** you can drop into `/docs` and implement directly in your existing skeleton.

***

## 📄 FRD — User Story 1: Register User

**ID:** US‑01\
**Service:** `user-service` (port 8081, name `USER-SERVICE`)\
**Gateway route:** `POST /users` → `lb://USER-SERVICE/users`\
**Status:** Ready for implementation

### 1) User Story

> As a new user, I want to register with name, email, and password so that I can sign in and access protected resources.

### 2) Business Rules

*   **BR‑01 Unique email:** Registration must reject an email that already exists.
*   **BR‑02 Secure password storage:** Store **BCrypt hash** only; never store/log plaintext.
*   **BR‑03 Default role:** New accounts get `USER` role.
*   **BR‑04 Auditable:** Persist `created_at`, `updated_at`; log an audit event (structured logs).

### 3) Functional Requirements

#### 3.1 Endpoint

`POST /users` (public)

**Request (JSON)**

```json
{
  "name": "Taalib Z",
  "email": "taalib@example.com",
  "password": "S3cureP@ss!"
}
```

**Validation**

*   `name`: required, 2–100 chars
*   `email`: required, valid email, **unique**
*   `password` (policy): required, 8–64 chars, must include uppercase, lowercase, number, and symbol (configurable)

**Processing**

1.  Validate DTO (Bean Validation + password policy).
2.  Check duplicate email → if exists, **409 Conflict**.
3.  Hash password via BCrypt (strength 10–12).
4.  Set role = `USER`, status = `ACTIVE`.
5.  Save user.
6.  Emit audit log `USER_REGISTERED` with `traceId`.

**Success (201 Created)**

```json
{
  "id": "764e4d23-8a83-4b4e-9ad1-0538a7b2e6e5",
  "name": "Taalib Z",
  "email": "taalib@example.com",
  "role": "USER",
  "createdAt": "2026-02-17T13:00:00Z"
}
```

> **Never** include password in responses/logs.

**Errors (Problem Details style)**

*   400 Validation error — include per-field messages
*   409 Duplicate email
*   500 Unexpected

Example 400:

```json
{
  "type": "https://eshop-lite/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "traceId": "a1b2c3",
  "errors": [
    {"field":"password","message":"must include uppercase, lowercase, number and symbol"}
  ]
}
```

#### 3.2 Data Model (user-service)

*   `users`
    *   `id UUID PK`
    *   `name VARCHAR(100) NOT NULL`
    *   `email VARCHAR(255) NOT NULL UNIQUE`
    *   `password_hash VARCHAR(255) NOT NULL`
    *   `role VARCHAR(20) NOT NULL DEFAULT 'USER'`
    *   `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'`
    *   `created_at TIMESTAMP NOT NULL`
    *   `updated_at TIMESTAMP NOT NULL`

> Skeleton already includes `V1__init.sql` with unique constraint & index.

#### 3.3 Security

*   Public endpoint; only rate‑limit via gateway (dev) if needed.
*   Passwords always hashed; logs must not contain secrets.

#### 3.4 Observability

*   **Metrics:**
    *   `user_registration_requests_total`
    *   `user_registration_failures_total`
    *   `user_registration_duration_seconds` (timer)
*   **Logs:** structured JSON with `traceId`, `event=USER_REGISTERED`, `userId`.
*   **Tracing:** Spans visible in Jaeger for the request.

#### 3.5 Non‑functional

*   p95 latency `< 500ms` including hashing (BCrypt cost \~10).
*   Error rate < 1% (excluding invalid input).

***

## 🔧 Tech Lead Guidance — How to Implement in the Skeleton

You already have the skeleton in `services/user-service`. Implement these deltas:

### A) Add a Password Policy validator

Create a reusable validator for the password complexity.

```java
// src/main/java/com/eshoplite/user/validation/PasswordPolicy.java
package com.eshoplite.user.validation;
import jakarta.validation.*;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordPolicyValidator.class)
public @interface PasswordPolicy {
  String message() default "must include uppercase, lowercase, number and symbol";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}

// src/main/java/com/eshoplite/user/validation/PasswordPolicyValidator.java
package com.eshoplite.user.validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {
  @Override public boolean isValid(String p, ConstraintValidatorContext c) {
    if (p == null || p.length() < 8 || p.length() > 64) return false;
    boolean up=false, low=false, dig=false, sym=false;
    for (char ch : p.toCharArray()) {
      if (Character.isUpperCase(ch)) up = true;
      else if (Character.isLowerCase(ch)) low = true;
      else if (Character.isDigit(ch)) dig = true;
      else sym = true;
    }
    return up && low && dig && sym;
  }
}
```

Use it in your DTO:

```java
// RegisterRequest.java
public record RegisterRequest(
  @NotBlank @Size(min=2,max=100) String name,
  @NotBlank @Email String email,
  @NotBlank @PasswordPolicy String password
) {}
```

### B) Add Problem Details controller advice

So 400/409/500 responses are consistent.

```java
// src/main/java/com/eshoplite/user/api/GlobalExceptionHandler.java
package com.eshoplite.user.api;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  record Problem(String type, String title, int status, String detail, String traceId, List<Map<String,String>> errors) {}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException ex) {
    var errs = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
        .toList();
    var pb = new Problem("https://eshop-lite/errors/validation", "Validation failed",
        400, "Input validation failed", traceId(), errs);
    return ResponseEntity.badRequest().body(pb);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Problem> handleRSE(ResponseStatusException ex) {
    var pb = new Problem(mapType(ex.getStatusCode()), ex.getReason(), ex.getStatusCode().value(),
        Optional.ofNullable(ex.getReason()).orElse(""),
        traceId(), List.of());
    return ResponseEntity.status(ex.getStatusCode()).body(pb);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleGeneric(Exception ex) {
    var pb = new Problem("https://eshop-lite/errors/internal", "Internal error", 500,
        "Unexpected error", traceId(), List.of());
    return ResponseEntity.status(500).body(pb);
  }

  private String traceId() {
    // If using MDC, return MDC.get("traceId"); else generate UUID for now.
    return UUID.randomUUID().toString();
  }
  private String mapType(HttpStatusCode sc) {
    if (sc.value()==409) return "https://eshop-lite/errors/conflict";
    if (sc.value()==400) return "https://eshop-lite/errors/validation";
    return "https://eshop-lite/errors";
  }
}
```

### C) Enhance `RegistrationService` with metrics/timer (optional now)

Add Micrometer counters/timers if you want:

```java
// src/main/java/com/eshoplite/user/service/RegistrationService.java
// ... inside class
private final io.micrometer.core.instrument.MeterRegistry meterRegistry;
public RegistrationService(UserRepository repo, MeterRegistry mr){
  this.repo = repo; this.meterRegistry = mr;
}
public User register(RegisterRequest r){
  var sample = io.micrometer.core.instrument.Timer.start(meterRegistry);
  try {
    if (repo.existsByEmail(r.email()))
      throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    User u = new User();
    u.setName(r.name());
    u.setEmail(r.email());
    u.setPasswordHash(encoder.encode(r.password()));
    u.setRole("USER");
    var saved = repo.save(u);
    meterRegistry.counter("user_registration_requests_total", "outcome", "success").increment();
    return saved;
  } catch (RuntimeException e) {
    meterRegistry.counter("user_registration_requests_total", "outcome", "failure").increment();
    throw e;
  } finally {
    sample.stop(io.micrometer.core.instrument.Timer
      .builder("user_registration_duration_seconds").register(meterRegistry));
  }
}
```

### D) Gateway (optional dev guard)

If you want basic anti‑abuse for public `POST /users`:

*   Add a **rate limiter** filter (Redis or in‑memory) later. For now, you can keep it open in dev.

### E) DB — already set

`V1__init.sql` is present with unique email. ✅

***

## 🧪 QA — Test Plan for US‑01

### A) API Functional Tests (positive)

1.  **US01‑P1** Register valid user → `201`
    *   Verify response body fields (no password).
    *   Verify DB row exists; role `USER`.
2.  **US01‑P2** Register without optional fields (none are optional now) → N/A
3.  **US01‑P3** Re‑register new unique email with same name → `201`

### B) Validation & Negative

4.  **US01‑N1** Invalid email format → `400` (field error on `email`)
5.  **US01‑N2** Weak password (missing symbol/upper/lower/digit) → `400` (field error on `password`)
6.  **US01‑N3** Short name `< 2` → `400`
7.  **US01‑N4** Missing required fields → `400` with per‑field errors
8.  **US01‑N5** Duplicate email → `409`

### C) Security

9.  **US01‑S1** Ensure plaintext password never appears in logs/responses
10. **US01‑S2** Confirm hashed password stored (BCrypt prefix `$2`)

### D) Observability

11. **US01‑O1** Metrics present:
    *   `user_registration_requests_total{outcome="success"}` increments on success
    *   `user_registration_requests_total{outcome="failure"}` increments on failure
12. **US01‑O2** Trace visible in Jaeger for request

### E) Performance

13. **US01‑Perf1** Median < 300ms; p95 < 500ms for 1–2 concurrent requests (local).

***

## 🔬 Example RestAssured Test (integration)

```java
// src/test/java/.../UserRegistrationIT.java
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;

class UserRegistrationIT {
  static { RestAssured.baseURI = "http://localhost:8080"; }

  @Test
  void shouldRegister201() {
    RestAssured.given().contentType("application/json")
      .body("""
      {"name":"Taalib","email":"taalib+1@example.com","password":"S3cureP@ss!"}
      """)
      .when().post("/users")
      .then().statusCode(201)
      .body("email", equalTo("taalib+1@example.com"))
      .body("$", not(hasKey("password")))
      .body("role", equalTo("USER"));
  }

  @Test
  void shouldRejectDuplicate409() {
    var email = "taalib+dup@example.com";
    RestAssured.given().contentType("application/json")
      .body("{\"name\":\"A\",\"email\":\""+email+"\",\"password\":\"S3cureP@ss!\"}")
      .post("/users").then().statusCode(201);

    RestAssured.given().contentType("application/json")
      .body("{\"name\":\"A\",\"email\":\""+email+"\",\"password\":\"S3cureP@ss!\"}")
      .post("/users").then().statusCode(409);
  }

  @Test
  void shouldRejectWeakPassword400() {
    RestAssured.given().contentType("application/json")
      .body("{\"name\":\"A\",\"email\":\"weak@example.com\",\"password\":\"password\"}")
      .post("/users").then().statusCode(400)
      .body("errors.find { it.field == 'password' }.message", containsString("uppercase"));
  }
}
```

> For DB assertions, add a repository bean in test and verify persisted data; or query Postgres when using Testcontainers.

***

## 🧾 OpenAPI (drop into `docs/openapi/user-service.yaml`)

```yaml
openapi: 3.0.3
info:
  title: User Service API
  version: 1.0.0
paths:
  /users:
    post:
      summary: Register User
      tags: [User]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [name, email, password]
              properties:
                name: { type: string, minLength: 2, maxLength: 100 }
                email: { type: string, format: email }
                password: { type: string, minLength: 8, maxLength: 64 }
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                type: object
                properties:
                  id: { type: string, format: uuid }
                  name: { type: string }
                  email: { type: string, format: email }
                  role: { type: string, example: USER }
                  createdAt: { type: string, format: date-time }
        '400': { description: Validation error }
        '409': { description: Email already exists }
```

***

## 📈 System Sequence (Mermaid)



***

## ✅ Definition of Done (US‑01)

*   [ ] All ACs pass (including duplicates & validation).
*   [ ] Password hashed (BCrypt) and never logged.
*   [ ] Problem Details responses for 400/409.
*   [ ] Metrics counters increment.
*   [ ] Trace visible in Jaeger.
*   [ ] Postman & integration tests green.
*   [ ] OpenAPI updated.

***

## 🚦 Implementation Checklist (short)

1.  **Add PasswordPolicy** validator.
2.  **Add GlobalExceptionHandler** for Problem Details.
3.  Wire **metrics** counters/timer (optional but good).
4.  Confirm Flyway `V1` (unique email).
5.  Manual smoke via Postman (Register → Login → Me).
6.  Write RestAssured integration tests.
7.  Update `docs/openapi/user-service.yaml`.
8.  Commit: `feat(user): register user (US-01)`.

***

When you’re ready, I can:

*   Wire **BCrypt password verification** for login,
*   Add an **admin seed** (Flyway) so `POST /products` can be tested immediately,
*   Generate **integration tests** using **Testcontainers** for Postgres.

What would you like next: **admin seed**, **login hardening**, or **Testcontainers tests**?
