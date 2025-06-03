package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {
}
