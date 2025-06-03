package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Purchase;
import com.example.inventoryManagementSystem.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findBySupplier(Supplier supplier);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.supplier = :supplier")
    long countBySupplier(Supplier supplier);
}