package com.marketplace.infrastructure.adapter.output.external;

import com.marketplace.domain.model.Product;
import com.marketplace.domain.port.ExternalProductPort;
import com.marketplace.domain.port.ProductPersistencePort;
import com.marketplace.infrastructure.rest.dto.external.ExternalProductCreateRequest;
import com.marketplace.infrastructure.rest.dto.external.ExternalProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Adapter for integrating with external product API (FakeAPI Platzi)
 * Demonstrates:
 * - HTTP client usage (WebClient)
 * - Functional programming
 * - Concurrency (CompletableFuture, Virtual Threads)
 * - Transactional consistency
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalProductAdapter implements ExternalProductPort {

    private final WebClient.Builder webClientBuilder;
    private final ProductPersistencePort productPersistencePort;
    private final ExecutorService virtualThreadExecutor;

    @Value("${external.api.base-url:https://api.escuelajs.co/api/v1}")
    private String externalApiBaseUrl;

    @Value("${external.api.products-path:/products}")
    private String productsPath;

    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder
                    .baseUrl(externalApiBaseUrl)
                    .build();
        }
        return webClient;
    }

    @Override
    public CompletableFuture<List<Product>> fetchExternalProducts() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Fetching external products from {}", externalApiBaseUrl + productsPath);
                
                List<ExternalProductDto> externalProducts = getWebClient()
                        .get()
                        .uri(productsPath)
                        .retrieve()
                        .bodyToFlux(ExternalProductDto.class)
                        .collectList()
                        .block(); // Blocking call, but executed in virtual thread

                if (externalProducts == null || externalProducts.isEmpty()) {
                    log.warn("No external products found");
                    return new ArrayList<>();
                }

                log.info("Fetched {} external products", externalProducts.size());
                
                // Convert to domain models using functional programming
                return externalProducts.stream()
                        .map(this::mapToDomainProduct)
                        .collect(Collectors.toList());
                        
            } catch (Exception e) {
                log.error("Error fetching external products", e);
                throw new RuntimeException("Failed to fetch external products: " + e.getMessage(), e);
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<Product> fetchProductByExternalId(String externalId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Fetching external product with ID: {}", externalId);
                
                ExternalProductDto externalProduct = getWebClient()
                        .get()
                        .uri(productsPath + "/{id}", externalId)
                        .retrieve()
                        .bodyToMono(ExternalProductDto.class)
                        .block();

                if (externalProduct == null) {
                    throw new RuntimeException("External product not found with ID: " + externalId);
                }

                return mapToDomainProduct(externalProduct);
                
            } catch (Exception e) {
                log.error("Error fetching external product with ID: {}", externalId, e);
                throw new RuntimeException("Failed to fetch external product: " + e.getMessage(), e);
            }
        }, virtualThreadExecutor);
    }

    @Override
    public List<Product> synchronizeExternalProducts(List<Product> externalProducts) {
        log.info("Synchronizing {} external products", externalProducts.size());
        
        List<Product> synchronizedProducts = new ArrayList<>();
        
        for (Product externalProduct : externalProducts) {
            try {
                // Check if product already exists by external provider ID
                Product existingProduct = findExistingProduct(externalProduct);
                
                if (existingProduct != null) {
                    // Update existing product
                    updateProductFromExternal(existingProduct, externalProduct);
                    synchronizedProducts.add(productPersistencePort.save(existingProduct));
                    log.debug("Updated product: {}", existingProduct.getSku());
                } else {
                    // Create new product
                    externalProduct.setIsExternal(true);
                    synchronizedProducts.add(productPersistencePort.save(externalProduct));
                    log.debug("Created new product: {}", externalProduct.getSku());
                }
            } catch (Exception e) {
                log.error("Error synchronizing product: {}", externalProduct.getSku(), e);
                // Continue with next product instead of failing entire batch
            }
        }
        
        log.info("Successfully synchronized {} products", synchronizedProducts.size());
        return synchronizedProducts;
    }

    @Override
    public CompletableFuture<Product> createExternalProduct(Product product) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Creating external product from local product SKU={}", product.getSku());

                Long categoryId = null;
                if (product.getCategory() != null && product.getCategory().getId() != null) {
                    categoryId = product.getCategory().getId();
                }

                // Try to extract images from metadata if present
                java.util.List<String> images = java.util.List.of("https://placehold.co/600x400");
                if (product.getMetadata() != null) {
                    String imagesCsv = product.getMetadata().get("images");
                    if (imagesCsv != null && !imagesCsv.isBlank()) {
                        images = java.util.Arrays.asList(imagesCsv.split(","));
                    }
                }

                ExternalProductCreateRequest request = new ExternalProductCreateRequest(
                        product.getName(),
                        product.getPrice(),
                        product.getDescription(),
                        categoryId,
                        images
                );

                ExternalProductDto created = getWebClient()
                        .post()
                        .uri(productsPath)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(ExternalProductDto.class)
                        .block();

                if (created == null) {
                    throw new RuntimeException("External API returned null when creating product");
                }

                return mapToDomainProduct(created);
            } catch (Exception e) {
                log.error("Error creating external product from local product SKU={}", product.getSku(), e);
                throw new RuntimeException("Failed to create external product: " + e.getMessage(), e);
            }
        }, virtualThreadExecutor);
    }

    /**
     * Maps external product DTO to domain Product model
     * Uses functional programming approach
     */
    private Product mapToDomainProduct(ExternalProductDto dto) {
        Product.ProductBuilder builder = Product.builder()
                .name(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .sku("EXT-" + dto.getId()) // Generate SKU from external ID
                .isExternal(true)
                .externalProviderId(String.valueOf(dto.getId()))
                .active(true);

        // Note: we do NOT attach a Category entity here to avoid foreign key issues.
        // Category information from the external API is stored in metadata / features instead.

        // Create feature vector from product attributes for matrix calculations
        // Simple feature extraction: price normalized, category ID, etc.
        double[] features = extractFeatures(dto);
        builder.features(features);

        // Store images in metadata
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            metadata.put("images", String.join(",", dto.getImages()));
        }
        if (dto.getCategory() != null) {
            metadata.put("externalCategoryId", String.valueOf(dto.getCategory().getId()));
            metadata.put("externalCategoryName", dto.getCategory().getName());
        }
        builder.metadata(metadata);

        return builder.build();
    }

    /**
     * Extracts feature vector from product for similarity/recommendation calculations
     * Demonstrates matrix/vector operations
     */
    private double[] extractFeatures(ExternalProductDto dto) {
        // Simple feature vector: [normalized_price, category_id, has_images]
        double normalizedPrice = dto.getPrice() != null ? 
            dto.getPrice().doubleValue() / 1000.0 : 0.0; // Normalize price
        double categoryId = dto.getCategory() != null ? 
            dto.getCategory().getId().doubleValue() : 0.0;
        double hasImages = (dto.getImages() != null && !dto.getImages().isEmpty()) ? 1.0 : 0.0;
        
        return new double[]{normalizedPrice, categoryId, hasImages};
    }

    /**
     * Finds existing product by external provider ID or SKU
     */
    private Product findExistingProduct(Product externalProduct) {
        // Try to find by external provider ID
        if (externalProduct.getExternalProviderId() != null) {
            List<Product> allProducts = productPersistencePort.findAll();
            return allProducts.stream()
                    .filter(p -> externalProduct.getExternalProviderId().equals(p.getExternalProviderId()))
                    .findFirst()
                    .orElse(null);
        }
        
        // Try to find by SKU
        if (externalProduct.getSku() != null) {
            return productPersistencePort.findBySku(externalProduct.getSku()).orElse(null);
        }
        
        return null;
    }

    /**
     * Updates existing product with data from external product
     */
    private void updateProductFromExternal(Product existing, Product external) {
        // Update price if changed
        if (external.getPrice() != null && 
            !external.getPrice().equals(existing.getPrice())) {
            existing.updatePrice(external.getPrice());
        }
        
        // Update description
        if (external.getDescription() != null) {
            existing.setDescription(external.getDescription());
        }
        
        // Update features
        if (external.getFeatures() != null) {
            existing.setFeatures(external.getFeatures());
        }
        
        // Update metadata
        if (external.getMetadata() != null) {
            if (existing.getMetadata() == null) {
                existing.setMetadata(new java.util.HashMap<>());
            }
            existing.getMetadata().putAll(external.getMetadata());
        }
    }
}

