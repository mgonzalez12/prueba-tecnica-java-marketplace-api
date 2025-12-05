package com.marketplace.application.usecases;

import com.marketplace.domain.model.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);

    Category updateCategory(Long id, Category category);

    Category getCategory(Long id);

    List<Category> getAllCategories();

    List<Category> getCategoryTree();

    List<Category> getCategoryHierarchy(Long categoryId);

    void deleteCategory(Long id);
}
