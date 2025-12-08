package com.marketplace.infrastructure.rest.dto.external;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating products in the external Fake Store API.
 * Located under infrastructure.rest.dto to keep all DTOs in a single root package.
 *
 * Expected payload:
 * {
 *   "title": "New Product",
 *   "price": 10,
 *   "description": "A description",
 *   "categoryId": 1,
 *   "images": ["https://placehold.co/600x400"]
 * }
 */
public record ExternalProductCreateRequest(
        String title,
        BigDecimal price,
        String description,
        Long categoryId,
        List<String> images
) {
}
