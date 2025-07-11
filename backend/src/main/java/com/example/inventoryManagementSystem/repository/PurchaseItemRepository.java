
package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    void deleteByPurchaseId(Long purchaseId);
}