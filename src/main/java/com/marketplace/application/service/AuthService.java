package com.marketplace.application.service;

import com.marketplace.domain.model.Role;
import com.marketplace.domain.model.User;
import com.marketplace.domain.port.UserPersistencePort;
import com.marketplace.infrastructure.adapter.entity.RoleEntity;
import com.marketplace.infrastructure.adapter.repository.RoleJpaRepository;
import com.marketplace.infrastructure.adapter.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserPersistencePort userPersistencePort;
    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String firstName,
                         String lastName,
                         String email,
                         String rawPassword,
                         boolean admin) {

        if (userJpaRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setIsActive(true);

        RoleEntity.RoleName roleName = admin ? RoleEntity.RoleName.ADMIN : RoleEntity.RoleName.USER;

        var roleEntity = roleJpaRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role " + roleName + " not found"));

        Role role = new Role();
        role.setId(roleEntity.getId());
        role.setName(Role.RoleName.valueOf(roleEntity.getName().name()));

        user.setRoles(Set.of(role));

        return userPersistencePort.save(user);
    }
}


