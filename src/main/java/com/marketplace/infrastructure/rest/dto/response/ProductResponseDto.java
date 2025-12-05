package com.marketplace.infrastructure.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record ProductResponseDto(
    Long id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    BigDecimal promotionalPrice,
    BigDecimal effectivePrice,
    CategoryResponseDto category,
    double[] features,
    Map<String, String> metadata,
    Boolean active,
    Boolean isExternal,
    String externalProviderId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

