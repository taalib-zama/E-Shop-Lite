package com.eshoplite.user;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserRegistrationIT {

  @LocalServerPort
  private Integer port;

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("users_db")
      .withUsername("eshop")
      .withPassword("eshop");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.url", postgres::getJdbcUrl);
    registry.add("spring.flyway.user", postgres::getUsername);
    registry.add("spring.flyway.password", postgres::getPassword);
  }

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void shouldRegisterSuccessfully_201() {
    String email = "test-" + System.currentTimeMillis() + "@example.com";
    String requestBody = """
        {
          "name": "Taalib Z",
          "email": "%s",
          "password": "S3cureP@ss!"
        }
        """.formatted(email);

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/users")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo("Taalib Z"))
        .body("email", equalTo(email))
        .body("role", equalTo("USER"))
        .body("$", not(hasKey("password")));
  }

  @Test
  void shouldRejectDuplicateEmail_409() {
    String email = "duplicate@example.com";
    String requestBody = """
        {
          "name": "User A",
          "email": "%s",
          "password": "S3cureP@ss!"
        }
        """.formatted(email);

    // First registration
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .post("/users")
        .then()
        .statusCode(201);

    // Second registration with same email
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .post("/users")
        .then()
        .statusCode(409)
        .body("title", equalTo("Email already registered"))
        .body("type", equalTo("https://eshop-lite/errors/conflict"));
  }

  @Test
  void shouldRejectWeakPassword_400() {
    String requestBody = """
        {
          "name": "Taalib Z",
          "email": "weak@example.com",
          "password": "password"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .post("/users")
        .then()
        .statusCode(400)
        .body("title", equalTo("Validation failed"))
        .body("errors", hasItem(allOf(
            hasEntry("field", "password"),
            hasEntry("message", "must include uppercase, lowercase, number and symbol")
        )));
  }

  @Test
  void shouldRejectInvalidEmail_400() {
    String requestBody = """
        {
          "name": "Taalib Z",
          "email": "not-an-email",
          "password": "S3cureP@ss!"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .post("/users")
        .then()
        .statusCode(400)
        .body("errors.find { it.field == 'email' }.message", notNullValue());
  }
}
