package com.marketplace.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Marketplace Tecnológico API")
                        .version("1.0.0")
                        .description("API for managing a technological marketplace with Hexagonal Architecture.")
                        .contact(new Contact()
                                .name("Backend Developer")
                                .email("dev@marketplace.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server")
                ));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("marketplace-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
