package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.usecases.CustomerService;
import com.marketplace.domain.model.Customer;
import com.marketplace.infrastructure.rest.api.CustomersApi;
import com.marketplace.infrastructure.rest.dto.generated.CustomerRequest;
import com.marketplace.infrastructure.rest.mapper.CustomerApiMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CustomerApiController implements CustomersApi {

    private final CustomerService customerService;
    private final CustomerApiMapper customerApiMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.marketplace.infrastructure.rest.dto.generated.Customer>> getAllCustomers() {
        List<com.marketplace.infrastructure.rest.dto.generated.Customer> customers = customerService.getAllCustomers().stream()
                .map(customerApiMapper::toApiDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.marketplace.infrastructure.rest.dto.generated.Customer> createCustomer(CustomerRequest customerRequest) {
        Customer domain = customerApiMapper.toDomain(customerRequest);
        Customer saved = customerService.registerCustomer(domain);
        return new ResponseEntity<>(customerApiMapper.toApiDto(saved), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.marketplace.infrastructure.rest.dto.generated.Customer> getCustomerById(Long id) {
        Customer customer = customerService.getCustomer(id);
        return ResponseEntity.ok(customerApiMapper.toApiDto(customer));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.marketplace.infrastructure.rest.dto.generated.Customer> updateCustomer(Long id,
                                                                                                  CustomerRequest customerRequest) {
        Customer update = customerApiMapper.toDomain(customerRequest);
        Customer updated = customerService.updateCustomer(id, update);
        return ResponseEntity.ok(customerApiMapper.toApiDto(updated));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<com.marketplace.infrastructure.rest.dto.generated.Customer> recordCustomerActivity(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Customer customer = customerService.getCustomer(id);
            String currentUserEmail = authentication.getName();
            if (customer.getEmail() == null || !customer.getEmail().equalsIgnoreCase(currentUserEmail)) {
                throw new AccessDeniedException("You can only record activity for your own customer profile");
            }
        }

        Customer updated = customerService.recordCustomerActivity(id);
        return ResponseEntity.ok(customerApiMapper.toApiDto(updated));
    }
}

