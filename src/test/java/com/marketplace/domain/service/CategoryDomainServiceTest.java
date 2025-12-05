package com.marketplace.domain.service;

import com.marketplace.domain.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Category Domain Service Tests")
class CategoryDomainServiceTest {

    private CategoryDomainService categoryDomainService;

    @BeforeEach
    void setUp() {
        categoryDomainService = new CategoryDomainService();
    }

    @Test
    @DisplayName("Should validate category hierarchy without cycles")
    void shouldValidateCategoryHierarchyWithoutCycles() {
        // Given
        Category parent = Category.builder()
                .id(1L)
                .name("Parent")
                .build();

        Category child = Category.builder()
                .id(2L)
                .name("Child")
                .build();

        // When & Then
        assertDoesNotThrow(() -> 
            categoryDomainService.validateCategoryHierarchy(child, parent)
        );
    }

    @Test
    @DisplayName("Should throw exception when category is its own parent")
    void shouldThrowExceptionWhenCategoryIsItsOwnParent() {
        // Given
        Category category = Category.builder()
                .id(1L)
                .name("Category")
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> categoryDomainService.validateCategoryHierarchy(category, category)
        );
        
        assertTrue(exception.getMessage().contains("cannot be its own parent"));
    }

    @Test
    @DisplayName("Should throw exception when cycle is detected")
    void shouldThrowExceptionWhenCycleIsDetected() {
        // Given
        Category grandparent = Category.builder()
                .id(1L)
                .name("Grandparent")
                .build();
        
        Category parent = Category.builder()
                .id(2L)
                .name("Parent")
                .parent(grandparent)
                .build();
        
        Category child = Category.builder()
                .id(3L)
                .name("Child")
                .build();

        // When & Then - Trying to make grandparent a child of child creates a cycle
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> categoryDomainService.validateCategoryHierarchy(grandparent, parent)
        );
        
        assertTrue(exception.getMessage().contains("Cycle detected"));
    }

    @Test
    @DisplayName("Should build category tree correctly")
    void shouldBuildCategoryTreeCorrectly() {
        // Given
        Category root1 = Category.builder().id(1L).name("Root1").build();
        Category root2 = Category.builder().id(2L).name("Root2").build();
        Category child1 = Category.builder().id(3L).name("Child1").parent(root1).build();
        
        List<Category> categories = List.of(root1, root2, child1);

        // When
        List<Category> tree = categoryDomainService.buildCategoryTree(categories);

        // Then
        assertEquals(2, tree.size());
        assertTrue(tree.contains(root1));
        assertTrue(tree.contains(root2));
    }

    @Test
    @DisplayName("Should get category path from root to category")
    void shouldGetCategoryPath() {
        // Given
        Category root = Category.builder().id(1L).name("Root").build();
        Category child = Category.builder().id(2L).name("Child").parent(root).build();
        Category grandchild = Category.builder().id(3L).name("Grandchild").parent(child).build();

        // When
        List<Category> path = categoryDomainService.getCategoryPath(grandchild);

        // Then
        assertEquals(3, path.size());
        assertEquals(root, path.get(0));
        assertEquals(child, path.get(1));
        assertEquals(grandchild, path.get(2));
    }
}

