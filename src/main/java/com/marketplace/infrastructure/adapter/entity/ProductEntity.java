package com.marketplace.infrastructure.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sku;

    private String name;
    
    @Column(length = 2000)
    private String description;

    private BigDecimal price;
    private BigDecimal promotionalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(name = "features", length = 1000)
    private String featuresJson; // Store features array as JSON string

    @Column(name = "metadata", length = 2000)
    private String metadataJson; // Store metadata map as JSON string

    private Boolean active;
    private Boolean isExternal;
    private String externalProviderId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

