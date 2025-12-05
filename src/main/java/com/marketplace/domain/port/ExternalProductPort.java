package com.marketplace.domain.port;

import com.marketplace.domain.model.Product;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Port for integrating with external product APIs
 */
public interface ExternalProductPort {
    
    /**
     * Fetches products from external API
     * @return List of external products
     */
    CompletableFuture<List<Product>> fetchExternalProducts();
    
    /**
     * Fetches a single product by external ID
     * @param externalId External product identifier
     * @return Product from external API
     */
    CompletableFuture<Product> fetchProductByExternalId(String externalId);
    
    /**
     * Processes and synchronizes external products with local database
     * Determines if products should be stored, updated, or only displayed
     * @param externalProducts Products from external API
     * @return List of processed products
     */
    List<Product> synchronizeExternalProducts(List<Product> externalProducts);

    /**
     * Creates a product directly in the external API and returns the mapped domain product.
     * This does NOT persist the product locally (unless explicitly done by the caller).
     */
    CompletableFuture<Product> createExternalProduct(Product product);
}

