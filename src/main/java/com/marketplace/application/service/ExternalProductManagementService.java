package com.marketplace.application.service;

import com.marketplace.domain.model.Product;
import com.marketplace.domain.port.ExternalProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Service for managing external product integration
 * Demonstrates:
 * - Async processing
 * - CompletableFuture usage
 * - Concurrent processing of large volumes
 * - Data consistency
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalProductManagementService {

    private final ExternalProductPort externalProductPort;
    private final ExecutorService virtualThreadExecutor;

    /**
     * Fetches and synchronizes external products asynchronously
     * Uses CompletableFuture for concurrent processing
     */
    @Async
    public CompletableFuture<List<Product>> fetchAndSynchronizeProducts() {
        log.info("Starting external product synchronization");
        
        return externalProductPort.fetchExternalProducts()
                .thenApply(externalProducts -> {
                    log.info("Fetched {} external products from provider", externalProducts.size());
                    
                    // For now, synchronize all fetched products directly.
                    // (Additional validation rules can be applied later via processAndSynchronize)
                    List<Product> synchronizedProducts = 
                        externalProductPort.synchronizeExternalProducts(externalProducts);
                    
                    log.info("Successfully synchronized {} products", synchronizedProducts.size());
                    return synchronizedProducts;
                })
                .exceptionally(throwable -> {
                    log.error("Error during product synchronization", throwable);
                    return List.of(); // Return empty list on error
                });
    }

    /**
     * Fetches a single external product by ID
     */
    public CompletableFuture<Product> fetchExternalProduct(String externalId) {
        String normalizedId = normalizeExternalId(externalId);
        return externalProductPort.fetchProductByExternalId(normalizedId);
    }

    /**
     * Creates a product directly in the external API (without persisting it locally).
     * The returned Product is the mapped representation of the external API response.
     */
    public CompletableFuture<Product> createExternalProduct(Product product) {
        return externalProductPort.createExternalProduct(product);
    }

    /**
     * Processes multiple external products concurrently
     * Demonstrates handling large volumes simultaneously
     */
    @Transactional
    public List<Product> processExternalProductsBatch(List<Product> externalProducts) {
        log.info("Processing batch of {} external products", externalProducts.size());
        
        // Process in parallel using CompletableFuture over virtual threads
        List<CompletableFuture<Product>> futures = externalProducts.stream()
                .map(product -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // Determine if product should be stored, updated, or only displayed
                        return processSingleProduct(product);
                    } catch (Exception e) {
                        log.error("Error processing product: {}", product.getSku(), e);
                        return null; // Skip failed products
                    }
                }, virtualThreadExecutor))
                .toList();

        // Wait for all futures and collect results
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(product -> product != null) // Filter out failed products
                .toList();
    }

    /**
     * Normalizes an external product identifier so that the API can accept:
     * - The raw numeric ID from the provider (e.g. "336")
     * - The local SKU format used in this system (e.g. "EXT-336")
     */
    private String normalizeExternalId(String externalId) {
        if (externalId == null) {
            return null;
        }
        if (externalId.startsWith("EXT-")) {
            return externalId.substring(4);
        }
        return externalId;
    }

    /**
     * Processes a single external product
     * Determines if it should be stored, updated, or only displayed
     */
    private Product processSingleProduct(Product product) {
        return java.util.Optional.ofNullable(product)
                // Validación básica: nombre obligatorio
                .filter(p -> {
                    if (p.getName() == null || p.getName().isBlank()) {
                        log.warn("Skipping external product without name. SKU={}", p.getSku());
                        return false;
                    }
                    return true;
                })
                // Validación de precio positivo
                .filter(p -> {
                    if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        log.warn("Skipping external product with invalid price. SKU={}, price={}",
                                p.getSku(), p.getPrice());
                        return false;
                    }
                    return true;
                })
                // Regla de negocio: ignorar datos de prueba
                .filter(p -> {
                    String lowerName = p.getName().toLowerCase();
                    if (lowerName.contains("test") || lowerName.contains("dummy")) {
                        log.info("Ignoring external test product. SKU={}, name={}", p.getSku(), p.getName());
                        return false;
                    }
                    return true;
                })
                // Normalización de flags por defecto
                .map(p -> {
                    if (p.getIsExternal() == null) {
                        p.setIsExternal(true);
                    }
                    if (p.getActive() == null) {
                        p.setActive(true);
                    }
                    return p;
                })
                // Si alguna validación falla, devolvemos null (para ser filtrado más adelante)
                .orElse(null);
    }
}

