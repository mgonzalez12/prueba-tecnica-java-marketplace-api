package com.marketplace.infrastructure.adapter;

import com.marketplace.domain.model.Customer;
import com.marketplace.domain.port.CustomerPersistencePort;
import com.marketplace.infrastructure.adapter.entity.CustomerEntity;
import com.marketplace.infrastructure.adapter.mapper.CustomerDboMapper;
import com.marketplace.infrastructure.adapter.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerSpringJpaAdapter implements CustomerPersistencePort {

    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerDboMapper customerDboMapper;

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = customerDboMapper.toEntity(customer);
        CustomerEntity saved = customerJpaRepository.save(entity);
        return customerDboMapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerJpaRepository.findById(id)
                .map(customerDboMapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return customerJpaRepository.findAll().stream()
                .map(customerDboMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return customerJpaRepository.findByEmail(email)
                .map(customerDboMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        customerJpaRepository.deleteById(id);
    }

    @Override
    public List<Customer> findByActivityDateBetween(LocalDate fromDate, LocalDate toDate) {
        return customerJpaRepository.findByLastActivityDateBetween(fromDate, toDate).stream()
                .map(customerDboMapper::toDomain)
                .collect(Collectors.toList());
    }
}
