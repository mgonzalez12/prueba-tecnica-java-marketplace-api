package com.marketplace.application.service;

import com.marketplace.application.usecases.InventoryService;
import com.marketplace.application.usecases.OrderService;
import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.Order;
import com.marketplace.domain.model.OrderItem;
import com.marketplace.domain.port.OrderPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderManagementService implements OrderService {

    private final OrderPersistencePort orderPersistencePort;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // Validar que haya items (estilo funcional)
        var items = java.util.Optional.ofNullable(order.getItems())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Order must have at least one item"));

        // Generar número de orden único si no viene informado
        order.setOrderNumber(
                java.util.Optional.ofNullable(order.getOrderNumber())
                        .orElseGet(() -> "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
        );

        // Fecha de la orden
        order.setOrderDate(
                java.util.Optional.ofNullable(order.getOrderDate())
                        .orElseGet(LocalDateTime::now)
        );

        // Estado inicial usando switch expression (Java 21, soporta case null)
        Order.OrderStatus currentStatus = order.getStatus();
        Order.OrderStatus effectiveStatus = switch (currentStatus) {
            case null -> Order.OrderStatus.PENDING;
            default -> currentStatus;
        };
        order.setStatus(effectiveStatus);

        // Validar y reservar stock para cada item (forEach funcional)
        items.forEach(item -> {
            Inventory inventory = inventoryService.getInventoryByProductId(item.getProductId());

            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new IllegalStateException(
                        String.format(
                                "Insufficient stock for product %d. Available: %d, Requested: %d",
                                item.getProductId(), inventory.getAvailableQuantity(), item.getQuantity()
                        )
                );
            }

            inventoryService.reserveStock(
                    item.getProductId(),
                    item.getQuantity(),
                    order.getOrderNumber()
            );
        });

        // Calculate totals
        calculateOrderTotals(order);

        // Save order
        Order savedOrder = orderPersistencePort.save(order);

        // Try to confirm order automatically if stock is available
        try {
            savedOrder.updateStatus(Order.OrderStatus.CONFIRMED);
            Order confirmedOrder = orderPersistencePort.save(savedOrder);

            // Confirm reserved stock (convert reservation to actual removal)
            java.util.Optional.ofNullable(confirmedOrder.getItems())
                    .orElse(List.of())
                    .forEach(item ->
                            inventoryService.confirmReservedStock(
                                    item.getProductId(),
                                    item.getQuantity(),
                                    confirmedOrder.getOrderNumber()
                            )
                    );

            return confirmedOrder;
        } catch (Exception e) {
            // If confirmation fails, order remains PENDING
            // Stock remains reserved until order is confirmed or cancelled
            return savedOrder;
        }
    }

    @Override
    public Order getOrder(Long id) {
        return orderPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Override
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderPersistencePort.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = getOrder(orderId);
        
        try {
            order.updateStatus(newStatus);

            // Manejo de stock según el nuevo estado (switch expression style)
            switch (newStatus) {
                case CANCELLED -> {
                    // Release reserved stock
                    for (OrderItem item : order.getItems()) {
                        try {
                            inventoryService.releaseReservedStock(
                                    item.getProductId(),
                                    item.getQuantity(),
                                    order.getOrderNumber()
                            );
                        } catch (Exception e) {
                            // Log error but continue
                        }
                    }
                }
                case CONFIRMED, PROCESSING -> {
                    // Confirm reserved stock
                    for (OrderItem item : order.getItems()) {
                        try {
                            inventoryService.confirmReservedStock(
                                    item.getProductId(),
                                    item.getQuantity(),
                                    order.getOrderNumber()
                            );
                        } catch (Exception e) {
                            // Log error but continue
                        }
                    }
                }
                // Otros estados (PENDING, DELIVERED, SHIPPED, FAILED) no requieren cambios de stock
                default -> {
                }
            }

            return orderPersistencePort.save(order);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Cannot update order status: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderPersistencePort.findAll();
    }

    /**
     * Calcula subtotal, impuestos y total de un pedido a partir de sus items.
     */
    private void calculateOrderTotals(Order order) {
        List<OrderItem> items = java.util.Optional.ofNullable(order.getItems())
                .orElse(List.of());

        if (items.isEmpty()) {
            order.setSubtotalAmount(BigDecimal.ZERO);
            order.setTaxAmount(BigDecimal.ZERO);
            order.setTotalAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotalAmount(subtotal);

        // Impuesto 21% con 2 decimales
        BigDecimal tax = subtotal
                .multiply(new BigDecimal("0.21"))
                .setScale(2, RoundingMode.HALF_UP);
        order.setTaxAmount(tax);

        // Total = subtotal + impuesto (2 decimales)
        BigDecimal total = subtotal
                .add(tax)
                .setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmount(total);
    }
}

