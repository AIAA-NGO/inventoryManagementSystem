package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Sale;
import com.example.inventoryManagementSystem.model.Sale.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // Fetch sale with items eagerly
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") Long id);

    // Find sales within date range
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find sales by customer
    List<Sale> findByCustomer_Id(Long customerId);

    // Find sales by status
    List<Sale> findByStatus(SaleStatus status);

    // Sum totals by status
    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.status = :status")
    Optional<BigDecimal> sumTotalByStatus(@Param("status") SaleStatus status);

    // Count sales by status
    long countByStatus(SaleStatus status);

    // Get recent sales by status
    List<Sale> findTop5ByStatusOrderBySaleDateDesc(SaleStatus status);

    // Sales trend analysis - using native query for PostgreSQL
    @Query(value = """
        SELECT 
            date_trunc(:periodType, s.sale_date) AS period,
            COALESCE(SUM(s.subtotal), 0) AS amount,
            COUNT(s.id) AS salesCount,
            CASE 
                WHEN :periodType = 'day' THEN TO_CHAR(date_trunc(:periodType, s.sale_date), 'DD Mon')
                WHEN :periodType = 'week' THEN 'Week ' || TO_CHAR(date_trunc(:periodType, s.sale_date), 'WW, YYYY')
                WHEN :periodType = 'month' THEN TO_CHAR(date_trunc(:periodType, s.sale_date), 'Mon YYYY')
                ELSE TO_CHAR(date_trunc(:periodType, s.sale_date), 'YYYY-MM-DD')
            END AS periodLabel
        FROM sales s  
        WHERE s.sale_date BETWEEN :startDate AND :endDate
        AND s.status = 'COMPLETED'
        GROUP BY date_trunc(:periodType, s.sale_date), 
                 CASE 
                     WHEN :periodType = 'day' THEN TO_CHAR(date_trunc(:periodType, s.sale_date), 'DD Mon')
                     WHEN :periodType = 'week' THEN 'Week ' || TO_CHAR(date_trunc(:periodType, s.sale_date), 'WW, YYYY')
                     WHEN :periodType = 'month' THEN TO_CHAR(date_trunc(:periodType, s.sale_date), 'Mon YYYY')
                     ELSE TO_CHAR(date_trunc(:periodType, s.sale_date), 'YYYY-MM-DD')
                 END
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> getSalesTrendNative(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("periodType") String periodType);

    // Count completed sales in date range
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :start AND :end")
    long countCompletedSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Sales summary methods
    @Query("SELECT COALESCE(SUM(s.subtotal), 0) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal getCompletedSalesSubtotal();

    @Query("SELECT COALESCE(SUM(s.discountAmount), 0) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal getCompletedSalesDiscounts();

    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal getCompletedSalesTotal();

    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal getCompletedSalesRevenue();

    @Query(value = """
        SELECT COALESCE(SUM(si.quantity * (si.unit_price - p.cost_price)), 0) 
        FROM sale_items si 
        JOIN products p ON si.product_id = p.id 
        JOIN sales s ON si.sale_id = s.id
        WHERE s.status = 'COMPLETED'
        """, nativeQuery = true)
    BigDecimal getCompletedSalesProfit();

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.status = 'COMPLETED'")
    long countCompletedSales();

    // Additional useful queries
    @Query("SELECT s FROM Sale s WHERE s.status = 'COMPLETED' ORDER BY s.saleDate DESC")
    List<Sale> findRecentCompletedSales();

    @Query("SELECT s FROM Sale s JOIN FETCH s.items WHERE s.customer.id = :customerId")
    List<Sale> findByCustomerWithItems(@Param("customerId") Long customerId);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.status = 'COMPLETED'")
    List<Sale> findCompletedSalesBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.status = :status")
    List<Sale> findByDateBetweenAndStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") SaleStatus status);
}