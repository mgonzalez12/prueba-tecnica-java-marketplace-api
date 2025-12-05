package com.marketplace.infrastructure.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Category response data")
@JsonIgnoreProperties(value = {"subCategories"}, allowSetters = true)
public record CategoryResponseDto(
    @Schema(description = "Category ID", example = "1")
    Long id,
    
    @Schema(description = "Category name", example = "Electronics")
    String name,
    
    @Schema(description = "Category description")
    String description,
    
    @Schema(description = "Category code", example = "ELEC")
    String code,
    
    @Schema(description = "Is category active", example = "true")
    Boolean active,
    
    @Schema(description = "Parent category ID")
    Long parentId,
    
    @Schema(description = "Parent category name")
    String parentName,
    
    @Schema(description = "Subcategories (limited depth to avoid circular references)")
    List<CategoryResponseDto> subCategories,
    
    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,
    
    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt,
    
    @Schema(description = "Full category path from root", example = "Electronics > Laptops")
    String fullPath
) {}
