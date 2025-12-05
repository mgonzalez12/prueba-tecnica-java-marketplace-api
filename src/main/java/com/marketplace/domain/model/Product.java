package com.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal promotionalPrice; // For promotions
    private Category category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;

    // Metadata for matrix calculations (feature vector for similarity/recommendation)
    private double[] features;

    // Additional metadata (key-value pairs for flexible product attributes)
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    // External product flag
    private Boolean isExternal;
    private String externalProviderId; // ID from external API

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPromotionalPrice(BigDecimal promotionalPrice) {
        if (promotionalPrice != null && promotionalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Promotional price cannot be negative");
        }
        this.promotionalPrice = promotionalPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getEffectivePrice() {
        return (promotionalPrice != null && promotionalPrice.compareTo(BigDecimal.ZERO) > 0) 
            ? promotionalPrice 
            : price;
    }
}
