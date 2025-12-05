package com.marketplace.infrastructure.adapter;

import com.marketplace.domain.model.Order;
import com.marketplace.domain.model.OrderItem;
import com.marketplace.domain.port.OrderPersistencePort;
import com.marketplace.infrastructure.adapter.entity.OrderEntity;
import com.marketplace.infrastructure.adapter.entity.OrderItemEntity;
import com.marketplace.infrastructure.adapter.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderSpringJpaAdapter implements OrderPersistencePort {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        OrderEntity saved = orderJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return orderJpaRepository.findByCustomerId(customerId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private OrderEntity toEntity(Order domain) {
        if (domain == null) return null;
        
        OrderEntity entity = new OrderEntity();
        entity.setId(domain.getId());
        entity.setOrderNumber(domain.getOrderNumber());
        entity.setCustomerId(domain.getCustomerId());
        entity.setOrderDate(domain.getOrderDate());
        entity.setProcessedDate(domain.getProcessedDate());
        entity.setShippedDate(domain.getShippedDate());
        entity.setDeliveredDate(domain.getDeliveredDate());
        entity.setStatus(domain.getStatus() != null ? 
            OrderEntity.OrderStatus.valueOf(domain.getStatus().name()) : null);
        entity.setSubtotalAmount(domain.getSubtotalAmount());
        entity.setDiscountAmount(domain.getDiscountAmount());
        entity.setTaxAmount(domain.getTaxAmount());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setShippingAddress(domain.getShippingAddress());
        entity.setNotes(domain.getNotes());
        
        // Map items
        if (domain.getItems() != null) {
            List<OrderItemEntity> itemEntities = domain.getItems().stream()
                    .map(item -> {
                        OrderItemEntity itemEntity = new OrderItemEntity();
                        itemEntity.setId(item.getId());
                        itemEntity.setProductId(item.getProductId());
                        itemEntity.setQuantity(item.getQuantity());
                        itemEntity.setUnitPrice(item.getUnitPrice());
                        itemEntity.setSubtotal(item.getSubtotal());
                        itemEntity.setOrder(entity);
                        return itemEntity;
                    })
                    .collect(Collectors.toList());
            entity.setItems(itemEntities);
        }
        
        return entity;
    }

    private Order toDomain(OrderEntity entity) {
        if (entity == null) return null;
        
        Order.OrderBuilder builder = Order.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .customerId(entity.getCustomerId())
                .orderDate(entity.getOrderDate())
                .processedDate(entity.getProcessedDate())
                .shippedDate(entity.getShippedDate())
                .deliveredDate(entity.getDeliveredDate())
                .status(entity.getStatus() != null ? 
                    Order.OrderStatus.valueOf(entity.getStatus().name()) : null)
                .subtotalAmount(entity.getSubtotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .taxAmount(entity.getTaxAmount())
                .totalAmount(entity.getTotalAmount())
                .shippingAddress(entity.getShippingAddress())
                .notes(entity.getNotes());
        
        // Map items
        if (entity.getItems() != null) {
            List<OrderItem> items = entity.getItems().stream()
                    .map(itemEntity -> OrderItem.builder()
                            .id(itemEntity.getId())
                            .productId(itemEntity.getProductId())
                            .quantity(itemEntity.getQuantity())
                            .unitPrice(itemEntity.getUnitPrice())
                            .subtotal(itemEntity.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
            builder.items(items);
        }
        
        return builder.build();
    }
}

