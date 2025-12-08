package com.marketplace.application.service;

import static com.marketplace.domain.constant.Constants.PARENT_CATEGORY_NOT_FOUND;
import static com.marketplace.domain.constant.Constants.CATEGORY_NOT_FOUND_WITH_ID;
import static com.marketplace.domain.constant.Constants.SUBCATEGORIES_FIRST;

import com.marketplace.application.usecases.CategoryService;
import com.marketplace.domain.model.Category;
import com.marketplace.domain.port.CategoryPersistencePort;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CategoryManagementService implements CategoryService {

    private final CategoryPersistencePort categoryPersistencePort;

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // Validate hierarchy cycles if parent is present
        Optional.ofNullable(category.getParent())
                .map(Category::getId)
                .ifPresent(parentId -> {
                    Category parent = categoryPersistencePort.findById(parentId)
                            .orElseThrow(() -> new IllegalArgumentException(PARENT_CATEGORY_NOT_FOUND));
                        validateCategoryHierarchy(category, parent);
                    category.setParent(parent);
                });

        // Set timestamps
        category.setCreatedAt(
                Optional.ofNullable(category.getCreatedAt())
                        .orElseGet(LocalDateTime::now)
        );
        category.setUpdatedAt(LocalDateTime.now());

        // Set active by default
        category.setActive(
                Optional.ofNullable(category.getActive())
                        .orElse(true)
        );
        
        return categoryPersistencePort.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existing = getCategory(id);

        // Functional updates for simple fields
        Optional.ofNullable(category.getName()).ifPresent(existing::setName);
        Optional.ofNullable(category.getDescription()).ifPresent(existing::setDescription);
        Optional.ofNullable(category.getCode()).ifPresent(existing::setCode);
        Optional.ofNullable(category.getActive()).ifPresent(existing::setActive);

        // Parent update with validation (avoid unnecessary DB hits if parent doesn't change)
        Optional<Long> newParentIdOpt = Optional.ofNullable(category.getParent())
                .map(Category::getId);

        Long currentParentId = Optional.ofNullable(existing.getParent())
                .map(Category::getId)
                .orElse(null);

        newParentIdOpt
                .filter(newParentId -> !Objects.equals(newParentId, currentParentId))
                .ifPresent(newParentId -> {
                    Category newParent = categoryPersistencePort.findById(newParentId)
                            .orElseThrow(() -> new IllegalArgumentException(PARENT_CATEGORY_NOT_FOUND));
                    validateCategoryHierarchy(existing, newParent);
                    existing.setParent(newParent);
                });

        // Removing parent when explicitly set to null
        if (category.getParent() == null && existing.getParent() != null) {
            existing.setParent(null);
        }

        existing.setUpdatedAt(LocalDateTime.now());
        return categoryPersistencePort.save(existing);
    }

    @Override
    public Category getCategory(Long id) {
        return categoryPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryPersistencePort.findAll();
    }

    @Override
    public List<Category> getCategoryTree() {
        // Validate and build tree structure
        List<Category> allCategories = categoryPersistencePort.findAll();
            validateTreeStructure(allCategories);
            return buildCategoryTree(allCategories);
    }

    @Override
    public List<Category> getCategoryHierarchy(Long categoryId) {
        Category category = getCategory(categoryId);
        // Get full path from root to this category
            return getCategoryPath(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategory(id);
        
        // Check if category has subcategories
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            throw new IllegalStateException(SUBCATEGORIES_FIRST);
        }
        
        categoryPersistencePort.deleteById(id);
    }

        // ===================== LÓGICA DE DOMINIO (antes en CategoryDomainService) =====================

        /**
         * Valida si al asignar un padre a una categoría se crea un ciclo.
         * Implementado con estilo funcional (Optional + lambdas).
         */
        private void validateCategoryHierarchy(Category category, Category newParent) {
            Optional.ofNullable(newParent).ifPresent(parent -> {
                boolean isSelfParent = Optional.ofNullable(parent.getId())
                        .filter(parentId -> category.getId() != null && parentId.equals(category.getId()))
                        .isPresent();

                if (isSelfParent) {
                    throw new IllegalArgumentException("A category cannot be its own parent.");
                }

                // Detección recursiva de ciclos: comprobar si category es ancestro de parent
                if (isAncestor(category, parent, new HashSet<>())) {
                    throw new IllegalArgumentException(
                            "Cycle detected: Category cannot be an ancestor of its own parent."
                    );
                }
            });
        }

        /**
         * Comprueba recursivamente si ancestor está en la jerarquía de category.
         * Implementado con switch expression (Java 21).
         */
        private boolean isAncestor(Category ancestor, Category category, Set<Long> visited) {
            if (ancestor == null) {
                return false;
            }

            return switch (category) {
                case null -> false;
                default -> {
                    Long categoryId = category.getId();

                    if (categoryId == null || visited.contains(categoryId)) {
                        // Sin id o ya visitado → evitamos ciclos / datos inconsistentes
                        yield false;
                    }

                    visited.add(categoryId);

                    if (categoryId.equals(ancestor.getId())) {
                        yield true;
                    }

                    // Comprobar recursivamente el padre
                    yield isAncestor(ancestor, category.getParent(), visited);
                }
            };
        }

        /**
         * Construye el árbol de categorías partiendo de las raíces.
         */
        private List<Category> buildCategoryTree(List<Category> categories) {
            // Validar ciclos
            validateTreeStructure(categories);

            // Raíces = categorías sin padre
            return categories.stream()
                    .filter(cat -> cat.getParent() == null)
                    .toList();
        }

        /**
         * Valida la estructura completa del árbol de categorías.
         */
        private void validateTreeStructure(List<Category> categories) {
            Set<Long> visited = new HashSet<>();
            Set<Long> recursionStack = new HashSet<>();

            for (Category category : categories) {
                if (category.getId() != null && !visited.contains(category.getId())) {
                    validateCategoryRecursive(category, visited, recursionStack);
                }
            }
        }

        /**
         * Valida recursivamente una categoría y sus ancestros para detectar ciclos.
         * Implementado con switch expression (Java 21).
         */
        private void validateCategoryRecursive(Category category, Set<Long> visited, Set<Long> recursionStack) {
            switch (category) {
                case null -> {
                    // Nada que validar
                }
                default -> {
                    Long id = category.getId();
                    if (id == null) {
                        return;
                    }

                    if (recursionStack.contains(id)) {
                        throw new IllegalStateException(
                                String.format("Cycle detected in category hierarchy at category ID: %d", id)
                        );
                    }

                    if (visited.contains(id)) {
                        return; // Ya validada
                    }

                    recursionStack.add(id);

                    // Validar recursivamente el padre
                    Optional.ofNullable(category.getParent())
                            .ifPresent(parent -> validateCategoryRecursive(parent, visited, recursionStack));

                    recursionStack.remove(id);
                    visited.add(id);
                }
            }
        }

        /**
         * Devuelve la ruta completa desde la raíz hasta la categoría dada.
         */
        private List<Category> getCategoryPath(Category category) {
            if (category == null) {
                return List.of();
            }

            List<Category> path = getCategoryPathRecursive(category, new java.util.ArrayList<>());
            java.util.Collections.reverse(path);
            return path;
        }

        /**
         * Construye recursivamente el path desde la categoría hasta la raíz.
         */
        private List<Category> getCategoryPathRecursive(Category category, List<Category> path) {
            if (category == null) {
                return path;
            }
            path.add(category);
            return getCategoryPathRecursive(category.getParent(), path);
        }
}
