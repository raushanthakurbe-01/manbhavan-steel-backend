package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.entity.Order;
import com.manbhavansteel.manbhavan_steel_backend.entity.OrderItem;
import com.manbhavansteel.manbhavan_steel_backend.entity.User;
import com.manbhavansteel.manbhavan_steel_backend.repository.OrderItemRepository;
import com.manbhavansteel.manbhavan_steel_backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerAuthController customerAuthController;

    public OrderController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerAuthController customerAuthController
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerAuthController = customerAuthController;
    }

    // =========================================
    // CREATE ORDER
    // LOGIN IS MANDATORY
    // =========================================

    @PostMapping
    @Transactional
    public ResponseEntity<?> createOrder(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @RequestBody OrderRequest request
    ) {

        // =========================================
        // CUSTOMER AUTHENTICATION
        // =========================================

        if (authorization == null ||
                authorization.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login to place an order");
        }

        User authenticatedUser =
                customerAuthController
                        .getAuthenticatedUser(authorization);

        if (authenticatedUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired customer session. Please login again.");
        }

        // =========================================
        // BASIC VALIDATION
        // =========================================

        if (request == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid order request");
        }

        if (request.customerName == null ||
                request.customerName.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Customer name is required");
        }

        if (request.mobile == null ||
                request.mobile.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Mobile number is required");
        }

        if (!request.mobile.trim().matches("^[0-9]{10}$")) {

            return ResponseEntity
                    .badRequest()
                    .body("Enter a valid 10-digit mobile number");
        }

        if (request.address == null ||
                request.address.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Address is required");
        }

        if (request.city == null ||
                request.city.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("City is required");
        }

        if (request.pincode == null ||
                request.pincode.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Pincode is required");
        }

        if (!request.pincode.trim().matches("^[0-9]{6}$")) {

            return ResponseEntity
                    .badRequest()
                    .body("Enter a valid 6-digit pincode");
        }

        if (request.items == null ||
                request.items.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Order must contain at least one item");
        }

        // =========================================
        // PAYMENT METHOD
        // =========================================

        String paymentMethod = request.paymentMethod;

        if (paymentMethod == null ||
                paymentMethod.isBlank()) {

            paymentMethod = "COD";

        } else {

            paymentMethod =
                    paymentMethod.trim().toUpperCase();

            if (!paymentMethod.equals("COD") &&
                    !paymentMethod.equals("ONLINE")) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Invalid payment method. Use COD or ONLINE."
                        );
            }
        }

        // =========================================
        // CREATE ORDER
        // =========================================

        Order order = new Order();

        // LOGIN USER SE ORDER LINK HOGA
        order.setUserId(
                authenticatedUser.getId()
        );

        order.setCustomerName(
                request.customerName.trim()
        );

        order.setMobile(
                request.mobile.trim()
        );

        order.setAddress(
                request.address.trim()
        );

        order.setCity(
                request.city.trim()
        );

        order.setPincode(
                request.pincode.trim()
        );

        order.setStatus("PENDING");

        order.setPaymentMethod(paymentMethod);

        // =========================================
        // CALCULATE TOTAL
        // =========================================

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        for (OrderItemRequest itemRequest :
                request.items) {

            if (itemRequest == null) {

                return ResponseEntity
                        .badRequest()
                        .body("Invalid order item");
            }

            if (itemRequest.productId == null) {

                return ResponseEntity
                        .badRequest()
                        .body("Product ID is required");
            }

            if (itemRequest.quantity == null ||
                    itemRequest.quantity <= 0) {

                return ResponseEntity
                        .badRequest()
                        .body("Invalid item quantity");
            }

            /*
             * Price-on-request products ke liye
             * zero store kar rahe hain because
             * order_items.unit_price NOT NULL hai.
             */
            BigDecimal unitPrice =
                    itemRequest.unitPrice != null
                            ? itemRequest.unitPrice
                            : BigDecimal.ZERO;

            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {

                return ResponseEntity
                        .badRequest()
                        .body("Invalid item price");
            }

            BigDecimal subtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    itemRequest.quantity
                            )
                    );

            totalAmount =
                    totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        // =========================================
        // SAVE ORDER
        // =========================================

        Order savedOrder =
                orderRepository.save(order);

        // =========================================
        // SAVE ORDER ITEMS
        // =========================================

        for (OrderItemRequest itemRequest :
                request.items) {

            BigDecimal unitPrice =
                    itemRequest.unitPrice != null
                            ? itemRequest.unitPrice
                            : BigDecimal.ZERO;

            BigDecimal subtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    itemRequest.quantity
                            )
                    );

            OrderItem item =
                    new OrderItem();

            item.setOrderId(
                    savedOrder.getId()
            );

            item.setProductId(
                    itemRequest.productId
            );

            item.setProductName(
                    itemRequest.productName
            );

            item.setModelCode(
                    itemRequest.modelCode
            );

            item.setColorId(
                    itemRequest.colorId
            );

            item.setColorName(
                    itemRequest.colorName
            );

            item.setQuantity(
                    itemRequest.quantity
            );

            item.setUnitPrice(
                    unitPrice
            );

            item.setSubtotal(
                    subtotal
            );

            orderItemRepository.save(item);
        }

        // =========================================
        // RESPONSE
        // =========================================

        OrderResponse response =
                new OrderResponse(
                        savedOrder.getId(),
                        savedOrder.getStatus(),
                        savedOrder.getPaymentMethod(),
                        savedOrder.getTotalAmount()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================
    // GET ORDER
    // LOGIN + OWNER CHECK MANDATORY
    // =========================================

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(
            @PathVariable Long orderId,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        // =========================================
        // CUSTOMER AUTHENTICATION
        // =========================================

        if (authorization == null ||
                authorization.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login to view this order.");
        }

        User authenticatedUser =
                customerAuthController
                        .getAuthenticatedUser(authorization);

        if (authenticatedUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired customer session. Please login again.");
        }

        // =========================================
        // FIND ORDER
        // =========================================

        return orderRepository.findById(orderId)
                .map(order -> {

                    // =========================================
                    // OWNER CHECK
                    // =========================================

                    if (order.getUserId() == null ||
                            !order.getUserId().equals(
                                    authenticatedUser.getId()
                            )) {

                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(
                                        "You are not allowed to view this order."
                                );
                    }

                    // =========================================
                    // GET ORDER ITEMS
                    // =========================================

                    List<OrderItem> items =
                            orderItemRepository.findByOrderId(
                                    orderId
                            );

                    return ResponseEntity.ok(
                            new OrderDetailsResponse(
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
    // ORDER REQUEST
    // =========================================

    public static class OrderRequest {

        public String customerName;
        public String mobile;
        public String address;
        public String city;
        public String pincode;

        public String paymentMethod;

        public List<OrderItemRequest> items;
    }

    // =========================================
    // ORDER ITEM REQUEST
    // =========================================

    public static class OrderItemRequest {

        public Long productId;
        public String productName;
        public String modelCode;
        public Long colorId;
        public String colorName;
        public Integer quantity;
        public BigDecimal unitPrice;
    }

    // =========================================
    // ORDER RESPONSE
    // =========================================

    public static class OrderResponse {

        public Long orderId;
        public String status;
        public String paymentMethod;
        public BigDecimal totalAmount;

        public OrderResponse(
                Long orderId,
                String status,
                String paymentMethod,
                BigDecimal totalAmount
        ) {
            this.orderId = orderId;
            this.status = status;
            this.paymentMethod = paymentMethod;
            this.totalAmount = totalAmount;
        }
    }

    // =========================================
    // ORDER DETAILS RESPONSE
    // =========================================

    public static class OrderDetailsResponse {

        public Order order;
        public List<OrderItem> items;

        public OrderDetailsResponse(
                Order order,
                List<OrderItem> items
        ) {
            this.order = order;
            this.items = items;
        }
    }
}