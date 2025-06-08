package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByCompanyNameContaining(String companyName);
    List<Supplier> findByContactPersonContaining(String contactPerson);

    List<Supplier> findByCompanyNameContainingIgnoreCase(String companyName);
    List<Supplier> findByContactPersonContainingIgnoreCase(String contactPerson);

    @Query("SELECT s FROM Supplier s JOIN s.suppliedCategories c WHERE LOWER(c.name) LIKE LOWER(concat('%', :category, '%'))")
    List<Supplier> findBySuppliedCategoriesNameContainingIgnoreCase(@Param("category") String category);

    @Query("SELECT s FROM Supplier s JOIN s.suppliedCategories c WHERE c.name LIKE %:category%")
    List<Supplier> findBySuppliedCategoriesNameContaining(@Param("category") String category);
}