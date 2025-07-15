package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.ExportReportRequest;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.service.ReportService;
import org.springframework.core.io.Resource; // Correct import for Resource
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/financial")
@RequiredArgsConstructor
public class FinancialReportController {
    private final ReportService reportService;

    @GetMapping("/profit-loss")
    public ResponseEntity<ProfitLossResponse> getProfitLossReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            ProfitLossResponse report = reportService.generateProfitLossReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ProfitLossResponse());
        }
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryValuationResponse>> getInventoryValuationReport() {
        try {
            return ResponseEntity.ok(reportService.generateInventoryValuationReport());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/sales")
    public ResponseEntity<List<SalesReportResponse>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseEntity.ok(reportService.generateSalesReport(startDate, endDate, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductPerformanceResponse>> getProductPerformanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseEntity.ok(reportService.generateProductPerformanceReport(startDate, endDate, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierPurchaseResponse>> getSupplierPurchaseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseEntity.ok(reportService.generateSupplierPurchaseReport(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<DashboardResponse> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            return ResponseEntity.ok(reportService.generateDashboardSummary(date, date));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DashboardResponse());
        }
    }

    @PostMapping("/export")
    public ResponseEntity<Resource> exportReport(@RequestBody ExportReportRequest request) {
        try {
            return reportService.exportReport(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}