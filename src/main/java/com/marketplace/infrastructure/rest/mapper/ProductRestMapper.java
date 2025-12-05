package com.marketplace.infrastructure.rest.mapper;

import com.marketplace.domain.model.Product;
import com.marketplace.infrastructure.rest.dto.request.ProductRequestDto;
import com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto;
import com.marketplace.infrastructure.rest.dto.response.ProductResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ProductRestMapper {

    @Autowired
    protected CategoryRestMapper categoryRestMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "isExternal", ignore = true)
    @Mapping(target = "externalProviderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", defaultValue = "true")
    public abstract Product toDomain(ProductRequestDto request);

    public ProductResponseDto toResponse(Product product) {
        if (product == null) {
            return null;
        }
        
        CategoryResponseDto categoryDto = null;
        if (product.getCategory() != null && categoryRestMapper != null) {
            categoryDto = categoryRestMapper.toResponse(product.getCategory());
        }
        
        return new ProductResponseDto(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getPromotionalPrice(),
            product.getEffectivePrice(),
            categoryDto,
            product.getFeatures(),
            product.getMetadata(),
            product.getActive(),
            product.getIsExternal(),
            product.getExternalProviderId(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}

