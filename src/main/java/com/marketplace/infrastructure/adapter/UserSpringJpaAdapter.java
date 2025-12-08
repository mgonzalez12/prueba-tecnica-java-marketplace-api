package com.marketplace.infrastructure.adapter;

import com.marketplace.domain.model.User;
import com.marketplace.domain.port.UserPersistencePort;
import com.marketplace.infrastructure.adapter.mapper.UserDboMapper;
import com.marketplace.infrastructure.adapter.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSpringJpaAdapter implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        var entity = UserDboMapper.toEntity(user);
        var saved = userJpaRepository.save(entity);
        return UserDboMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserDboMapper::toDomain);
    }
}


