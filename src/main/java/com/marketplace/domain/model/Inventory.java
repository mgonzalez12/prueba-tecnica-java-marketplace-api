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
public class Inventory {

    private Long id;
    private Long productId;
    private Integer quantity;
    private Integer reservedQuantity; // Stock reserved for pending orders
    private Integer minimumStockLevel; // Reorder point
    private Integer maximumStockLevel;
    private LocalDateTime lastUpdated;
    private String location; // Warehouse location

    /**
     * Gets available stock (total - reserved)
     */
    public Integer getAvailableQuantity() {
        int reserved = (reservedQuantity != null) ? reservedQuantity : 0;
        int total = (quantity != null) ? quantity : 0;
        return Math.max(0, total - reserved);
    }

    /**
     * Thread-safe stock addition
     */
    public synchronized void addStock(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        if (this.quantity == null) {
            this.quantity = 0;
        }
        this.quantity += amount;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Thread-safe stock removal with availability check
     */
    public synchronized void removeStock(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        if (this.quantity == null) {
            this.quantity = 0;
        }
        int available = getAvailableQuantity();
        if (available < amount) {
            throw new IllegalStateException(
                String.format("Insufficient stock. Available: %d, Requested: %d", available, amount)
            );
        }
        this.quantity -= amount;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Reserves stock for an order (thread-safe)
     */
    public synchronized void reserveStock(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Reservation amount cannot be negative");
        int available = getAvailableQuantity();
        if (available < amount) {
            throw new IllegalStateException(
                String.format("Cannot reserve stock. Available: %d, Requested: %d", available, amount)
            );
        }
        if (this.reservedQuantity == null) {
            this.reservedQuantity = 0;
        }
        this.reservedQuantity += amount;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Releases reserved stock (thread-safe)
     */
    public synchronized void releaseReservedStock(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Release amount cannot be negative");
        if (this.reservedQuantity == null || this.reservedQuantity < amount) {
            throw new IllegalStateException("Cannot release more stock than reserved");
        }
        this.reservedQuantity -= amount;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Confirms reserved stock (converts reservation to actual removal)
     */
    public synchronized void confirmReservedStock(int amount) {
        releaseReservedStock(amount);
        removeStock(amount);
    }

    /**
     * Checks if stock level is below minimum (needs reorder)
     */
    public boolean needsReorder() {
        if (minimumStockLevel == null) {
            return false;
        }
        return getAvailableQuantity() <= minimumStockLevel;
    }
}
