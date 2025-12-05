package com.marketplace.infrastructure.rest.dto.response;

import java.time.LocalDateTime;

public record InventoryResponseDto(
    Long id,
    Long productId,
    Integer quantity,
    Integer reservedQuantity,
    Integer availableQuantity,
    Integer minimumStockLevel,
    Integer maximumStockLevel,
    String location,
    LocalDateTime lastUpdated,
    Boolean needsReorder
) {}

