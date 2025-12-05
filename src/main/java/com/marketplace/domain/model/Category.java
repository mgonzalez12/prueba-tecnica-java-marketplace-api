package com.marketplace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long id;
    private String name;
    private String description;
    private String code; // Unique category code
    private Boolean active;
    private Category parent;
    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addSubCategory(Category subCategory) {
        if (subCategories == null) {
            subCategories = new ArrayList<>();
        }
        this.subCategories.add(subCategory);
        subCategory.setParent(this);
    }

    /**
     * Gets the full hierarchy path from root to this category
     * Demonstrates recursive traversal
     */
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    /**
     * Gets all ancestor categories (recursive)
     */
    public List<Category> getAllAncestors() {
        List<Category> ancestors = new ArrayList<>();
        Category current = this.parent;
        while (current != null) {
            ancestors.add(current);
            current = current.getParent();
        }
        return ancestors;
    }

    /**
     * Gets all descendant categories recursively
     */
    public List<Category> getAllDescendants() {
        List<Category> descendants = new ArrayList<>();
        if (subCategories != null) {
            for (Category subCategory : subCategories) {
                descendants.add(subCategory);
                descendants.addAll(subCategory.getAllDescendants());
            }
        }
        return descendants;
    }

    /**
     * Checks if this category is a descendant of the given category (recursive check)
     */
    public boolean isDescendantOf(Category ancestor) {
        if (parent == null) {
            return false;
        }
        if (parent.equals(ancestor)) {
            return true;
        }
        return parent.isDescendantOf(ancestor);
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
