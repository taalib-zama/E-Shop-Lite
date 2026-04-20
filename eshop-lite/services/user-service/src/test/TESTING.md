# User Registration Test Suite (US-1)

This test suite ensures the `user-service` handles registration according to the US-1 functional requirements and security guidelines.

## 1. Unit Tests (`RegistrationServiceTest.java`)
- **Focus:** Business logic in `RegistrationService`.
- **Mocking:** Uses Mockito to isolate dependencies (`UserRepository`, `PasswordEncoder`, `MeterRegistry`).
- **Key Checks:**
  - **Success:** Verifies user creation, password hashing, and "success" metric increment.
  - **Conflict:** Ensures a `ResponseStatusException` (401) is thrown for duplicate emails.

## 2. Integration Tests (`UserRegistrationIT.java`)
- **Focus:** End-to-end API flow from Controller to Database.
- **Technology:** 
  - **Testcontainers:** Spins up a real **PostgreSQL 16** instance for reliable, isolated DB testing.
  - **RestAssured:** Performs fluent HTTP assertions against the running random-port server.
- **Scenarios:**
  - **201 Created:** Validates full registration cycle and ensures no password leakage in the JSON response.
  - **409 Conflict:** Confirms duplicate email rejection via the database unique constraint.
  - **400 Bad Request:** Enforces the **Password Policy** (length + complexity) and email format validation.

## 3. Security & Observability Validations
- **Hashed Passwords:** All tests confirm that passwords are never stored in plaintext (using BCrypt) and never returned in API responses.
- **Traceability:** Integration tests verify the "Problem Details" structure, including `traceId` and standardized error `type` URLs.
- **Metrics:** Unit tests explicitly verify that Micrometer counters are incremented on both success and failure outcomes.
