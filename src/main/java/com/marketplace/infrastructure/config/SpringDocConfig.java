package com.marketplace.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration for OpenAPI/Swagger:
 * - Cleans problematic schemas
 * - Declares global JWT bearer security scheme
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SpringDocConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // Set global security requirement for all operations except /auth/**
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, item) -> {
                    if (path.startsWith("/auth")) {
                        return; // login/register públicas
                    }
                    item.readOperations().forEach(operation ->
                            operation.addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                                    .addList("bearerAuth"))
                    );
                });
            }

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

