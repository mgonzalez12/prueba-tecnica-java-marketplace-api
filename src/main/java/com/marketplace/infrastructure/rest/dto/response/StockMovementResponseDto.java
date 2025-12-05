package com.marketplace.infrastructure.rest.dto.response;

import java.time.LocalDateTime;

public record StockMovementResponseDto(
    Long id,
    Long productId,
    Integer quantity,
    String type,
    LocalDateTime timestamp,
    String referenceOrder
) {}

