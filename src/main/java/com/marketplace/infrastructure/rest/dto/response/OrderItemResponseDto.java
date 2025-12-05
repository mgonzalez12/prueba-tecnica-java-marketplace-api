package com.marketplace.infrastructure.rest.dto.response;

import java.math.BigDecimal;

public record OrderItemResponseDto(
    Long id,
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}

