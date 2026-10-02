package com.manbhavansteel.manbhavan_steel_backend.repository;

import com.manbhavansteel.manbhavan_steel_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

}