package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.usecases.CustomerService;
import com.marketplace.domain.model.Customer;
import com.marketplace.infrastructure.rest.dto.CustomerRequestDto;
import com.marketplace.infrastructure.rest.dto.CustomerResponseDto;
import com.marketplace.infrastructure.rest.mapper.CustomerRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRestMapper customerRestMapper;

    @PostMapping
    @Operation(summary = "Register a new customer")
    public ResponseEntity<CustomerResponseDto> registerCustomer(@Valid @RequestBody CustomerRequestDto request) {
        Customer domain = customerRestMapper.toDomain(request);
        Customer saved = customerService.registerCustomer(domain);
        return new ResponseEntity<>(customerRestMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDto request) {
        Customer domain = customerRestMapper.toDomain(request);
        Customer updated = customerService.updateCustomer(id, domain);
        return ResponseEntity.ok(customerRestMapper.toResponse(updated));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponseDto> getCustomer(@PathVariable Long id) {
        Customer customer = customerService.getCustomer(id);
        return ResponseEntity.ok(customerRestMapper.toResponse(customer));
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        List<CustomerResponseDto> customers = customerService.getAllCustomers().stream()
                .map(customerRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @PostMapping("/{id}/activity")
    @Operation(summary = "Record customer activity")
    public ResponseEntity<CustomerResponseDto> recordActivity(@PathVariable Long id) {
        Customer customer = customerService.recordCustomerActivity(id);
        return ResponseEntity.ok(customerRestMapper.toResponse(customer));
    }
}
