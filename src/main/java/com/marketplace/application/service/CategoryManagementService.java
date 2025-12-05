package com.marketplace.application.service;

import com.marketplace.application.usecases.CategoryService;
import com.marketplace.domain.model.Category;
import com.marketplace.domain.port.CategoryPersistencePort;
import com.marketplace.domain.service.CategoryDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryManagementService implements CategoryService {

    private final CategoryPersistencePort categoryPersistencePort;
    private final CategoryDomainService categoryDomainService;

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // Validate hierarchy cycles if parent is present
        if (category.getParent() != null && category.getParent().getId() != null) {
            // Fetch full parent to check hierarchy
            Category parent = categoryPersistencePort.findById(category.getParent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));

            categoryDomainService.validateCategoryHierarchy(category, parent);
            category.setParent(parent);
        }
        
        // Set timestamps
        if (category.getCreatedAt() == null) {
            category.setCreatedAt(LocalDateTime.now());
        }
        category.setUpdatedAt(LocalDateTime.now());
        
        // Set active by default
        if (category.getActive() == null) {
            category.setActive(true);
        }
        
        return categoryPersistencePort.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existing = getCategory(id);
        
        if (category.getName() != null) {
            existing.setName(category.getName());
        }
        if (category.getDescription() != null) {
            existing.setDescription(category.getDescription());
        }
        if (category.getCode() != null) {
            existing.setCode(category.getCode());
        }
        if (category.getActive() != null) {
            existing.setActive(category.getActive());
        }
        
        // Handle parent update with validation
        if (category.getParent() != null) {
            if (category.getParent().getId() != null && 
                !category.getParent().getId().equals(existing.getParent() != null ? existing.getParent().getId() : null)) {
                Category newParent = categoryPersistencePort.findById(category.getParent().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
                categoryDomainService.validateCategoryHierarchy(existing, newParent);
                existing.setParent(newParent);
            }
        } else if (category.getParent() == null && existing.getParent() != null) {
            // Removing parent
            existing.setParent(null);
        }
        
        existing.setUpdatedAt(LocalDateTime.now());
        return categoryPersistencePort.save(existing);
    }

    @Override
    public Category getCategory(Long id) {
        return categoryPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryPersistencePort.findAll();
    }

    @Override
    public List<Category> getCategoryTree() {
        // Validate and build tree structure
        List<Category> allCategories = categoryPersistencePort.findAll();
        categoryDomainService.validateTreeStructure(allCategories);
        return categoryDomainService.buildCategoryTree(allCategories);
    }

    @Override
    public List<Category> getCategoryHierarchy(Long categoryId) {
        Category category = getCategory(categoryId);
        // Get full path from root to this category
        return categoryDomainService.getCategoryPath(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategory(id);
        
        // Check if category has subcategories
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories. Please delete or move subcategories first.");
        }
        
        categoryPersistencePort.deleteById(id);
    }
}
