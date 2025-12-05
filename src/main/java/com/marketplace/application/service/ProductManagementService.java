package com.marketplace.application.service;

import com.marketplace.application.usecases.InventoryService;
import com.marketplace.application.usecases.ProductService;
import com.marketplace.domain.model.Product;
import com.marketplace.domain.port.ProductPersistencePort;
import com.marketplace.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductManagementService implements ProductService {

    private final ProductPersistencePort productPersistencePort;
    private final ProductDomainService productDomainService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public Product createProduct(Product product) {
        // Validate SKU uniqueness
        if (product.getSku() != null && productPersistencePort.findBySku(product.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists");
        }
        
        // Set timestamps
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }
        product.setUpdatedAt(LocalDateTime.now());
        
        // Set active by default
        if (product.getActive() == null) {
            product.setActive(true);
        }
        
        // Initialize metadata if null
        if (product.getMetadata() == null) {
            product.setMetadata(new java.util.HashMap<>());
        }
        
        Product savedProduct = productPersistencePort.save(product);
        
        // Initialize inventory for the new product (stock 0)
        inventoryService.updateInventory(savedProduct.getId(), 0);
        
        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product product) {
        Product existing = getProduct(id);
        
        if (product.getName() != null) {
            existing.setName(product.getName());
        }
        if (product.getDescription() != null) {
            existing.setDescription(product.getDescription());
        }
        if (product.getPrice() != null) {
            existing.updatePrice(product.getPrice());
        }
        if (product.getPromotionalPrice() != null) {
            existing.setPromotionalPrice(product.getPromotionalPrice());
        }
        if (product.getCategory() != null) {
            existing.setCategory(product.getCategory());
        }
        if (product.getFeatures() != null) {
            existing.setFeatures(product.getFeatures());
        }
        if (product.getMetadata() != null) {
            existing.setMetadata(product.getMetadata());
        }
        if (product.getActive() != null) {
            existing.setActive(product.getActive());
        }
        
        existing.setUpdatedAt(LocalDateTime.now());
        return productPersistencePort.save(existing);
    }

    @Override
    public Product getProduct(Long id) {
        return productPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public List<Product> getAllProducts() {
        return productPersistencePort.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productPersistencePort.findAll().stream()
                .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> searchProducts(String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        return productPersistencePort.findAll().stream()
                .filter(p -> 
                    (p.getName() != null && p.getName().toLowerCase().contains(lowerSearchTerm)) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerSearchTerm)) ||
                    (p.getSku() != null && p.getSku().toLowerCase().contains(lowerSearchTerm))
                )
                .collect(Collectors.toList());
    }

    @Override
    public double[] calculateProductSimilarity(Long productId, double[][] weights) {
        Product product = getProduct(productId);
        return productDomainService.calculateProductScore(product, weights);
    }

    @Override
    public List<Product> getRecommendedProducts(Long productId, int limit) {
        Product product = getProduct(productId);
        
        if (product.getFeatures() == null || product.getFeatures().length == 0) {
            return List.of();
        }
        
        // Simple recommendation based on feature similarity
        // In a real system, this would use more sophisticated algorithms
        List<Product> allProducts = productPersistencePort.findAll();
        
        return allProducts.stream()
                .filter(p -> !p.getId().equals(productId) && p.getFeatures() != null && p.getFeatures().length == product.getFeatures().length)
                .sorted((p1, p2) -> {
                    double sim1 = calculateCosineSimilarity(product.getFeatures(), p1.getFeatures());
                    double sim2 = calculateCosineSimilarity(product.getFeatures(), p2.getFeatures());
                    return Double.compare(sim2, sim1); // Descending order
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    private double calculateCosineSimilarity(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denominator == 0.0 ? 0.0 : dotProduct / denominator;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProduct(id);
        productPersistencePort.deleteById(id);
    }
}

