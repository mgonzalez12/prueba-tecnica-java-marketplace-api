package com.marketplace.infrastructure.adapter.mapper;

import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.StockMovement;
import com.marketplace.infrastructure.adapter.entity.InventoryEntity;
import com.marketplace.infrastructure.adapter.entity.StockMovementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryDboMapper {

    InventoryEntity toEntity(Inventory domain);

    Inventory toDomain(InventoryEntity entity);

    StockMovementEntity toEntity(StockMovement domain);

    StockMovement toDomain(StockMovementEntity entity);
}

