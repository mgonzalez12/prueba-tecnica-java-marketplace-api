package com.marketplace.infrastructure.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private LocalDateTime timestamp;

    @Column(length = 100)
    private String referenceOrder;

    public enum MovementType {
        IN, OUT, ADJUSTMENT, RESERVED
    }
}

