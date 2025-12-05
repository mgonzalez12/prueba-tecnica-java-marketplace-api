package com.marketplace.infrastructure.adapter.mapper;

import com.marketplace.domain.model.Product;
import com.marketplace.infrastructure.adapter.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = CategoryDboMapper.class)
public interface ProductDboMapper {

    @Mapping(target = "featuresJson", ignore = true)
    @Mapping(target = "metadataJson", ignore = true)
    ProductEntity toEntity(Product domain);

    @Mapping(target = "features", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    Product toDomain(ProductEntity entity);
}

