package com.marketplace.infrastructure.adapter;

import com.marketplace.domain.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CategorySpringJpaAdapterIntegrationTest.MapperConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Category Spring JPA Adapter Integration Tests")
class CategorySpringJpaAdapterIntegrationTest {

    @Autowired
    private CategorySpringJpaAdapter categoryAdapter;

    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        rootCategory = Category.builder()
                .name("Electronics")
                .description("Electronic products")
                .build();
        
        childCategory = Category.builder()
                .name("Laptops")
                .description("Laptop computers")
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve category")
    void shouldSaveAndRetrieveCategory() {
        // When
        Category saved = categoryAdapter.save(rootCategory);
        Optional<Category> found = categoryAdapter.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Electronics", found.get().getName());
    }

    @Test
    @DisplayName("Should save category with parent")
    void shouldSaveCategoryWithParent() {
        // Given
        Category savedParent = categoryAdapter.save(rootCategory);
        childCategory.setParent(savedParent);

        // When
        Category savedChild = categoryAdapter.save(childCategory);
        Optional<Category> found = categoryAdapter.findById(savedChild.getId());

        // Then
        assertTrue(found.isPresent());
        assertNotNull(found.get().getParent());
        assertEquals(savedParent.getId(), found.get().getParent().getId());
    }

    @Test
    @DisplayName("Should find all categories")
    void shouldFindAllCategories() {
        // Given
        categoryAdapter.save(rootCategory);
        categoryAdapter.save(childCategory);

        // When
        List<Category> allCategories = categoryAdapter.findAll();

        // Then
        assertEquals(2, allCategories.size());
    }

    @Test
    @DisplayName("Should find categories by parent ID")
    void shouldFindCategoriesByParentId() {
        // Given
        Category savedParent = categoryAdapter.save(rootCategory);
        childCategory.setParent(savedParent);
        categoryAdapter.save(childCategory);

        // When
        List<Category> children = categoryAdapter.findByParentId(savedParent.getId());

        // Then
        assertEquals(1, children.size());
        assertEquals("Laptops", children.get(0).getName());
    }
    @TestConfiguration
    static class MapperConfig {
        @Bean
        CategorySpringJpaAdapter categorySpringJpaAdapter(com.marketplace.infrastructure.adapter.mapper.CategoryDboMapper mapper,
                                                          com.marketplace.infrastructure.adapter.repository.CategoryJpaRepository repository) {
            return new CategorySpringJpaAdapter(repository, mapper);
        }

        @Bean
        com.marketplace.infrastructure.adapter.mapper.CategoryDboMapper categoryDboMapper() {
            return org.mapstruct.factory.Mappers.getMapper(com.marketplace.infrastructure.adapter.mapper.CategoryDboMapper.class);
        }
    }
}

