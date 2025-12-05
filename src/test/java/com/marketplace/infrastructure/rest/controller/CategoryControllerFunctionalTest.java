package com.marketplace.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.application.usecases.CategoryService;
import com.marketplace.domain.model.Category;
import com.marketplace.infrastructure.rest.dto.request.CategoryRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("Category Controller Functional Tests")
class CategoryControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("deprecation")
    @MockBean
    private CategoryService categoryService;

    @SuppressWarnings("deprecation")
    @MockBean
    private com.marketplace.infrastructure.rest.mapper.CategoryRestMapper categoryRestMapper;

    private Category category;
    private CategoryRequestDto categoryRequestDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic products")
                .build();

        categoryRequestDto = new CategoryRequestDto(
            "Electronics",
            "Electronic products",
            "ELEC",
            null,
            true
        );
    }

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategorySuccessfully() throws Exception {
        // Given
        when(categoryRestMapper.toDomain(any(CategoryRequestDto.class))).thenReturn(category);
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);
        when(categoryRestMapper.toResponse(any(Category.class)))
            .thenReturn(new com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto(
                1L, "Electronics", "Electronic products", "ELEC", true,
                null, null, null, null, null, null
            ));

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("Should get category by ID")
    void shouldGetCategoryById() throws Exception {
        // Given
        when(categoryService.getCategory(1L)).thenReturn(category);
        when(categoryRestMapper.toResponse(any(Category.class)))
            .thenReturn(new com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto(
                1L, "Electronics", "Electronic products", "ELEC", true,
                null, null, null, null, null, null
            ));

        // When & Then
        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("Should get category tree")
    void shouldGetCategoryTree() throws Exception {
        // Given
        when(categoryService.getCategoryTree()).thenReturn(List.of(category));
        when(categoryRestMapper.toResponse(any(Category.class)))
            .thenReturn(new com.marketplace.infrastructure.rest.dto.response.CategoryResponseDto(
                1L, "Electronics", "Electronic products", "ELEC", true,
                null, null, null, null, null, null
            ));

        // When & Then
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }
}

