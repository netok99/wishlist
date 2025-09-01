package com.wishlist.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MongoDBContainer;

@TestConfiguration
public class TestContainersConfig {
    @Bean
    @Primary
    public MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);
        container.start();
        return container;
    }
}
