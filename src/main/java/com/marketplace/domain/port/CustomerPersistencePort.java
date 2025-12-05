package com.marketplace.domain.port;

import com.marketplace.domain.model.Customer;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface CustomerPersistencePort {
    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    List<Customer> findAll();

    Optional<Customer> findByEmail(String email);

    List<Customer> findByActivityDateBetween(LocalDate fromDate, LocalDate toDate);

    void deleteById(Long id);
}
