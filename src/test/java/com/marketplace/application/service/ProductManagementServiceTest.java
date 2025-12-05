package com.marketplace.application.service;

import com.marketplace.domain.model.Product;
import com.marketplace.domain.port.ProductPersistencePort;
import com.marketplace.domain.service.ProductDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Management Service Tests")
class ProductManagementServiceTest {

    @Mock
    private ProductPersistencePort productPersistencePort;

    @Mock
    private ProductDomainService productDomainService;

    @InjectMocks
    private ProductManagementService productManagementService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        // Given
        when(productPersistencePort.findBySku("SKU-001")).thenReturn(Optional.empty());
        when(productPersistencePort.save(any(Product.class))).thenReturn(product);

        // When
        Product result = productManagementService.createProduct(product);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getActive());
        verify(productPersistencePort, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when SKU already exists")
    void shouldThrowExceptionWhenSkuAlreadyExists() {
        // Given
        when(productPersistencePort.findBySku("SKU-001")).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            productManagementService.createProduct(product)
        );
    }

    @Test
    @DisplayName("Should search products by term")
    void shouldSearchProductsByTerm() {
        // Given
        Product product1 = Product.builder().name("Laptop").build();
        Product product2 = Product.builder().name("Mouse").build();
        Product product3 = Product.builder().name("Keyboard").build();
        
        when(productPersistencePort.findAll()).thenReturn(List.of(product1, product2, product3));

        // When
        List<Product> results = productManagementService.searchProducts("lap");

        // Then
        assertEquals(1, results.size());
        assertEquals("Laptop", results.get(0).getName());
    }

    @Test
    @DisplayName("Should calculate product similarity")
    void shouldCalculateProductSimilarity() {
        // Given
        double[][] weights = {{0.5, 0.3, 0.2}};
        double[] expectedResult = {1.4};
        
        when(productPersistencePort.findById(1L)).thenReturn(Optional.of(product));
        when(productDomainService.calculateProductScore(product, weights)).thenReturn(expectedResult);

        // When
        double[] result = productManagementService.calculateProductSimilarity(1L, weights);

        // Then
        assertArrayEquals(expectedResult, result);
        verify(productDomainService, times(1)).calculateProductScore(product, weights);
    }
}

