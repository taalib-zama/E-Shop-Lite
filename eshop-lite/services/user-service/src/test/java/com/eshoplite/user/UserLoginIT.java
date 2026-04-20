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
class UserLoginIT {

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
  void shouldLoginSuccessfully_200() {
    String email = "login-test@example.com";
    String registerBody = """
        {
          "name": "Taalib Z",
          "email": "%s",
          "password": "S3cureP@ss!"
        }
        """.formatted(email);

    // Register first
    given()
        .contentType(ContentType.JSON)
        .body(registerBody)
        .post("/users")
        .then()
        .statusCode(201);

    // Login
    String loginBody = """
        {
          "email": "%s",
          "password": "S3cureP@ss!"
        }
        """.formatted(email);

    String token = given()
        .contentType(ContentType.JSON)
        .body(loginBody)
        .when()
        .post("/auth/login")
        .then()
        .statusCode(200)
        .body("accessToken", notNullValue())
        .body("tokenType", equalTo("Bearer"))
        .body("expiresIn", equalTo(900))
        .extract().path("accessToken");

    // Access /users/me
    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/users/me")
        .then()
        .statusCode(200)
        .body("email", equalTo(email))
        .body("name", equalTo("Taalib Z"));
  }

  @Test
  void shouldRejectWrongPassword_401() {
    String email = "wrong-pass@example.com";
    String registerBody = """
        {
          "name": "User",
          "email": "%s",
          "password": "S3cureP@ss!"
        }
        """.formatted(email);

    given()
        .contentType(ContentType.JSON)
        .body(registerBody)
        .post("/users")
        .then()
        .statusCode(201);

    String loginBody = """
        {
          "email": "%s",
          "password": "WrongPassword1!"
        }
        """.formatted(email);

    given()
        .contentType(ContentType.JSON)
        .body(loginBody)
        .post("/auth/login")
        .then()
        .statusCode(401)
        .body("title", equalTo("Invalid credentials"))
        .body("type", equalTo("https://eshop-lite/errors/auth"));
  }

  @Test
  void shouldRejectUnregisteredUser_401() {
    String loginBody = """
        {
          "email": "not-found@example.com",
          "password": "S3cureP@ss!"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(loginBody)
        .post("/auth/login")
        .then()
        .statusCode(401)
        .body("title", equalTo("Invalid credentials"));
  }
}
