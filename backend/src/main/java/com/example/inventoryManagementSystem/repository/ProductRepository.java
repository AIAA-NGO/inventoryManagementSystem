package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(String query);

    @Query("SELECT p FROM Product p WHERE p.quantityInStock <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    List<Product> findBySupplier(Supplier supplier);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.supplier = :supplier")
    long countBySupplier(Supplier supplier);
}