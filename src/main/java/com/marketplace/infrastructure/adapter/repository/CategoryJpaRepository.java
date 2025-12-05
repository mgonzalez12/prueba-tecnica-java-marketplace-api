package com.marketplace.infrastructure.adapter.repository;

import com.marketplace.infrastructure.adapter.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentId(Long parentId);
}
