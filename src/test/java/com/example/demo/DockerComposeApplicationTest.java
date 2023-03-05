package com.example.demo;

import com.example.demo.model.Rating;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

@Testcontainers
public class DockerComposeApplicationTest  {

    static Network network = Network.newNetwork();

    @Container
    static final GenericContainer redis = new GenericContainer("redis:6-alpine")
            .withExposedPorts(6379)
            .withNetwork(network)
            .withNetworkAliases("redis");

    @Container
    static final KafkaContainer kafka = new KafkaContainer (
            DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
            .withNetwork(network)
            .withNetworkAliases("kafka");

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/talks-schema.sql"), "/docker-entrypoint-initdb.d/")
            .withNetwork(network)
            .withNetworkAliases("db");

    @Container
    static DockerComposeContainer<?> composeContainer = new DockerComposeContainer<>(new File("docker-compose.yml"))
            .withLocalCompose(true)
            .withExposedService("app_1", 8080)
            .waitingFor("app_1", Wait.forHttp("/actuator/health"));

    @Container
    static final GenericContainer appContainer = new GenericContainer<>(
            new ImageFromDockerfile()
                    .withFileFromPath("Dockerfile", Paths.get("Dockerfile"))
                    .withFileFromPath("build/libs/test-containers-szjug-workshop.jar", Paths.get("build/libs/test-containers-szjug-workshop.jar"))
    )
            .withExposedPorts(8080)
            .withEnv("SPRING_REDIS_HOST", "redis")
            .withEnv("SPRING_REDIS_PORT", "6379")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "BROKER://kafka:9092")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:5432/test")
            .withEnv("SPRING_DATASOURCE_USERNAME", "test")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "test")
            .withNetwork(network)
            .waitingFor(Wait.forHttp("/actuator/health"))
            .dependsOn(redis, kafka, postgres);

    protected RequestSpecification requestSpecification;

    @BeforeEach
    public void setUpAbstractIntegrationTest() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri(String.format("http://%s:%d", appContainer.getHost(), appContainer.getFirstMappedPort()))
                .addHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
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

    @Test
    public void testRatings() {
        String talkId = "testcontainers-integration-testing";

        given(requestSpecification)
                .body(new Rating(talkId, 5))
                .when()
                .post("/ratings")
                .then()
                .statusCode(202);

        await().untilAsserted(() -> {
            given(requestSpecification)
                    .queryParam("talkId", talkId)
                    .when()
                    .get("/ratings")
                    .then()
                    .body("5", is(1));
        });

        for (int i = 1; i <= 5; i++) {
            given(requestSpecification)
                    .body(new Rating(talkId, i))
                    .when()
                    .post("/ratings");
        }

        await().untilAsserted(() -> {
            given(requestSpecification)
                    .queryParam("talkId", talkId)
                    .when()
                    .get("/ratings")
                    .then()
                    .body("1", is(1))
                    .body("2", is(1))
                    .body("3", is(1))
                    .body("4", is(1))
                    .body("5", is(2));
        });
    }

}