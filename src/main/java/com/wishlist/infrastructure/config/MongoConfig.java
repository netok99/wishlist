package com.wishlist.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.context.annotation.Bean;
import com.mongodb.client.MongoClient;
import org.springframework.data.mongodb.core.convert.MongoConverter;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    @Override
    protected String getDatabaseName() {
        return "wishlist_db";
    }

    @Bean
    public MongoTemplate mongoTemplate(
        MongoClient mongoClient,
        MongoConverter mongoConverter
    ) {
        return new MongoTemplate(mongoClient, getDatabaseName());
    }
}
