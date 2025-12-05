package com.marketplace.domain.service;

import com.marketplace.domain.model.Category;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoryDomainService {

    /**
     * Validates if adding a parent to a category creates a cycle.
     * Uses recursive approach to check the entire hierarchy.
     * 
     * @param category  The category being updated.
     * @param newParent The new parent category.
     * @throws IllegalArgumentException if a cycle is detected.
     */
    public void validateCategoryHierarchy(Category category, Category newParent) {
        if (newParent == null)
            return;

        // Check if newParent is the category itself
        if (newParent.getId() != null && category.getId() != null 
            && newParent.getId().equals(category.getId())) {
            throw new IllegalArgumentException("A category cannot be its own parent.");
        }

        // Recursive cycle detection: check if category is an ancestor of newParent
        if (isAncestor(category, newParent, new HashSet<>())) {
            throw new IllegalArgumentException(
                "Cycle detected: Category cannot be an ancestor of its own parent."
            );
        }
    }

    /**
     * Recursively checks if ancestor is in the hierarchy of category.
     * Uses visited set to prevent infinite loops in case of existing cycles.
     * 
     * @param ancestor The category to check if it's an ancestor
     * @param category The category to check from
     * @param visited Set of visited category IDs to prevent infinite recursion
     * @return true if ancestor is found in the hierarchy
     */
    private boolean isAncestor(Category ancestor, Category category, Set<Long> visited) {
        if (category == null || ancestor == null) {
            return false;
        }

        if (category.getId() != null) {
            if (visited.contains(category.getId())) {
                // Already visited, potential cycle in existing data
                return false;
            }
            visited.add(category.getId());

            if (category.getId().equals(ancestor.getId())) {
                return true;
            }
        }

        // Recursively check parent
        return isAncestor(ancestor, category.getParent(), visited);
    }

    /**
     * Gets the complete hierarchy tree starting from root categories.
     * Validates the entire tree structure for cycles and consistency.
     * 
     * @param categories All categories in the system
     * @return Root categories with their complete hierarchies
     * @throws IllegalStateException if cycles or inconsistencies are found
     */
    public List<Category> buildCategoryTree(List<Category> categories) {
        // Validate for cycles
        validateTreeStructure(categories);
        
        // Find root categories (categories without parents)
        return categories.stream()
            .filter(cat -> cat.getParent() == null)
            .toList();
    }

    /**
     * Validates the entire category tree structure for cycles and consistency.
     * Uses recursive depth-first search approach.
     * 
     * @param categories All categories to validate
     * @throws IllegalStateException if cycles or inconsistencies are found
     */
    public void validateTreeStructure(List<Category> categories) {
        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();

        for (Category category : categories) {
            if (category.getId() != null && !visited.contains(category.getId())) {
                validateCategoryRecursive(category, visited, recursionStack);
            }
        }
    }

    /**
     * Recursively validates a category and its ancestors for cycles.
     * 
     * @param category The category to validate
     * @param visited Set of all visited categories
     * @param recursionStack Set of categories in current recursion path
     * @throws IllegalStateException if a cycle is detected
     */
    private void validateCategoryRecursive(Category category, Set<Long> visited, Set<Long> recursionStack) {
        if (category.getId() == null) {
            return;
        }

        if (recursionStack.contains(category.getId())) {
            throw new IllegalStateException(
                String.format("Cycle detected in category hierarchy at category ID: %d", category.getId())
            );
        }

        if (visited.contains(category.getId())) {
            return; // Already validated
        }

        recursionStack.add(category.getId());
        
        // Recursively validate parent
        if (category.getParent() != null) {
            validateCategoryRecursive(category.getParent(), visited, recursionStack);
        }

        recursionStack.remove(category.getId());
        visited.add(category.getId());
    }

    /**
     * Gets the full hierarchy path from root to the given category.
     * Demonstrates recursive traversal.
     * 
     * @param category The category to get the path for
     * @return List of categories from root to the given category
     */
    public List<Category> getCategoryPath(Category category) {
        if (category == null) {
            return List.of();
        }
        
        List<Category> path = getCategoryPathRecursive(category, new java.util.ArrayList<>());
        java.util.Collections.reverse(path);
        return path;
    }

    /**
     * Recursively builds the path from category to root.
     */
    private List<Category> getCategoryPathRecursive(Category category, List<Category> path) {
        if (category == null) {
            return path;
        }
        path.add(category);
        return getCategoryPathRecursive(category.getParent(), path);
    }
}
