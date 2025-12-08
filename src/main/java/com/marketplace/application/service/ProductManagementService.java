package com.marketplace.application.service;

import com.marketplace.application.usecases.InventoryService;
import com.marketplace.application.usecases.ProductService;
import com.marketplace.domain.model.Product;
import com.marketplace.domain.port.ProductPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProductManagementService implements ProductService {

    private final ProductPersistencePort productPersistencePort;
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

        // Actualización funcional de campos simples
        Optional.ofNullable(product.getName()).ifPresent(existing::setName);
        Optional.ofNullable(product.getDescription()).ifPresent(existing::setDescription);
        Optional.ofNullable(product.getPrice()).ifPresent(existing::updatePrice);
        Optional.ofNullable(product.getPromotionalPrice()).ifPresent(existing::setPromotionalPrice);
        Optional.ofNullable(product.getCategory()).ifPresent(existing::setCategory);
        Optional.ofNullable(product.getFeatures()).ifPresent(existing::setFeatures);
        Optional.ofNullable(product.getMetadata()).ifPresent(existing::setMetadata);
        Optional.ofNullable(product.getActive()).ifPresent(existing::setActive);

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
            return calculateProductScore(product, weights);
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

        double dotProduct = IntStream.range(0, vec1.length)
                .mapToDouble(i -> vec1[i] * vec2[i])
                .sum();

        double norm1 = Arrays.stream(vec1)
                .map(v -> v * v)
                .sum();

        double norm2 = Arrays.stream(vec2)
                .map(v -> v * v)
                .sum();

        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denominator == 0.0 ? 0.0 : dotProduct / denominator;
    }

    /**
     * Simula un cálculo complejo de matriz para similitud o clasificación de productos.
     * Multiplica el vector de características del producto por una matriz de pesos.
     */
    private double[] calculateProductScore(Product product, double[][] weights) {
        double[] features = product.getFeatures();
        if (features == null || weights == null) {
            throw new IllegalArgumentException("Features and weights cannot be null");
        }

        int featureLength = features.length;

        // Validar que todas las filas tengan el mismo número de columnas
        int cols = weights[0].length;
        if (cols != featureLength ||
                Arrays.stream(weights).anyMatch(row -> row.length != cols)) {
            throw new IllegalArgumentException("Matrix columns must match feature vector length");
        }

        // Multiplicación Matriz-Vector en estilo funcional
        return Arrays.stream(weights)
                .mapToDouble(row ->
                        IntStream.range(0, featureLength)
                                .mapToDouble(j -> row[j] * features[j])
                                .sum()
                )
                .toArray();
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProduct(id);
        productPersistencePort.deleteById(id);
    }
}

