package com.example.demo.support;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:tc:postgresql:14-alpine://testcontainers/workshop"
})
public class AbstractIntegrationTest {

}
