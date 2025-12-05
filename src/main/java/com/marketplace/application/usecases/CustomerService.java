package com.marketplace.application.usecases;

import com.marketplace.domain.model.Customer;
import java.time.LocalDate;
import java.util.List;

public interface CustomerService {
    Customer registerCustomer(Customer customer);

    Customer updateCustomer(Long id, Customer customer);

    Customer getCustomer(Long id);

    List<Customer> getAllCustomers();

    List<Customer> getCustomersByActivityDate(LocalDate fromDate, LocalDate toDate);

    Customer recordCustomerActivity(Long customerId);
}
