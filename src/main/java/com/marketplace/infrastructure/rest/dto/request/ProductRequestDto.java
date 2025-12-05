package com.marketplace.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequestDto(
    @NotBlank(message = "SKU is required")
    String sku,
    
    @NotBlank(message = "Name is required")
    String name,
    
    String description,
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    BigDecimal price,
    
    BigDecimal promotionalPrice,
    
    Long categoryId,
    
    double[] features,
    
    Boolean active
) {}

