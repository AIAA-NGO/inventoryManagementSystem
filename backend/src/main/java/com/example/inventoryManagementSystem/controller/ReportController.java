package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.ExportReportRequest;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Sales Reports
    @GetMapping("/sales/daily")
    public ResponseEntity<List<SalesReportResponse>> getDailySalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String statusFilter) {
        return ResponseEntity.ok(reportService.generateSalesReport(startDate, endDate, statusFilter));
    }

    @GetMapping("/sales/summary")
    public ResponseEntity<SalesSummaryResponse> getSalesSummaryReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateSalesSummaryReport(startDate, endDate));
    }

    // Product Reports
    @GetMapping("/products/performance")
    public ResponseEntity<List<ProductPerformanceResponse>> getProductPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(reportService.generateProductPerformanceReport(startDate, endDate, categoryId));
    }

    @GetMapping("/products/sales-trend")
    public ResponseEntity<List<ProductSalesTrendResponse>> getProductSalesTrendReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(reportService.generateProductSalesTrendReport(startDate, endDate, productId));
    }

    // Inventory Reports
    @GetMapping("/inventory/valuation")
    public ResponseEntity<List<InventoryValuationResponse>> getInventoryValuationReport() {
        return ResponseEntity.ok(reportService.generateInventoryValuationReport());
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<LowStockReportResponse>> getLowStockReport(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(reportService.generateLowStockReport(threshold));
    }

    @GetMapping("/inventory/expiring-items")
    public ResponseEntity<List<ExpiringItemsReportResponse>> getExpiringItemsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cutoffDate) {
        return ResponseEntity.ok(reportService.generateExpiringItemsReport(cutoffDate));
    }

    // Financial Reports
//    @GetMapping("/financial/profit-loss")
//    public ResponseEntity<ProfitLossResponse> getProfitLossReport(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//        return ResponseEntity.ok(reportService.generateProfitLossReport(startDate, endDate));
//    }



    @GetMapping("/financial/cash-flow")
    public ResponseEntity<CashFlowResponse> getCashFlowReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateCashFlowReport(startDate, endDate));
    }

    // Supplier Reports
    @GetMapping("/suppliers/purchases")
    public ResponseEntity<List<SupplierPurchaseResponse>> getSupplierPurchaseReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateSupplierPurchaseReport(startDate, endDate));
    }

    @GetMapping("/suppliers/performance")
    public ResponseEntity<SupplierPerformanceResponse> getSupplierPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Long supplierId) {
        return ResponseEntity.ok(reportService.generateSupplierPerformanceReport(startDate, endDate, supplierId));
    }

    // Business Intelligence
    @GetMapping("/business/performance")
    public ResponseEntity<BusinessPerformanceResponse> getBusinessPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateBusinessPerformanceReport(startDate, endDate));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateDashboardSummary(startDate, endDate));
    }

    @GetMapping("/sales/trend")
    public ResponseEntity<List<SalesTrendResponse>> getSalesTrendReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String period) {
        return ResponseEntity.ok(reportService.generateSalesTrendReport(startDate, endDate, period));
    }

    // Export Functionality
    @PostMapping("/export")
    public ResponseEntity<Resource> exportReport(@RequestBody ExportReportRequest request) {
        return reportService.exportReport(request);
    }

    @GetMapping("/export/sales")
    public ResponseEntity<Resource> exportSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String format) {
        ExportReportRequest request = new ExportReportRequest();
        request.setReportType(ExportReportRequest.ReportType.SALES);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setFormat(ExportReportRequest.FormatType.valueOf(format.toUpperCase()));
        return reportService.exportReport(request);
    }

    @GetMapping("/export/dashboard")
    public ResponseEntity<Resource> exportDashboardAsPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.exportDashboardAsPdf(startDate, endDate);
    }

    // Error handling endpoint
    @GetMapping("/products/performance/error-handling")
    public ResponseEntity<?> getProductPerformanceReportWithErrorHandling(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<ProductPerformanceResponse> reportData = reportService.generateProductPerformanceReport(start, end, null);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(reportData);

        } catch (Exception e) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Failed to generate report: " + e.getMessage());
            problemDetail.setTitle("Report Generation Error");
            return ResponseEntity.badRequest()
                    .body(problemDetail);
        }
    }
}