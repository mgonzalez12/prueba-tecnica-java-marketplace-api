package com.marketplace.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.domain.model.Product;
import com.marketplace.domain.port.ProductPersistencePort;
import com.marketplace.infrastructure.adapter.entity.ProductEntity;
import com.marketplace.infrastructure.adapter.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductSpringJpaAdapter implements ProductPersistencePort {

    private final ProductJpaRepository productJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Product save(Product product) {
        ProductEntity entity = toEntity(product);
        ProductEntity saved = productJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productJpaRepository.findBySku(sku)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        productJpaRepository.deleteById(id);
    }

    private ProductEntity toEntity(Product domain) {
        if (domain == null) return null;
        
        ProductEntity entity = new ProductEntity();
        entity.setId(domain.getId());
        entity.setSku(domain.getSku());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPrice(domain.getPrice());
        entity.setPromotionalPrice(domain.getPromotionalPrice());
        entity.setActive(domain.getActive());
        entity.setIsExternal(domain.getIsExternal());
        entity.setExternalProviderId(domain.getExternalProviderId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        
        // Convert features array to JSON
        if (domain.getFeatures() != null) {
            try {
                entity.setFeaturesJson(objectMapper.writeValueAsString(domain.getFeatures()));
            } catch (JsonProcessingException e) {
                log.error("Error converting features to JSON", e);
            }
        }
        
        // Convert metadata map to JSON
        if (domain.getMetadata() != null) {
            try {
                entity.setMetadataJson(objectMapper.writeValueAsString(domain.getMetadata()));
            } catch (JsonProcessingException e) {
                log.error("Error converting metadata to JSON", e);
            }
        }
        
        // Map category if present
        if (domain.getCategory() != null && domain.getCategory().getId() != null) {
            com.marketplace.infrastructure.adapter.entity.CategoryEntity categoryEntity = 
                new com.marketplace.infrastructure.adapter.entity.CategoryEntity();
            categoryEntity.setId(domain.getCategory().getId());
            entity.setCategory(categoryEntity);
        }
        
        return entity;
    }

    private Product toDomain(ProductEntity entity) {
        if (entity == null) return null;
        
        Product.ProductBuilder builder = Product.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .promotionalPrice(entity.getPromotionalPrice())
                .active(entity.getActive())
                .isExternal(entity.getIsExternal())
                .externalProviderId(entity.getExternalProviderId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());
        
        // Convert features JSON to array
        if (entity.getFeaturesJson() != null) {
            try {
                builder.features(objectMapper.readValue(entity.getFeaturesJson(), double[].class));
            } catch (JsonProcessingException e) {
                log.error("Error converting features from JSON", e);
            }
        }
        
        // Convert metadata JSON to map
        if (entity.getMetadataJson() != null) {
            try {
                builder.metadata(objectMapper.readValue(entity.getMetadataJson(), 
                    java.util.Map.class));
            } catch (JsonProcessingException e) {
                log.error("Error converting metadata from JSON", e);
            }
        }
        
        // Map category if present
        if (entity.getCategory() != null) {
            com.marketplace.domain.model.Category category = 
                com.marketplace.domain.model.Category.builder()
                    .id(entity.getCategory().getId())
                    .name(entity.getCategory().getName())
                    .build();
            builder.category(category);
        }
        
        return builder.build();
    }
}

