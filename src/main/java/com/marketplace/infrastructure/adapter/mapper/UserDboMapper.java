package com.marketplace.infrastructure.adapter.mapper;

import com.marketplace.domain.model.Role;
import com.marketplace.domain.model.User;
import com.marketplace.infrastructure.adapter.entity.RoleEntity;
import com.marketplace.infrastructure.adapter.entity.UserEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDboMapper {

    private UserDboMapper() {
    }

    public static UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setIsActive(user.getIsActive());

        if (user.getRoles() != null) {
            Set<RoleEntity> roleEntities = user.getRoles().stream()
                    .map(UserDboMapper::toRoleEntity)
                    .collect(Collectors.toSet());
            entity.setRoles(roleEntities);
        } else {
            entity.setRoles(new HashSet<>());
        }

        return entity;
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        User user = new User();
        user.setId(entity.getId());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setIsActive(entity.getIsActive());

        if (entity.getRoles() != null) {
            Set<Role> roles = entity.getRoles().stream()
                    .map(UserDboMapper::toRoleDomain)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            user.setRoles(new HashSet<>());
        }

        return user;
    }

    private static RoleEntity toRoleEntity(Role role) {
        if (role == null) {
            return null;
        }
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        if (role.getName() != null) {
            entity.setName(RoleEntity.RoleName.valueOf(role.getName().name()));
        }
        return entity;
    }

    private static Role toRoleDomain(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        Role role = new Role();
        role.setId(entity.getId());
        if (entity.getName() != null) {
            role.setName(Role.RoleName.valueOf(entity.getName().name()));
        }
        return role;
    }
}


