package com.marketplace.infrastructure.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequestDto(
    @Schema(description = "Product ID", example = "1")
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @Schema(description = "Quantity", example = "2")
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity,
    
    @Schema(description = "Unit price", example = "99.99")
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    BigDecimal unitPrice
) {}

