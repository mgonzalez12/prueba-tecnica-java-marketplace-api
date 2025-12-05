package com.marketplace.infrastructure.adapter.repository;

import com.marketplace.infrastructure.adapter.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByEmail(String email);
    
    List<CustomerEntity> findByLastActivityDateBetween(LocalDate fromDate, LocalDate toDate);
}
