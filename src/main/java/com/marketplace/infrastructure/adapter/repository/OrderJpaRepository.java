package com.marketplace.infrastructure.adapter.repository;

import com.marketplace.infrastructure.adapter.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerId(Long customerId);
    
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}

