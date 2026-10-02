package com.manbhavansteel.manbhavan_steel_backend.repository;

import com.manbhavansteel.manbhavan_steel_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}