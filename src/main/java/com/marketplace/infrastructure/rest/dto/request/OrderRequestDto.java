package com.marketplace.infrastructure.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequestDto(
    @Schema(description = "Customer ID", example = "1")
    @NotNull(message = "Customer ID is required")
    Long customerId,
    
    @Schema(description = "List of order items")
    @NotEmpty(message = "Order must have at least one item")
    List<OrderItemRequestDto> items,
    
    @Schema(description = "Shipping address", example = "123 Main St, City, Country")
    String shippingAddress,
    
    @Schema(description = "Additional notes", example = "Leave at front door")
    String notes
) {}

