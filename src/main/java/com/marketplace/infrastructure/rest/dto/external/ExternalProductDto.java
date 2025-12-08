package com.marketplace.infrastructure.rest.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for external product API response (FakeAPI Platzi).
 * Located under infrastructure.rest.dto to keep all DTOs in a single root package.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalProductDto(
        Long id,
        String title,
        BigDecimal price,
        String description,
        List<String> images,
        ExternalCategoryDto category
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExternalCategoryDto(
            Long id,
            String name,
            String image
    ) {
    }
}
