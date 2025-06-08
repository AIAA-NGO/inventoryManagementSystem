package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.ExportReportRequest;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.service.ReportService;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public ResponseEntity<List<SalesReportResponse>> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateSalesReport(startDate, endDate));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductPerformanceResponse>> getProductPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateProductPerformanceReport(startDate, endDate));
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryValuationResponse>> getInventoryValuationReport() {
        return ResponseEntity.ok(reportService.generateInventoryValuationReport());
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<ProfitLossResponse> getProfitLossReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateProfitLossReport(startDate, endDate));
    }

    @GetMapping("/tax")
    public ResponseEntity<TaxReportResponse> getTaxReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateTaxReport(startDate, endDate));
    }

    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierPurchaseResponse>> getSupplierPurchaseReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateSupplierPurchaseReport(startDate, endDate));
    }

    @PostMapping("/export")
    public ResponseEntity<Resource> exportReport(@RequestBody ExportReportRequest request) {
        return reportService.exportReport(request);
    }
}