package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.usecases.CategoryService;
import com.marketplace.application.usecases.ProductService;
import com.marketplace.domain.model.Category;
import com.marketplace.domain.model.Product;
import com.marketplace.infrastructure.rest.dto.request.ProductRequestDto;
import com.marketplace.infrastructure.rest.dto.response.ProductResponseDto;
import com.marketplace.infrastructure.rest.mapper.ProductRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductRestMapper productRestMapper;

    @PostMapping
    @Operation(summary = "Create a new product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto request) {
        Product product = buildDomainProduct(request);
        Product created = productService.createProduct(product);
        return new ResponseEntity<>(productRestMapper.toResponse(created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return ResponseEntity.ok(productRestMapper.toResponse(product));
    }

    @GetMapping
    @Operation(summary = "Get all products")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts().stream()
                .map(productRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponseDto> products = productService.getProductsByCategory(categoryId).stream()
                .map(productRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<ProductResponseDto>> searchProducts(@RequestParam String q) {
        List<ProductResponseDto> products = productService.searchProducts(q).stream()
                .map(productRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}/similarity")
    @Operation(summary = "Calculate product similarity using matrix operations", hidden = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<double[]> calculateSimilarity(
            @PathVariable Long id,
            @RequestBody double[][] weights) {
        return ResponseEntity.ok(productService.calculateProductSimilarity(id, weights));
    }

    @GetMapping("/{id}/recommendations")
    @Operation(summary = "Get product recommendations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDto>> getRecommendations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductResponseDto> products = productService.getRecommendedProducts(id, limit).stream()
                .map(productRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto request) {
        Product product = buildDomainProduct(request);
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(productRestMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Builds the domain Product from the REST request, resolving the category by ID.
     */
    private Product buildDomainProduct(ProductRequestDto request) {
        Product product = productRestMapper.toDomain(request);

        if (request.categoryId() != null) {
            Category category = categoryService.getCategory(request.categoryId());
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        return product;
    }
}

