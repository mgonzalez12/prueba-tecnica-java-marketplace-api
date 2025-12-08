package com.marketplace.domain.port;

import com.marketplace.domain.model.User;

import java.util.Optional;

public interface UserPersistencePort {

    User save(User user);

    Optional<User> findByEmail(String email);
}


