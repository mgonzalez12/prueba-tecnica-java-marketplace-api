package com.marketplace.infrastructure.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
    Long id,
    String orderNumber,
    Long customerId,
    LocalDateTime orderDate,
    LocalDateTime processedDate,
    LocalDateTime shippedDate,
    LocalDateTime deliveredDate,
    String status,
    BigDecimal subtotalAmount,
    BigDecimal discountAmount,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    String shippingAddress,
    String notes,
    List<OrderItemResponseDto> items
) {}

