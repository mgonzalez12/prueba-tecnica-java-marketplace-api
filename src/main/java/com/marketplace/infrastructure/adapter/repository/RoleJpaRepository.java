package com.marketplace.infrastructure.adapter.repository;

import com.marketplace.infrastructure.adapter.entity.RoleEntity;
import com.marketplace.infrastructure.adapter.entity.RoleEntity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(RoleName name);
}


