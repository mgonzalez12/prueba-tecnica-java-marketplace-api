package com.marketplace.infrastructure.rest.mapper;

import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.StockMovement;
import com.marketplace.infrastructure.rest.dto.response.InventoryResponseDto;
import com.marketplace.infrastructure.rest.dto.response.StockMovementResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryRestMapper {

    @Mapping(target = "availableQuantity", source = ".", qualifiedByName = "getAvailableQuantity")
    @Mapping(target = "needsReorder", source = ".", qualifiedByName = "needsReorder")
    InventoryResponseDto toResponse(Inventory inventory);

    @Mapping(target = "type", source = ".", qualifiedByName = "getMovementTypeName")
    StockMovementResponseDto toResponse(StockMovement movement);

    @Named("getAvailableQuantity")
    default Integer getAvailableQuantity(Inventory inventory) {
        return inventory != null ? inventory.getAvailableQuantity() : null;
    }

    @Named("needsReorder")
    default Boolean needsReorder(Inventory inventory) {
        return inventory != null ? inventory.needsReorder() : null;
    }

    @Named("getMovementTypeName")
    default String getMovementTypeName(StockMovement movement) {
        return movement.getType() != null ? movement.getType().name() : null;
    }
}

