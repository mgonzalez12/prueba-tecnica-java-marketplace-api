package com.marketplace.infrastructure.adapter.mapper;

import com.marketplace.domain.model.Category;
import com.marketplace.infrastructure.adapter.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryDboMapper {

    @Mapping(target = "subCategories", ignore = true)
    CategoryEntity toEntity(Category domain);

    @Mapping(target = "subCategories", ignore = true)
    Category toDomain(CategoryEntity entity);
}
