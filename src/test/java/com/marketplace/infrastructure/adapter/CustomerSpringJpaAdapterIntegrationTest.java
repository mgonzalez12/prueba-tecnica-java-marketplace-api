package com.marketplace.infrastructure.adapter;

import com.marketplace.domain.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CustomerSpringJpaAdapterIntegrationTest.MapperConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
@DisplayName("Customer Spring JPA Adapter Integration Tests")
class CustomerSpringJpaAdapterIntegrationTest {

    @Autowired
    private CustomerSpringJpaAdapter customerAdapter;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setBirthDate(LocalDate.of(1990, 1, 15));
        customer.setRegistrationDate(new Date());
        customer.setLastActivityDate(LocalDate.now());
    }

    @Test
    @DisplayName("Should save and retrieve customer")
    void shouldSaveAndRetrieveCustomer() {
        // When
        Customer saved = customerAdapter.save(customer);
        Optional<Customer> found = customerAdapter.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Should find all customers")
    void shouldFindAllCustomers() {
        // Given
        customerAdapter.save(customer);
        Customer customer2 = new Customer();
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");
        customerAdapter.save(customer2);

        // When
        var allCustomers = customerAdapter.findAll();

        // Then
        assertEquals(2, allCustomers.size());
    }

    @Test
    @DisplayName("Should find customer by email")
    void shouldFindCustomerByEmail() {
        // Given
        customerAdapter.save(customer);

        // When
        Optional<Customer> found = customerAdapter.findByEmail("john.doe@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    @DisplayName("Should find customers by activity date range")
    void shouldFindCustomersByActivityDateRange() {
        // Given
        customer.setLastActivityDate(LocalDate.of(2024, 1, 15));
        customerAdapter.save(customer);
        
        Customer customer2 = new Customer();
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");
        customer2.setLastActivityDate(LocalDate.of(2024, 6, 15));
        customerAdapter.save(customer2);

        // When
        var customers = customerAdapter.findByActivityDateBetween(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 3, 31)
        );

        // Then
        assertEquals(1, customers.size());
        assertEquals("John", customers.get(0).getFirstName());
    }
    @TestConfiguration
    static class MapperConfig {
        @Bean
        CustomerSpringJpaAdapter customerSpringJpaAdapter(com.marketplace.infrastructure.adapter.mapper.CustomerDboMapper mapper,
                                                          com.marketplace.infrastructure.adapter.repository.CustomerJpaRepository repository) {
            return new CustomerSpringJpaAdapter(repository, mapper);
        }

        @Bean
        com.marketplace.infrastructure.adapter.mapper.CustomerDboMapper customerDboMapper() {
            return org.mapstruct.factory.Mappers.getMapper(com.marketplace.infrastructure.adapter.mapper.CustomerDboMapper.class);
        }
    }
}

