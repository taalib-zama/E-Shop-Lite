package com.eshoplite.qa.generated;

import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class SampleConnectivityTest {
    @Test
    void healthEndpointShouldBeUp() {
        String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
        when().get(baseUrl + "/actuator/health").then().statusCode(anyOf(is(200), is(401), is(403))); // allow secured
    }
}
