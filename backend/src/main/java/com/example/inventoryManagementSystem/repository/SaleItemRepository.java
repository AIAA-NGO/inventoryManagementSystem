
package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

}