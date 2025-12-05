package com.marketplace.infrastructure.adapter.output.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for external product API response (FakeAPI Platzi)
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

