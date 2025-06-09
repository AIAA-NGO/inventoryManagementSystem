package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.dto.response.SalesTrendResponse;
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

    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") Long id);

    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Sale> findByCustomer_Id(Long customerId);

    List<Sale> findByStatus(SaleStatus status);

    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.status = :status")
    Optional<BigDecimal> sumTotalByStatus(@Param("status") SaleStatus status);

    long countByStatus(SaleStatus status);

    List<Sale> findTop5ByStatusOrderBySaleDateDesc(SaleStatus status);

    @Query(value = "SELECT * FROM sales WHERE status = 'COMPLETED' ORDER BY sale_date DESC LIMIT :limit",
            nativeQuery = true)
    List<Sale> findRecentSales(@Param("limit") int limit);

    @Query(value = """
    SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
    SELECT 
        DATE_FORMAT(s.sale_date, 
            CASE WHEN :periodType = 'DAILY' THEN '%Y-%m-%d' 
                 WHEN :periodType = 'WEEKLY' THEN '%Y-%u' 
                 ELSE '%Y-%m' END) AS period,
        SUM(s.total) AS amount,
        DATE(s.sale_date) AS date
    FROM sales s
    WHERE s.sale_date BETWEEN :startDate AND :endDate
    AND s.status = 'COMPLETED'
    GROUP BY DATE_FORMAT(s.sale_date, 
            CASE WHEN :periodType = 'DAILY' THEN '%Y-%m-%d' 
                 WHEN :periodType = 'WEEKLY' THEN '%Y-%u' 
                 ELSE '%Y-%m' END)
    ORDER BY DATE(s.sale_date)
    """, nativeQuery = true)
    List<SalesTrendResponse> getSalesTrend(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("periodType") String periodType);
}