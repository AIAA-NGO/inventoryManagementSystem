package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.service.DashboardService;
import com.example.inventoryManagementSystem.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    private final ProductService productService;

    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary() {
        try {
            return ResponseEntity.ok(dashboardService.getDashboardSummary());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching dashboard summary: " + e.getMessage());
        }
    }

    @GetMapping("/sales-trend")
    public ResponseEntity<?> getSalesTrend(
            @RequestParam(defaultValue = "MONTHLY") String periodType) {
        try {
            if (!List.of("DAILY", "WEEKLY", "MONTHLY").contains(periodType.toUpperCase())) {
                periodType = "MONTHLY";
            }
            return ResponseEntity.ok(dashboardService.getSalesTrend(periodType));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching sales trend: " + e.getMessage());
        }
    }

    @GetMapping("/top-products")
    public ResponseEntity<?> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            return ResponseEntity.ok(dashboardService.getTopSellingProducts(limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching top products: " + e.getMessage());
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getCriticalLowStockItems() {
        try {
            // Get all products with pagination (use large page size to get all)
            List<ProductResponse> allProducts = productService.getAllProducts(0, Integer.MAX_VALUE).getContent();

            List<LowStockItemResponse> lowStockItems = allProducts.stream()
                    .filter(p -> p.getQuantityInStock() <= (p.getLowStockThreshold() != null ?
                            p.getLowStockThreshold() : 10))
                    .map(p -> LowStockItemResponse.builder()
                            .productId(p.getId())
                            .productName(p.getName())
                            .sku(p.getSku())
                            .currentStock(p.getQuantityInStock())
                            .threshold(p.getLowStockThreshold() != null ?
                                    p.getLowStockThreshold() : 10)
                            .category(p.getCategoryName())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lowStockItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching low stock items: " + e.getMessage());
        }
    }

    @GetMapping("/recent-sales")
    public ResponseEntity<?> getRecentSales(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            return ResponseEntity.ok(dashboardService.getRecentSales(limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching recent sales: " + e.getMessage());
        }
    }

    @GetMapping("/expiring-items")
    public ResponseEntity<?> getSoonToExpireItems() {
        try {
            return ResponseEntity.ok(dashboardService.getSoonToExpireItems());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching expiring items: " + e.getMessage());
        }
    }
}