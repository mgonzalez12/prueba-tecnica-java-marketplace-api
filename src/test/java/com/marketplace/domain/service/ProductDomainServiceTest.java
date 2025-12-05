package com.marketplace.domain.service;

import com.marketplace.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Domain Service Tests")
class ProductDomainServiceTest {

    private ProductDomainService productDomainService;

    @BeforeEach
    void setUp() {
        productDomainService = new ProductDomainService();
    }

    @Test
    @DisplayName("Should calculate product score using matrix multiplication")
    void shouldCalculateProductScore() {
        // Given
        Product product = Product.builder()
                .features(new double[]{1.0, 2.0, 3.0})
                .build();
        
        double[][] weights = {
            {0.5, 0.3, 0.2},
            {0.2, 0.4, 0.4},
            {0.1, 0.1, 0.8}
        };

        // When
        double[] result = productDomainService.calculateProductScore(product, weights);

        // Then
        assertEquals(3, result.length);
        assertEquals(1.7, result[0], 0.01); // 0.5*1 + 0.3*2 + 0.2*3
        assertEquals(2.2, result[1], 0.01); // 0.2*1 + 0.4*2 + 0.4*3
        assertEquals(2.7, result[2], 0.01); // 0.1*1 + 0.1*2 + 0.8*3
    }

    @Test
    @DisplayName("Should throw exception when features are null")
    void shouldThrowExceptionWhenFeaturesAreNull() {
        // Given
        Product product = Product.builder().build();
        double[][] weights = {{1.0}};

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            productDomainService.calculateProductScore(product, weights)
        );
    }

    @Test
    @DisplayName("Should throw exception when matrix dimensions don't match")
    void shouldThrowExceptionWhenMatrixDimensionsDontMatch() {
        // Given
        Product product = Product.builder()
                .features(new double[]{1.0, 2.0})
                .build();
        
        double[][] weights = {
            {0.5, 0.3, 0.2} // 3 columns but features has 2 elements
        };

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            productDomainService.calculateProductScore(product, weights)
        );
    }
}

