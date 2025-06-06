
package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}