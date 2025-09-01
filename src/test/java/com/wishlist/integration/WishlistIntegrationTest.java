package com.wishlist.integration;

import com.wishlist.WishlistApplication;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = WishlistApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("Wishlist API - Simple Integration Test")
class WishlistIntegrationTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "wishlist_simple_test");
    }

    @BeforeEach
    void setUp() {
        mongoTemplate.remove(new Query(), "wishlists");
    }

    @Test
    @DisplayName("Should handle basic CRUD operations via HTTP")
    void shouldHandleBasicCrudOperationsViaHttp() {
        final String customerId = "simple-test-customer";
        final String productId = "simple-test-product";
        final ResponseEntity<Map> emptyResponse = restTemplate.getForEntity(
            "/api/v1/customers/{customerId}/wishlist",
            Map.class,
            customerId
        );

        assertThat(emptyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(emptyResponse.getBody().get("totalItems")).isEqualTo(0);

        final ResponseEntity<Map> addResponse = restTemplate.postForEntity(
            "/api/v1/customers/{customerId}/wishlist/products/{productId}",
            null,
            Map.class,
            customerId,
            productId
        );

        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(addResponse.getBody().get("productId")).isEqualTo(productId);

        final ResponseEntity<Map> withProductResponse = restTemplate.getForEntity(
            "/api/v1/customers/{customerId}/wishlist",
            Map.class,
            customerId
        );

        assertThat(withProductResponse.getBody().get("totalItems")).isEqualTo(1);

        restTemplate.delete(
            "/api/v1/customers/{customerId}/wishlist/products/{productId}",
            customerId,
            productId
        );

        final ResponseEntity<Map> afterDeleteResponse = restTemplate.getForEntity(
            "/api/v1/customers/{customerId}/wishlist",
            Map.class,
            customerId
        );
        assertThat(afterDeleteResponse.getBody().get("totalItems")).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle business rules via HTTP")
    void shouldHandleBusinessRulesViaHttp() {
        final String customerId = "business-rule-customer";

        restTemplate.postForEntity(
            "/api/v1/customers/{customerId}/wishlist/products/{productId}",
            null,
            Map.class,
            customerId,
            "duplicate-product"
        );

        final ResponseEntity<Map> duplicateResponse = restTemplate.postForEntity(
            "/api/v1/customers/{customerId}/wishlist/products/{productId}",
            null,
            Map.class,
            customerId,
            "duplicate-product"
        );

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
