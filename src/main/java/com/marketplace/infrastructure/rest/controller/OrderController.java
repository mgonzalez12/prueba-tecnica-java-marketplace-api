package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.usecases.OrderService;
import com.marketplace.domain.model.Order;
import com.marketplace.infrastructure.rest.dto.request.OrderRequestDto;
import com.marketplace.infrastructure.rest.dto.response.OrderResponseDto;
import com.marketplace.infrastructure.rest.mapper.OrderRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;
    private final OrderRestMapper orderRestMapper;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto request) {
        Order domain = orderRestMapper.toDomain(request);
        Order created = orderService.createOrder(domain);
        return new ResponseEntity<>(orderRestMapper.toResponse(created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }

    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        List<OrderResponseDto> orders = orderService.getAllOrders().stream()
                .map(orderRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderResponseDto> orders = orderService.getOrdersByCustomer(customerId).stream()
                .map(orderRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        Order updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(orderRestMapper.toResponse(updated));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long id) {
        Order cancelled = orderService.cancelOrder(id);
        return ResponseEntity.ok(orderRestMapper.toResponse(cancelled));
    }
}

