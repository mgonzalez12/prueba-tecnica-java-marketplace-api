package com.marketplace.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.application.usecases.CustomerService;
import com.marketplace.domain.model.Customer;
import com.marketplace.infrastructure.rest.dto.CustomerRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@DisplayName("Customer Controller Functional Tests")
class CustomerControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("deprecation")
    @MockBean
    private CustomerService customerService;

    @SuppressWarnings("deprecation")
    @MockBean
    private com.marketplace.infrastructure.rest.mapper.CustomerRestMapper customerRestMapper;

    private Customer customer;
    private CustomerRequestDto customerRequestDto;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setBirthDate(LocalDate.of(1990, 1, 15));
        customer.setRegistrationDate(new Date());
        customer.setLastActivityDate(LocalDate.now());

        customerRequestDto = new CustomerRequestDto(
            "John",
            "Doe",
            "john.doe@example.com",
            LocalDate.of(1990, 1, 15),
            null,
            null,
            null
        );
    }

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomerSuccessfully() throws Exception {
        // Given
        when(customerRestMapper.toDomain(any(CustomerRequestDto.class))).thenReturn(customer);
        when(customerService.registerCustomer(any(Customer.class))).thenReturn(customer);
        when(customerRestMapper.toResponse(any(Customer.class)))
            .thenReturn(new com.marketplace.infrastructure.rest.dto.CustomerResponseDto(
                1L, "John", "Doe", "john.doe@example.com", "ACTIVE", 34,
                LocalDate.of(1990, 1, 15), "2024-01-01T00:00:00Z", LocalDate.now(), null, null, null, 0, null, 0L, 0
            ));

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should get customer by ID")
    void shouldGetCustomerById() throws Exception {
        // Given
        when(customerService.getCustomer(1L)).thenReturn(customer);
        when(customerRestMapper.toResponse(any(Customer.class)))
            .thenReturn(new com.marketplace.infrastructure.rest.dto.CustomerResponseDto(
                1L, "John", "Doe", "john.doe@example.com", "ACTIVE", 34,
                LocalDate.of(1990, 1, 15), "2024-01-01T00:00:00Z", LocalDate.now(), null, null, null, 0, null, 0L, 0
            ));

        // When & Then
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("Should get all customers")
    void shouldGetAllCustomers() throws Exception {
        // Given
        when(customerService.getAllCustomers()).thenReturn(List.of(customer));
        when(customerRestMapper.toResponse(any(Customer.class)))
            .thenReturn(new com.marketplace.infrastructure.rest.dto.CustomerResponseDto(
                1L, "John", "Doe", "john.doe@example.com", "ACTIVE", 34,
                LocalDate.of(1990, 1, 15), "2024-01-01T00:00:00Z", LocalDate.now(), null, null, null, 0, null, 0L, 0
            ));

        // When & Then
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given
        CustomerRequestDto invalidDto = new CustomerRequestDto(
            "", // Empty first name
            "Doe",
            "invalid-email", // Invalid email
            null,
            null,
            null,
            null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}

