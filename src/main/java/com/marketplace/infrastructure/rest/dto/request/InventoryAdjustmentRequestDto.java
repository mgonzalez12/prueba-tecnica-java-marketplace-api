package com.marketplace.infrastructure.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryAdjustmentRequestDto(
    @Schema(description = "Quantity to add or remove", example = "10")
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity,
    
    @Schema(description = "Reason for the adjustment", example = "Stock replenishment")
    String reason
) {}

