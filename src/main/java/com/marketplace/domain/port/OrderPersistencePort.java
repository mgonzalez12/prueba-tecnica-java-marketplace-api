package com.marketplace.domain.port;

import com.marketplace.domain.model.Order;
import java.util.Optional;
import java.util.List;

public interface OrderPersistencePort {
    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findAll();
}
