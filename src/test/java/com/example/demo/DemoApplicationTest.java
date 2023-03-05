package com.example.demo;

import com.example.demo.support.AbstractIntegrationTest;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class DemoApplicationTest extends AbstractIntegrationTest {

    @Test
    public void contextLoads() {
        //dummy test to see if Spring detects H2 db
    }

    @Test
    public void healthy() {
        given(requestSpecification)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .log().ifValidationFails(LogDetail.ALL);
    }
}
