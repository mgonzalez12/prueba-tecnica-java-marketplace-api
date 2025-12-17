package com.marketplace.infrastructure.rest.mapper;

import com.marketplace.domain.model.Order;
import com.marketplace.domain.model.OrderItem;
import com.marketplace.infrastructure.rest.dto.request.OrderItemRequestDto;
import com.marketplace.infrastructure.rest.dto.request.OrderRequestDto;
import com.marketplace.infrastructure.rest.dto.response.OrderItemResponseDto;
import com.marketplace.infrastructure.rest.dto.response.OrderResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderRestMapper {

    default Order toDomain(OrderRequestDto request) {
        if (request == null) {
            return null;
        }
        
        Order order = Order.builder()
                .customerId(request.customerId())
                .shippingAddress(request.shippingAddress())
                .notes(request.notes())
                .build();
        
        if (request.items() != null) {
            order.setItems(toDomainList(request.items()));
        }
        
        return order;
    }

    default OrderResponseDto toResponse(Order order) {
        if (order == null) {
            return null;
        }
        
        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getOrderDate(),
                order.getProcessedDate(),
                order.getShippedDate(),
                order.getDeliveredDate(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getNotes(),
                toResponseList(order.getItems())
        );
    }

    default OrderItem toDomain(OrderItemRequestDto request) {
        if (request == null) {
            return null;
        }
        
        return OrderItem.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .build();
    }

    default List<OrderItem> toDomainList(List<OrderItemRequestDto> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toDomain)
                .toList();
    }

    default OrderItemResponseDto toResponse(OrderItem item) {
        if (item == null) {
            return null;
        }
        
        return new OrderItemResponseDto(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    default List<OrderItemResponseDto> toResponseList(List<OrderItem> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toResponse)
                .toList();
    }
}

