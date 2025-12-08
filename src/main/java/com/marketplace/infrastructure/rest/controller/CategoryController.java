package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.usecases.CategoryService;
import com.marketplace.domain.model.Category;
import com.marketplace.infrastructure.rest.dto.request.CategoryRequestDto;
import com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto;
import com.marketplace.infrastructure.rest.mapper.CategoryRestMapper;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryRestMapper categoryRestMapper;

    @PostMapping
    @Operation(summary = "Create a new category or subcategory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto request) {
        Category domain = categoryRestMapper.toDomain(request);

        // If a parentId is provided, set a stub parent so the service can resolve it
        if (request.parentId() != null) {
            Category parent = new Category();
            parent.setId(request.parentId());
            domain.setParent(parent);
        }

        Category saved = categoryService.createCategory(domain);
        return new ResponseEntity<>(categoryRestMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        return ResponseEntity.ok(categoryRestMapper.toResponse(category));
    }

    @GetMapping
    @Operation(summary = "Get category tree")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryTree() {
        List<CategoryResponseDto> categories = categoryService.getCategoryTree().stream()
                .map(categoryRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}/hierarchy")
    @Operation(summary = "Get category hierarchy from root")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryHierarchy(@PathVariable Long id) {
        List<CategoryResponseDto> hierarchy = categoryService.getCategoryHierarchy(id).stream()
                .map(categoryRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(hierarchy);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDto request) {
        Category domain = categoryRestMapper.toDomain(request);

        // Map parentId to parent category stub for validation in the service layer
        if (request.parentId() != null) {
            Category parent = new Category();
            parent.setId(request.parentId());
            domain.setParent(parent);
        } else {
            domain.setParent(null);
        }

        Category updated = categoryService.updateCategory(id, domain);
        return ResponseEntity.ok(categoryRestMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
