package com.manbhavansteel.manbhavan_steel_backend.repository;

import com.manbhavansteel.manbhavan_steel_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}