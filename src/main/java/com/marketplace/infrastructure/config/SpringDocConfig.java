package com.marketplace.infrastructure.config;

import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration to handle problematic types in OpenAPI/Swagger
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // Remove problematic schemas that cause errors
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                @SuppressWarnings("rawtypes")
                Map<String, Schema> schemas = openApi.getComponents().getSchemas();

                // Remove CompletableFuture schemas
                schemas.entrySet().removeIf(entry ->
                        entry.getKey().contains("CompletableFuture") ||
                                entry.getKey().contains("Future")
                );

                // Simplify complex array types
                schemas.entrySet().removeIf(entry ->
                        entry.getKey().contains("double[]") ||
                                entry.getKey().contains("DoubleArray")
                );
            }
        };
    }
}

