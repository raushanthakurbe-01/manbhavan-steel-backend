package com.manbhavansteel.manbhavan_steel_backend.repository;

import com.manbhavansteel.manbhavan_steel_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
}