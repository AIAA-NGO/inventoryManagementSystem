package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Purchase;
import com.example.inventoryManagementSystem.model.Purchase.PurchaseStatus;
import com.example.inventoryManagementSystem.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findBySupplier(Supplier supplier);
    List<Purchase> findBySupplierId(Long supplierId);
    List<Purchase> findByStatus(PurchaseStatus status);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.supplier = :supplier")
    long countBySupplier(@Param("supplier") Supplier supplier);

    List<Purchase> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);


    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.status = :status")
    long countByStatus(@Param("status") PurchaseStatus status);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Purchase p WHERE p.status = 'RECEIVED' AND p.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumReceivedPurchasesInPeriod(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Purchase p WHERE p.status = :status ORDER BY p.orderDate DESC LIMIT :limit")
    List<Purchase> findRecentPurchasesByStatus(@Param("limit") int limit,
                                               @Param("status") PurchaseStatus status);

    @Query("SELECT p.supplier, SUM(p.totalAmount) FROM Purchase p " +
            "WHERE p.status = 'RECEIVED' AND p.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.supplier " +
            "ORDER BY SUM(p.totalAmount) DESC")
    List<Object[]> findTopSuppliersBySpend(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE', p.orderDate), SUM(p.totalAmount) FROM Purchase p " +
            "WHERE p.status = 'RECEIVED' AND p.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', p.orderDate) " +
            "ORDER BY FUNCTION('DATE', p.orderDate)")
    List<Object[]> getPurchaseTrend(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Purchase p JOIN FETCH p.items WHERE p.id = :id")
    Optional<Purchase> findByIdWithItems(@Param("id") Long id);

    List<Purchase> findBySupplierAndOrderDateBetween(Supplier supplier, LocalDateTime startDate, LocalDateTime endDate);
}