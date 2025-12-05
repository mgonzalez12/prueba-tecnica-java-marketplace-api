package com.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;
    private Long customerId;
    private String orderNumber; // Unique order identifier
    private LocalDateTime orderDate;
    private LocalDateTime processedDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private OrderStatus status;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String notes;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, FAILED
    }

    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public void updateStatus(OrderStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED -> {
                if (this.status != OrderStatus.PENDING) {
                    throw new IllegalStateException("Only PENDING orders can be confirmed");
                }
                this.processedDate = LocalDateTime.now();
            }
            case SHIPPED -> {
                if (this.status != OrderStatus.CONFIRMED && this.status != OrderStatus.PROCESSING) {
                    throw new IllegalStateException("Order must be confirmed or processing before shipping");
                }
                this.shippedDate = LocalDateTime.now();
            }
            case DELIVERED -> {
                if (this.status != OrderStatus.SHIPPED) {
                    throw new IllegalStateException("Order must be shipped before delivery");
                }
                this.deliveredDate = LocalDateTime.now();
            }
            case CANCELLED -> {
                if (this.status == OrderStatus.DELIVERED || this.status == OrderStatus.SHIPPED) {
                    throw new IllegalStateException("Cannot cancel shipped or delivered orders");
                }
            }
        }
        this.status = newStatus;
    }
}
