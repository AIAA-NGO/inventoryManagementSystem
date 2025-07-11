package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Discount;
import com.example.inventoryManagementSystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    @Query("SELECT d FROM Discount d WHERE " +
            "(d.validFrom IS NULL OR CURRENT_TIMESTAMP >= d.validFrom) AND " +
            "(d.validTo IS NULL OR CURRENT_TIMESTAMP <= d.validTo)")

    List<Discount> findActiveDiscounts();

    @Query("SELECT d FROM Discount d JOIN d.applicableProducts p WHERE p = :product")

    List<Discount> findByApplicableProductsContaining(@Param("product") Product product);

    Optional<Discount> findByCode(String code);

    @Query("SELECT d FROM Discount d JOIN d.applicableProducts p WHERE " +
            "p = :product AND " +
            "(d.validFrom IS NULL OR CURRENT_TIMESTAMP >= d.validFrom) AND " +
            "(d.validTo IS NULL OR CURRENT_TIMESTAMP <= d.validTo)")

    List<Discount> findActiveDiscountsForProduct(@Param("product") Product product);
}