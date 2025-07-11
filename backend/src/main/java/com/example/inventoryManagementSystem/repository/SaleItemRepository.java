package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.model.Sale;
import com.example.inventoryManagementSystem.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    // Basic CRUD operations are inherited from JpaRepository

    // Find all sale items for a specific sale
    List<SaleItem> findBySale_Id(Long saleId);

    // Find all sale items for a specific product
    List<SaleItem> findByProduct(Product product);

    // Find top selling products with limit
    @Query(value = "SELECT " +
            "p.id as productId, " +
            "p.name as productName, " +
            "p.image_url as productImage, " +
            "SUM(si.quantity) as unitsSold, " +
            "SUM(si.quantity * si.unit_price) as revenue " +
            "FROM sale_items si " +
            "JOIN products p ON si.product_id = p.id " +
            "JOIN sales s ON si.sale_id = s.id " +
            "WHERE s.status = 'COMPLETED' " +
            "GROUP BY p.id, p.name, p.image_url " +
            "ORDER BY unitsSold DESC " +
            "LIMIT ?1", nativeQuery = true)
    List<Object[]> findTopSellingProducts(int limit);


    // Calculate total revenue from completed sales
    @Query("SELECT COALESCE(SUM(si.totalPrice), 0) FROM SaleItem si WHERE si.sale.status = 'COMPLETED'")
    BigDecimal sumTotalRevenueFromCompletedSales();

    // Calculate total cost from completed sales
    @Query("SELECT COALESCE(SUM(si.quantity * p.costPrice), 0) FROM SaleItem si JOIN si.product p WHERE si.sale.status = 'COMPLETED'")
    BigDecimal sumTotalCostFromCompletedSales();

    // Find sale items with product details for a specific sale
    @Query("SELECT si FROM SaleItem si JOIN FETCH si.product WHERE si.sale.id = :saleId")
    List<SaleItem> findBySaleIdWithProduct(@Param("saleId") Long saleId);

    // Calculate profit for a specific product
    @Query("SELECT COALESCE(SUM(si.totalPrice - (si.quantity * p.costPrice)), 0) " +
            "FROM SaleItem si JOIN si.product p " +
            "WHERE si.product = :product AND si.sale.status = 'COMPLETED'")
    BigDecimal calculateProfitByProduct(@Param("product") Product product);

    // Get sales items within a date range
    @Query("SELECT si FROM SaleItem si WHERE si.sale.saleDate BETWEEN :startDate AND :endDate")
    List<SaleItem> findBySaleDateBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Get sales items by product and status
    @Query("SELECT si FROM SaleItem si WHERE si.product = :product AND si.sale.status = :status")
    List<SaleItem> findByProductAndStatus(@Param("product") Product product,
                                          @Param("status") Sale.SaleStatus status);

    // Get total quantity sold for a product
    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.product = :product AND si.sale.status = 'COMPLETED'")
    Integer sumQuantityByProduct(@Param("product") Product product);

    // Get sales items with discounts
    @Query("SELECT si FROM SaleItem si WHERE si.discountAmount > 0")
    List<SaleItem> findDiscountedItems();
}