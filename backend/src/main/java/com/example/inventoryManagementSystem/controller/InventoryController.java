package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.InventoryAdjustmentRequest;
import com.example.inventoryManagementSystem.dto.response.InventoryStatusResponse;
import com.example.inventoryManagementSystem.dto.response.LowStockSuggestionResponse;
import com.example.inventoryManagementSystem.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<Page<InventoryStatusResponse>> getInventoryStatus(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(required = false) Boolean expiredOnly,
            Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getInventoryStatus(
                search, categoryId, brandId, lowStockOnly, expiredOnly, pageable));
    }

    @PostMapping("/adjust")
    public ResponseEntity<Void> adjustInventory(@RequestBody InventoryAdjustmentRequest request) {
        validateAdjustmentRequest(request);
        inventoryService.adjustInventory(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove-expired")
    public ResponseEntity<Void> removeExpiredProducts() {
        inventoryService.removeExpiredProducts();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-stock-suggestions")
    public ResponseEntity<List<LowStockSuggestionResponse>> getLowStockSuggestions() {
        return ResponseEntity.ok(inventoryService.getLowStockSuggestions());
    }

    private void validateAdjustmentRequest(InventoryAdjustmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (request.getAdjustmentAmount() == null) {
            throw new IllegalArgumentException("Adjustment amount is required");
        }
        if (Boolean.TRUE.equals(request.getCreateSupplierOrder())) {
            if (request.getOrderQuantity() == null || request.getOrderQuantity() <= 0) {
                throw new IllegalArgumentException("Order quantity must be positive when creating supplier order");
            }
        }
    }
}