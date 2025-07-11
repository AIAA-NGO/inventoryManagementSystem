package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByNameContainingIgnoreCase(String name);
}