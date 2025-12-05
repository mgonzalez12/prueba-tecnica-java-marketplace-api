package com.marketplace.application.service;

import com.marketplace.application.usecases.CustomerService;
import com.marketplace.domain.model.Customer;
import com.marketplace.domain.port.CustomerPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerManagementService implements CustomerService {

    private final CustomerPersistencePort customerPersistencePort;

    @Override
    @Transactional
    public Customer registerCustomer(Customer customer) {
        // Set registration date using legacy Date API as required
        customer.setRegistrationDate(
                Optional.ofNullable(customer.getRegistrationDate())
                        .orElseGet(Date::new)
        );

        // Set initial activity date
        customer.setLastActivityDate(
                Optional.ofNullable(customer.getLastActivityDate())
                        .orElseGet(LocalDate::now)
        );

        // Initialize counters
        customer.setTotalOrders(
                Optional.ofNullable(customer.getTotalOrders())
                        .orElse(0)
        );

        return customerPersistencePort.save(customer);
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existing = getCustomer(id);
        
        // Update fields
        Optional.ofNullable(customer.getFirstName()).ifPresent(existing::setFirstName);
        Optional.ofNullable(customer.getLastName()).ifPresent(existing::setLastName);
        Optional.ofNullable(customer.getEmail()).ifPresent(existing::setEmail);
        Optional.ofNullable(customer.getPhone()).ifPresent(existing::setPhone);
        Optional.ofNullable(customer.getAddress()).ifPresent(existing::setAddress);
        Optional.ofNullable(customer.getBirthDate()).ifPresent(existing::setBirthDate);
        Optional.ofNullable(customer.getRenewalDate()).ifPresent(existing::setRenewalDate);
        Optional.ofNullable(customer.getStatus()).ifPresent(existing::setStatus);
        
        return customerPersistencePort.save(existing);
    }

    @Override
    public Customer getCustomer(Long id) {
        return customerPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerPersistencePort.findAll();
    }

    @Override
    public List<Customer> getCustomersByActivityDate(LocalDate fromDate, LocalDate toDate) {
        return customerPersistencePort.findByActivityDateBetween(fromDate, toDate);
    }

    @Override
    @Transactional
    public Customer recordCustomerActivity(Long customerId) {
        Customer customer = getCustomer(customerId);
        customer.recordActivity();
        return customerPersistencePort.save(customer);
    }
}
