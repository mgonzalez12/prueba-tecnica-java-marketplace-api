package com.marketplace.domain.port;

import com.marketplace.domain.model.Product;
import java.util.Optional;
import java.util.List;

public interface ProductPersistencePort {
    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    Optional<Product> findBySku(String sku);

    void deleteById(Long id);
}
