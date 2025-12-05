package com.marketplace.infrastructure.rest.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for external product API response (FakeAPI Platzi).
 * Located under infrastructure.rest.dto to keep all DTOs in a single root package.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalProductDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private String description;
    private List<String> images;
    private ExternalCategoryDto category;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExternalCategoryDto {
        private Long id;
        private String name;
        private String image;
    }
}


