package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getBusinessSummary() {
        return ResponseEntity.ok(dashboardService.getBusinessSummary());
    }

    @GetMapping("/sales-trend")
    public ResponseEntity<List<SalesTrendResponse>> getSalesTrend(
            @RequestParam(defaultValue = "MONTHLY") String periodType) {
        return ResponseEntity.ok(dashboardService.getSalesTrend(periodType));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getTopSellingProducts(limit));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockItemResponse>> getCriticalLowStockItems() {
        return ResponseEntity.ok(dashboardService.getCriticalLowStockItems());
    }

    @GetMapping("/recent-sales")
    public ResponseEntity<List<RecentSaleResponse>> getRecentSales(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentSales(limit));
    }

    @GetMapping("/expiring-items")
    public ResponseEntity<List<ExpiringItemResponse>> getSoonToExpireItems() {
        return ResponseEntity.ok(dashboardService.getSoonToExpireItems());
    }
}