package com.marketplace.infrastructure.rest.mapper;

import com.marketplace.domain.model.Order;
import com.marketplace.domain.model.OrderItem;
import com.marketplace.infrastructure.rest.dto.request.OrderItemRequestDto;
import com.marketplace.infrastructure.rest.dto.request.OrderRequestDto;
import com.marketplace.infrastructure.rest.dto.response.OrderItemResponseDto;
import com.marketplace.infrastructure.rest.dto.response.OrderResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "processedDate", ignore = true)
    @Mapping(target = "shippedDate", ignore = true)
    @Mapping(target = "deliveredDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotalAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "items", source = "items")
    Order toDomain(OrderRequestDto request);

    @Mapping(target = "status", source = ".", qualifiedByName = "getStatusName")
    OrderResponseDto toResponse(Order order);

    @Named("getStatusName")
    default String getStatusName(Order order) {
        return order.getStatus() != null ? order.getStatus().name() : null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    OrderItem toDomain(OrderItemRequestDto request);

    List<OrderItem> toDomainList(List<OrderItemRequestDto> items);

    OrderItemResponseDto toResponse(OrderItem item);

    List<OrderItemResponseDto> toResponseList(List<OrderItem> items);
}

