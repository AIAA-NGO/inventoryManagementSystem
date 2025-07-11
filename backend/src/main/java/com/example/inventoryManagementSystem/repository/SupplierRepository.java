package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("SELECT s FROM Supplier s JOIN s.suppliedCategories c WHERE LOWER(c.name) LIKE LOWER(concat('%', :category, '%'))")
    List<Supplier> findBySuppliedCategoriesNameContainingIgnoreCase(@Param("category") String category);
}