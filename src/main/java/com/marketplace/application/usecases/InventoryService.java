package com.marketplace.application.usecases;

import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.StockMovement;
import java.util.List;

public interface InventoryService {
    Inventory getInventoryByProductId(Long productId);

    Inventory updateInventory(Long productId, Integer quantity);

    void addStock(Long productId, int quantity, String reason);

    void removeStock(Long productId, int quantity, String reason);

    void reserveStock(Long productId, int quantity, String orderReference);

    void releaseReservedStock(Long productId, int quantity, String orderReference);

    void confirmReservedStock(Long productId, int quantity, String orderReference);

    void registerMovement(StockMovement movement);

    List<StockMovement> getMovementsByProductId(Long productId);

    List<Inventory> getLowStockItems();

    List<Inventory> getAllInventory();
}
