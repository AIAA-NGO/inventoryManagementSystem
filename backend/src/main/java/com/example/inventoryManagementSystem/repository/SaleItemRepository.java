package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySale_Id(Long saleId);


    @Query("SELECT si.product.id, p.name, p.imageData, SUM(si.quantity), SUM(si.totalPrice) " +
            "FROM SaleItem si JOIN si.product p " +
            "WHERE si.sale.status = 'COMPLETED' " +
            "GROUP BY si.product.id, p.name, p.imageData " +
            "ORDER BY SUM(si.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("limit") int limit);

    @Query("SELECT COALESCE(SUM(si.totalPrice), 0) FROM SaleItem si WHERE si.sale.status = 'COMPLETED'")
    BigDecimal sumTotalRevenueFromCompletedSales();

    @Query("SELECT SUM(si.quantity * p.costPrice) FROM SaleItem si JOIN si.product p WHERE si.sale.status = 'COMPLETED'")
    BigDecimal sumTotalCostFromCompletedSales();

    @Query("SELECT si FROM SaleItem si JOIN FETCH si.product WHERE si.sale.id = :saleId")
    List<SaleItem> findBySaleIdWithProduct(@Param("saleId") Long saleId);
}