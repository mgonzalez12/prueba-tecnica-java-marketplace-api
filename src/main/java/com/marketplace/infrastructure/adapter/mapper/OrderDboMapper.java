package com.marketplace.infrastructure.adapter.mapper;

import com.marketplace.domain.model.Order;
import com.marketplace.domain.model.OrderItem;
import com.marketplace.infrastructure.adapter.entity.OrderEntity;
import com.marketplace.infrastructure.adapter.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderDboMapper {

    @Mapping(target = "items", source = "items")
    OrderEntity toEntity(Order domain);

    Order toDomain(OrderEntity entity);

    @Mapping(target = "order", ignore = true)
    OrderItemEntity toEntity(OrderItem domain);

    OrderItem toDomain(OrderItemEntity entity);
}

