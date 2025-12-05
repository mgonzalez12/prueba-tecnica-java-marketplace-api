package com.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    private Long id;
    private Long productId;
    private Integer quantity;
    private MovementType type;
    private LocalDateTime timestamp;
    private String referenceOrder; // Optional, link to order

    public enum MovementType {
        IN, OUT, ADJUSTMENT, RESERVED
    }
}
