package com.marketplace.domain.service;

import com.marketplace.domain.model.Order;
import com.marketplace.domain.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Domain Service Tests")
class OrderDomainServiceTest {

    private OrderDomainService orderDomainService;

    @BeforeEach
    void setUp() {
        orderDomainService = new OrderDomainService();
    }

    @Test
    @DisplayName("Should calculate order totals correctly")
    void shouldCalculateOrderTotalsCorrectly() {
        // Given
        Order order = Order.builder()
                .items(new ArrayList<>())
                .build();
        
        OrderItem item1 = OrderItem.builder()
                .productId(1L)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .build();
        item1.calculateSubtotal();
        
        OrderItem item2 = OrderItem.builder()
                .productId(2L)
                .quantity(3)
                .unitPrice(new BigDecimal("50.00"))
                .build();
        item2.calculateSubtotal();
        
        order.addItem(item1);
        order.addItem(item2);

        // When
        orderDomainService.calculateOrderTotals(order);

        // Then
        assertEquals(new BigDecimal("350.00"), order.getSubtotalAmount());
        assertEquals(new BigDecimal("73.50"), order.getTaxAmount()); // 21% of 350
        assertEquals(new BigDecimal("423.50"), order.getTotalAmount());
    }

    @Test
    @DisplayName("Should handle empty order items")
    void shouldHandleEmptyOrderItems() {
        // Given
        Order order = Order.builder()
                .items(new ArrayList<>())
                .build();

        // When
        orderDomainService.calculateOrderTotals(order);

        // Then
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        assertEquals(BigDecimal.ZERO, order.getTaxAmount());
    }
}

