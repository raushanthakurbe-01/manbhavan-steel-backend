package com.manbhavansteel.manbhavan_steel_backend.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "https://manbhavansteel.pages.dev"
})
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @PersistenceContext
    private EntityManager entityManager;

    private static final List<String> ALLOWED_STATUSES = List.of(
            "PENDING",
            "CONFIRMED",
            "PROCESSING",
            "SHIPPED",
            "DELIVERED",
            "CANCELLED"
    );

    @GetMapping
    public ResponseEntity<?> getAllOrders() {

        String sql = """
                SELECT
                    id,
                    order_number,
                    customer_name,
                    mobile,
                    address,
                    city,
                    pincode,
                    total_amount,
                    status,
                    created_at
                FROM orders
                ORDER BY created_at DESC
                """;

        List<?> rows = entityManager
                .createNativeQuery(sql)
                .getResultList();

        List<Map<String, Object>> orders = new ArrayList<>();

        for (Object rowObject : rows) {

            Object[] row = (Object[]) rowObject;

            Map<String, Object> order = new LinkedHashMap<>();

            order.put("id", row[0]);
            order.put("orderNumber", row[1]);
            order.put("customerName", row[2]);
            order.put("mobile", row[3]);
            order.put("address", row[4]);
            order.put("city", row[5]);
            order.put("pincode", row[6]);
            order.put("totalAmount", row[7]);
            order.put("status", row[8]);
            order.put("createdAt", row[9]);

            orders.add(order);
        }

        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request
    ) {

        if (request.status() == null ||
                !ALLOWED_STATUSES.contains(
                        request.status().toUpperCase()
                )) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid order status"
                            )
                    );
        }

        String status = request.status().toUpperCase();

        int updatedRows = entityManager.createNativeQuery(
                        """
                        UPDATE orders
                        SET status = ?
                        WHERE id = ?
                        """
                )
                .setParameter(1, status)
                .setParameter(2, id)
                .executeUpdate();

        if (updatedRows == 0) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Order status updated successfully",
                        "status",
                        status
                )
        );
    }

    public record StatusRequest(
            String status
    ) {
    }
}