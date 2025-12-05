package com.marketplace.infrastructure.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(columnNames = "product_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", unique = true, nullable = false)
    private Long productId;

    private Integer quantity;
    private Integer reservedQuantity;
    private Integer minimumStockLevel;
    private Integer maximumStockLevel;
    private String location;

    private LocalDateTime lastUpdated;
}

