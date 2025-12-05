package com.marketplace.infrastructure.adapter;

import com.marketplace.domain.model.Category;
import com.marketplace.domain.port.CategoryPersistencePort;
import com.marketplace.infrastructure.adapter.entity.CategoryEntity;
import com.marketplace.infrastructure.adapter.mapper.CategoryDboMapper;
import com.marketplace.infrastructure.adapter.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorySpringJpaAdapter implements CategoryPersistencePort {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryDboMapper categoryDboMapper;

    @Override
    public Category save(Category category) {
        CategoryEntity entity = categoryDboMapper.toEntity(category);
        // If parent is set in domain, we need to fetch/set it in entity
        if (category.getParent() != null && category.getParent().getId() != null) {
            CategoryEntity parentEntity = categoryJpaRepository.findById(category.getParent().getId())
                    .orElse(null);
            entity.setParent(parentEntity);
        }

        CategoryEntity saved = categoryJpaRepository.save(entity);
        return categoryDboMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id)
                .map(categoryDboMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
                .map(categoryDboMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        categoryJpaRepository.deleteById(id);
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        return categoryJpaRepository.findByParentId(parentId).stream()
                .map(categoryDboMapper::toDomain)
                .collect(Collectors.toList());
    }
}
