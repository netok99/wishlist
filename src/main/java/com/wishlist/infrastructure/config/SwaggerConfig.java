package com.wishlist.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;

public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Wishlist Service API")
                    .description("Microservice for managing customer wishlists in e-commerce platform")
                    .version("1.0.0")
                    .contact(
                        new Contact()
                            .name("E-commerce Team")
                            .email("team@ecommerce.com")
                    )
            )
            .servers(
                List.of(
                    new Server()
                        .url("http://localhost:8080")
                        .description("Development server"),
                    new Server()
                        .url("https://api.ecommerce.com")
                        .description("Production server")
                )
            );
    }
}
