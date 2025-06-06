package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") Long id);

    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Sale> findByCustomer_Id(Long customerId);

    List<Sale> findByStatus(Sale.SaleStatus status);
}