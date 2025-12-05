package com.marketplace.infrastructure.adapter;

import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.StockMovement;
import com.marketplace.domain.port.InventoryPersistencePort;
import com.marketplace.infrastructure.adapter.entity.InventoryEntity;
import com.marketplace.infrastructure.adapter.entity.StockMovementEntity;
import com.marketplace.infrastructure.adapter.repository.InventoryJpaRepository;
import com.marketplace.infrastructure.adapter.repository.StockMovementJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventorySpringJpaAdapter implements InventoryPersistencePort {

    private final InventoryJpaRepository inventoryJpaRepository;
    private final StockMovementJpaRepository stockMovementJpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = toEntity(inventory);
        InventoryEntity saved = inventoryJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return inventoryJpaRepository.findByProductId(productId)
                .map(this::toDomain);
    }

    @Override
    public List<Inventory> findAll() {
        return inventoryJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void saveMovement(StockMovement movement) {
        StockMovementEntity entity = toMovementEntity(movement);
        stockMovementJpaRepository.save(entity);
    }

    @Override
    public List<StockMovement> findMovementsByProductId(Long productId) {
        return stockMovementJpaRepository.findByProductIdOrderByTimestampDesc(productId).stream()
                .map(this::toMovementDomain)
                .collect(Collectors.toList());
    }

    private InventoryEntity toEntity(Inventory domain) {
        if (domain == null) return null;
        
        InventoryEntity entity = new InventoryEntity();
        entity.setId(domain.getId());
        entity.setProductId(domain.getProductId());
        entity.setQuantity(domain.getQuantity());
        entity.setReservedQuantity(domain.getReservedQuantity());
        entity.setMinimumStockLevel(domain.getMinimumStockLevel());
        entity.setMaximumStockLevel(domain.getMaximumStockLevel());
        entity.setLocation(domain.getLocation());
        entity.setLastUpdated(domain.getLastUpdated());
        
        return entity;
    }

    private Inventory toDomain(InventoryEntity entity) {
        if (entity == null) return null;
        
        return Inventory.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .reservedQuantity(entity.getReservedQuantity())
                .minimumStockLevel(entity.getMinimumStockLevel())
                .maximumStockLevel(entity.getMaximumStockLevel())
                .location(entity.getLocation())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }

    private StockMovementEntity toMovementEntity(StockMovement domain) {
        if (domain == null) return null;
        
        StockMovementEntity entity = new StockMovementEntity();
        entity.setId(domain.getId());
        entity.setProductId(domain.getProductId());
        entity.setQuantity(domain.getQuantity());
        entity.setType(domain.getType() != null ? 
            StockMovementEntity.MovementType.valueOf(domain.getType().name()) : null);
        entity.setTimestamp(domain.getTimestamp());
        entity.setReferenceOrder(domain.getReferenceOrder());
        
        return entity;
    }

    private StockMovement toMovementDomain(StockMovementEntity entity) {
        if (entity == null) return null;
        
        return StockMovement.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .type(entity.getType() != null ? 
                    StockMovement.MovementType.valueOf(entity.getType().name()) : null)
                .timestamp(entity.getTimestamp())
                .referenceOrder(entity.getReferenceOrder())
                .build();
    }
}

