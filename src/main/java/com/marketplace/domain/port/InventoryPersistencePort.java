package com.marketplace.domain.port;

import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.StockMovement;
import java.util.Optional;
import java.util.List;

public interface InventoryPersistencePort {
    Inventory save(Inventory inventory);

    Optional<Inventory> findByProductId(Long productId);

    List<Inventory> findAll();

    void saveMovement(StockMovement movement);

    List<StockMovement> findMovementsByProductId(Long productId);
}
