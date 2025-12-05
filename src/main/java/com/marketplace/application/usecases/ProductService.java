package com.marketplace.application.usecases;

import com.marketplace.domain.model.Product;
import java.util.List;

public interface ProductService {
    Product createProduct(Product product);

    Product updateProduct(Long id, Product product);

    Product getProduct(Long id);

    List<Product> getAllProducts();

    List<Product> getProductsByCategory(Long categoryId);

    List<Product> searchProducts(String searchTerm);

    double[] calculateProductSimilarity(Long productId, double[][] weights);

    List<Product> getRecommendedProducts(Long productId, int limit);

    void deleteProduct(Long id);
}
