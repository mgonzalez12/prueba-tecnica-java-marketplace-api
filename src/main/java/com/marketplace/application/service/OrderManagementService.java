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
        // Validate order has items
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        // Generate unique order number
        if (order.getOrderNumber() == null) {
            order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        // Set order date
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        
        // Set initial status
        if (order.getStatus() == null) {
            order.setStatus(Order.OrderStatus.PENDING);
        }
        
        // Validate and reserve stock for each item
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryService.getInventoryByProductId(item.getProductId());
            
            // Check availability
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new IllegalStateException(
                    String.format("Insufficient stock for product %d. Available: %d, Requested: %d",
                        item.getProductId(), inventory.getAvailableQuantity(), item.getQuantity())
                );
            }
            
            // Reserve stock
            inventoryService.reserveStock(
                item.getProductId(), 
                item.getQuantity(), 
                order.getOrderNumber()
            );
        }
        
        // Calculate totals
        calculateOrderTotals(order);
        
        // Save order
        Order savedOrder = orderPersistencePort.save(order);
        
        // Try to confirm order automatically if stock is available
        try {
            savedOrder.updateStatus(Order.OrderStatus.CONFIRMED);
            savedOrder = orderPersistencePort.save(savedOrder);
            
            // Confirm reserved stock (convert reservation to actual removal)
            for (OrderItem item : savedOrder.getItems()) {
                inventoryService.confirmReservedStock(
                    item.getProductId(),
                    item.getQuantity(),
                    savedOrder.getOrderNumber()
                );
            }
        } catch (Exception e) {
            // If confirmation fails, order remains PENDING
            // Stock remains reserved until order is confirmed or cancelled
        }
        
        return savedOrder;
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
            
            // Handle stock based on status change
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
            List<OrderItem> items = order.getItems();

            if (items == null || items.isEmpty()) {
                order.setSubtotalAmount(BigDecimal.ZERO);
                order.setTaxAmount(BigDecimal.ZERO);
                order.setTotalAmount(BigDecimal.ZERO);
                return;
            }

            BigDecimal subtotal = BigDecimal.ZERO;

            for (OrderItem item : items) {
                if (item.getSubtotal() != null) {
                    subtotal = subtotal.add(item.getSubtotal());
                }
            }

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

