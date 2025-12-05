package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.usecases.CustomerService;
import com.marketplace.domain.model.Customer;
import com.marketplace.infrastructure.rest.api.CustomersApi;
import com.marketplace.infrastructure.rest.dto.generated.CustomerRequest;
import com.marketplace.infrastructure.rest.mapper.CustomerApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CustomerApiController implements CustomersApi {

    private final CustomerService customerService;
    private final CustomerApiMapper customerApiMapper;

    @Override
    public ResponseEntity<List<com.marketplace.infrastructure.rest.dto.generated.Customer>> getAllCustomers() {
        List<com.marketplace.infrastructure.rest.dto.generated.Customer> customers = customerService.getAllCustomers().stream()
                .map(customerApiMapper::toApiDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @Override
    public ResponseEntity<com.marketplace.infrastructure.rest.dto.generated.Customer> createCustomer(CustomerRequest customerRequest) {
        Customer domain = customerApiMapper.toDomain(customerRequest);
        Customer saved = customerService.registerCustomer(domain);
        return new ResponseEntity<>(customerApiMapper.toApiDto(saved), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<com.marketplace.infrastructure.rest.dto.generated.Customer> getCustomerById(Long id) {
        Customer customer = customerService.getCustomer(id);
        return ResponseEntity.ok(customerApiMapper.toApiDto(customer));
    }

    @Override
    public ResponseEntity<com.marketplace.infrastructure.rest.dto.generated.Customer> updateCustomer(Long id,
                                                                                                  CustomerRequest customerRequest) {
        Customer update = customerApiMapper.toDomain(customerRequest);
        Customer updated = customerService.updateCustomer(id, update);
        return ResponseEntity.ok(customerApiMapper.toApiDto(updated));
    }

    @Override
    public ResponseEntity<List<com.marketplace.infrastructure.rest.dto.generated.Customer>> getCustomersByActivityDate(
            String fromDate,
            String toDate) {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        List<com.marketplace.infrastructure.rest.dto.generated.Customer> customers =
                customerService.getCustomersByActivityDate(from, to).stream()
                        .map(customerApiMapper::toApiDto)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @Override
    public ResponseEntity<com.marketplace.infrastructure.rest.dto.generated.Customer> recordCustomerActivity(Long id) {
        Customer updated = customerService.recordCustomerActivity(id);
        return ResponseEntity.ok(customerApiMapper.toApiDto(updated));
    }
}

