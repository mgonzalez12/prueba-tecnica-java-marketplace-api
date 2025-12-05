package com.marketplace.application.service;

import com.marketplace.domain.model.Customer;
import com.marketplace.domain.port.CustomerPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Management Service Tests")
class CustomerManagementServiceTest {

    @Mock
    private CustomerPersistencePort customerPersistencePort;

    @InjectMocks
    private CustomerManagementService customerManagementService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setBirthDate(LocalDate.of(1990, 1, 15));
    }

    @Test
    @DisplayName("Should register customer successfully")
    void shouldRegisterCustomerSuccessfully() {
        // Given
        when(customerPersistencePort.save(any(Customer.class))).thenReturn(customer);

        // When
        Customer result = customerManagementService.registerCustomer(customer);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRegistrationDate());
        assertNotNull(result.getLastActivityDate());
        assertEquals(0, result.getTotalOrders());
        verify(customerPersistencePort, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should update customer successfully")
    void shouldUpdateCustomerSuccessfully() {
        // Given
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setFirstName("John");
        existingCustomer.setLastName("Doe");
        
        Customer updatedData = new Customer();
        updatedData.setFirstName("Jane");
        updatedData.setPhone("+1234567890");
        
        when(customerPersistencePort.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerPersistencePort.save(any(Customer.class))).thenReturn(existingCustomer);

        // When
        Customer result = customerManagementService.updateCustomer(1L, updatedData);

        // Then
        assertNotNull(result);
        verify(customerPersistencePort, times(1)).findById(1L);
        verify(customerPersistencePort, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        when(customerPersistencePort.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
            customerManagementService.getCustomer(999L)
        );
    }

    @Test
    @DisplayName("Should record customer activity")
    void shouldRecordCustomerActivity() {
        // Given
        customer.setTotalOrders(5);
        when(customerPersistencePort.findById(1L)).thenReturn(Optional.of(customer));
        when(customerPersistencePort.save(any(Customer.class))).thenReturn(customer);

        // When
        Customer result = customerManagementService.recordCustomerActivity(1L);

        // Then
        assertEquals(6, result.getTotalOrders());
        assertNotNull(result.getLastActivityDate());
        verify(customerPersistencePort, times(1)).save(any(Customer.class));
    }
}

