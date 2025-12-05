package com.marketplace.application.service;

import com.marketplace.application.usecases.InventoryService;
import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.StockMovement;
import com.marketplace.domain.port.InventoryPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryManagementService implements InventoryService {

    private final InventoryPersistencePort inventoryPersistencePort;

    @Override
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryPersistencePort.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product id: " + productId));
    }

    @Override
    @Transactional
    public Inventory updateInventory(Long productId, Integer quantity) {
        Inventory inventory = inventoryPersistencePort.findByProductId(productId)
                .orElse(Inventory.builder()
                        .productId(productId)
                        .quantity(0)
                        .reservedQuantity(0)
                        .build());
        
        inventory.setQuantity(quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        
        return inventoryPersistencePort.save(inventory);
    }

    @Override
    @Transactional
    public void addStock(Long productId, int quantity, String reason) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.addStock(quantity);
        inventoryPersistencePort.save(inventory);
        
        // Register movement
        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .quantity(quantity)
                .type(StockMovement.MovementType.IN)
                .timestamp(LocalDateTime.now())
                .referenceOrder(reason)
                .build();
        registerMovement(movement);
    }

    @Override
    @Transactional
    public void removeStock(Long productId, int quantity, String reason) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.removeStock(quantity);
        inventoryPersistencePort.save(inventory);
        
        // Register movement
        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .quantity(quantity)
                .type(StockMovement.MovementType.OUT)
                .timestamp(LocalDateTime.now())
                .referenceOrder(reason)
                .build();
        registerMovement(movement);
    }

    @Override
    @Transactional
    public void reserveStock(Long productId, int quantity, String orderReference) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.reserveStock(quantity);
        inventoryPersistencePort.save(inventory);
        
        // Register movement
        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .quantity(quantity)
                .type(StockMovement.MovementType.RESERVED)
                .timestamp(LocalDateTime.now())
                .referenceOrder(orderReference)
                .build();
        registerMovement(movement);
    }

    @Override
    @Transactional
    public void releaseReservedStock(Long productId, int quantity, String orderReference) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.releaseReservedStock(quantity);
        inventoryPersistencePort.save(inventory);
        
        // Note: We don't create a movement for release, as it's just a status change
        // The original RESERVED movement remains in history
    }

    @Override
    @Transactional
    public void confirmReservedStock(Long productId, int quantity, String orderReference) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.confirmReservedStock(quantity);
        inventoryPersistencePort.save(inventory);
        
        // Register OUT movement for the confirmed reservation
        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .quantity(quantity)
                .type(StockMovement.MovementType.OUT)
                .timestamp(LocalDateTime.now())
                .referenceOrder(orderReference)
                .build();
        registerMovement(movement);
    }

    @Override
    @Transactional
    public void registerMovement(StockMovement movement) {
        if (movement.getTimestamp() == null) {
            movement.setTimestamp(LocalDateTime.now());
        }
        inventoryPersistencePort.saveMovement(movement);
    }

    @Override
    public List<StockMovement> getMovementsByProductId(Long productId) {
        return inventoryPersistencePort.findMovementsByProductId(productId);
    }

    @Override
    public List<Inventory> getLowStockItems() {
        return getAllInventory().stream()
                .filter(Inventory::needsReorder)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> getAllInventory() {
        return inventoryPersistencePort.findAll();
    }
}

