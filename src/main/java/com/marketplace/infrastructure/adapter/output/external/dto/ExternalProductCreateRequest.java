package com.marketplace.infrastructure.adapter.output.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating products in the external Fake Store API.
 * Matches the expected payload:
 * {
 *   "title": "New Product",
 *   "price": 10,
 *   "description": "A description",
 *   "categoryId": 1,
 *   "images": ["https://placehold.co/600x400"]
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalProductCreateRequest {
    private String title;
    private BigDecimal price;
    private String description;
    private Long categoryId;
    private List<String> images;
}


