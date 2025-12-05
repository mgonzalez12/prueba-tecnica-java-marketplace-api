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

@Service
@RequiredArgsConstructor
public class CustomerManagementService implements CustomerService {

    private final CustomerPersistencePort customerPersistencePort;

    @Override
    @Transactional
    public Customer registerCustomer(Customer customer) {
        // Set registration date using legacy Date API as required
        if (customer.getRegistrationDate() == null) {
            customer.setRegistrationDate(new Date());
        }
        // Set initial activity date
        if (customer.getLastActivityDate() == null) {
            customer.setLastActivityDate(LocalDate.now());
        }
        // Initialize counters
        if (customer.getTotalOrders() == null) {
            customer.setTotalOrders(0);
        }
        return customerPersistencePort.save(customer);
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existing = getCustomer(id);
        
        // Update fields
        if (customer.getFirstName() != null) {
            existing.setFirstName(customer.getFirstName());
        }
        if (customer.getLastName() != null) {
            existing.setLastName(customer.getLastName());
        }
        if (customer.getEmail() != null) {
            existing.setEmail(customer.getEmail());
        }
        if (customer.getPhone() != null) {
            existing.setPhone(customer.getPhone());
        }
        if (customer.getAddress() != null) {
            existing.setAddress(customer.getAddress());
        }
        if (customer.getBirthDate() != null) {
            existing.setBirthDate(customer.getBirthDate());
        }
        if (customer.getRenewalDate() != null) {
            existing.setRenewalDate(customer.getRenewalDate());
        }
        if (customer.getStatus() != null) {
            existing.setStatus(customer.getStatus());
        }
        
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
