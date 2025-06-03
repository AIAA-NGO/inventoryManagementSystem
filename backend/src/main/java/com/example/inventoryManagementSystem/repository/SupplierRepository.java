package com.example.inventoryManagementSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<com.example.inventoryManagementSystem.model.Supplier, Long> {
}
