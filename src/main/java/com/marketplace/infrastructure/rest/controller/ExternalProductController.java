package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.service.ExternalProductManagementService;
import com.marketplace.domain.model.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/external-products")
@RequiredArgsConstructor
@Tag(name = "External Products", description = "External product integration APIs")
@SecurityRequirement(name = "bearerAuth")
public class ExternalProductController {

    private final ExternalProductManagementService externalProductService;

    @PostMapping("/sync")
    @Operation(summary = "Fetch and synchronize external products from external API")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<List<Product>> synchronizeProducts() {
        return externalProductService.fetchAndSynchronizeProducts();
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Fetch external product by external ID (without persisting it)")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<Product> getExternalProduct(@PathVariable String externalId) {
        return externalProductService.fetchExternalProduct(externalId);
    }

    @PostMapping("/create")
    @Operation(summary = "Create a product directly in the external Fake Store API (no local persistence)")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<Product> createExternalProduct(@RequestBody Product product) {
        return externalProductService.createExternalProduct(product);
    }
}

