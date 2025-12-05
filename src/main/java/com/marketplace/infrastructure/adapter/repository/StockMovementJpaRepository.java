package com.marketplace.infrastructure.adapter.repository;

import com.marketplace.infrastructure.adapter.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, Long> {
    List<StockMovementEntity> findByProductIdOrderByTimestampDesc(Long productId);
}

