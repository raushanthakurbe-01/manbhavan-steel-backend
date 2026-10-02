package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.entity.Order;
import com.manbhavansteel.manbhavan_steel_backend.entity.OrderItem;
import com.manbhavansteel.manbhavan_steel_backend.entity.User;
import com.manbhavansteel.manbhavan_steel_backend.repository.OrderItemRepository;
import com.manbhavansteel.manbhavan_steel_backend.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/customer/orders")
public class CustomerOrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerAuthController customerAuthController;

    public CustomerOrderController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerAuthController customerAuthController
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerAuthController = customerAuthController;
    }

    // =========================================
    // GET LOGGED-IN CUSTOMER ORDERS
    // =========================================

    @GetMapping
    public ResponseEntity<?> getMyOrders(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        User user =
                customerAuthController
                        .getAuthenticatedUser(authorization);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Please login to view your orders."
                            )
                    );
        }

        List<Order> orders =
                orderRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                user.getId()
                        );

        return ResponseEntity.ok(orders);
    }

    // =========================================
    // GET SINGLE CUSTOMER ORDER
    // =========================================

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getMyOrder(
            @PathVariable Long orderId,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        User user =
                customerAuthController
                        .getAuthenticatedUser(authorization);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Please login to view this order."
                            )
                    );
        }

        return orderRepository.findById(orderId)
                .map(order -> {

                    // Security check:
                    // Customer sirf apna order dekh sakta hai.
                    if (order.getUserId() == null ||
                            !order.getUserId().equals(user.getId())) {

                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(
                                        Map.of(
                                                "message",
                                                "You are not allowed to view this order."
                                        )
                                );
                    }

                    List<OrderItem> items =
                            orderItemRepository.findByOrderId(
                                    orderId
                            );

                    return ResponseEntity.ok(
                            new CustomerOrderDetailsResponse(
                                    order,
                                    items
                            )
                    );
                })
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // =========================================
    // RESPONSE CLASS
    // =========================================

    public static class CustomerOrderDetailsResponse {

        public Order order;
        public List<OrderItem> items;

        public CustomerOrderDetailsResponse(
                Order order,
                List<OrderItem> items
        ) {
            this.order = order;
            this.items = items;
        }
    }
}