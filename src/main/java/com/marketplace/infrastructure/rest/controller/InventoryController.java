package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.usecases.InventoryService;
import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.StockMovement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.marketplace.infrastructure.rest.dto.request.InventoryAdjustmentRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product ID")
    public ResponseEntity<Inventory> getInventoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @GetMapping
    @Operation(summary = "Get all inventory")
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock items")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @PostMapping("/product/{productId}/add")
    @Operation(summary = "Add stock to inventory")
    public ResponseEntity<Void> addStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryAdjustmentRequestDto request) {
        inventoryService.addStock(productId, request.quantity(), 
            request.reason() != null ? request.reason() : "Manual addition");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/product/{productId}/remove")
    @Operation(summary = "Remove stock from inventory")
    public ResponseEntity<Void> removeStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryAdjustmentRequestDto request) {
        inventoryService.removeStock(productId, request.quantity(), 
            request.reason() != null ? request.reason() : "Manual removal");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{productId}/movements")
    @Operation(summary = "Get stock movements by product ID")
    public ResponseEntity<List<StockMovement>> getMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getMovementsByProductId(productId));
    }
}

