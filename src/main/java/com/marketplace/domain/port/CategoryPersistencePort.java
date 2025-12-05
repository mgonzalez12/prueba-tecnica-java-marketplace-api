package com.marketplace.domain.port;

import com.marketplace.domain.model.Category;
import java.util.Optional;
import java.util.List;

public interface CategoryPersistencePort {
    Category save(Category category);

    Optional<Category> findById(Long id);

    List<Category> findAll();

    void deleteById(Long id);

    List<Category> findByParentId(Long parentId);
}
