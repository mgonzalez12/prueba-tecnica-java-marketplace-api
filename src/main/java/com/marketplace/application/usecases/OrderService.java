package com.marketplace.application.usecases;

import com.marketplace.domain.model.Order;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);

    Order getOrder(Long id);

    List<Order> getOrdersByCustomer(Long customerId);

    Order updateOrderStatus(Long orderId, Order.OrderStatus status);

    Order cancelOrder(Long orderId);

    List<Order> getAllOrders();
}
