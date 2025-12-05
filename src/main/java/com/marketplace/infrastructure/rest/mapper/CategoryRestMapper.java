package com.marketplace.infrastructure.rest.mapper;

import com.marketplace.domain.model.Category;
import com.marketplace.infrastructure.rest.dto.request.CategoryRequestDto;
import com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", defaultValue = "true")
    Category toDomain(CategoryRequestDto request);

    default CategoryResponseDto toResponse(Category category) {
        if (category == null) {
            return null;
        }
        
        // Map subcategories without recursion to avoid Swagger issues
        java.util.List<com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto> subCategoriesDto = 
            java.util.Collections.emptyList();
        
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            subCategoriesDto = category.getSubCategories().stream()
                .map(sub -> new com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto(
                    sub.getId(),
                    sub.getName(),
                    sub.getDescription(),
                    sub.getCode(),
                    sub.getActive(),
                    sub.getParent() != null ? sub.getParent().getId() : null,
                    sub.getParent() != null ? sub.getParent().getName() : null,
                    java.util.Collections.emptyList(), // Empty to avoid recursion in Swagger
                    sub.getCreatedAt(),
                    sub.getUpdatedAt(),
                    sub.getFullPath()
                ))
                .toList();
        }
        
        return new com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getCode(),
            category.getActive(),
            category.getParent() != null ? category.getParent().getId() : null,
            category.getParent() != null ? category.getParent().getName() : null,
            subCategoriesDto,
            category.getCreatedAt(),
            category.getUpdatedAt(),
            category.getFullPath()
        );
    }
}
