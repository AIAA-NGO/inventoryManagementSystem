package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.ExportReportRequest;
import com.example.inventoryManagementSystem.dto.response.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<SalesReportResponse> generateSalesReport(LocalDate startDate, LocalDate endDate, String statusFilter);
    SalesSummaryResponse generateSalesSummaryReport(LocalDate startDate, LocalDate endDate);
    List<ProductPerformanceResponse> generateProductPerformanceReport(LocalDate startDate, LocalDate endDate, Long categoryId);
    List<ProductSalesTrendResponse> generateProductSalesTrendReport(LocalDate startDate, LocalDate endDate, Long productId);
    List<InventoryValuationResponse> generateInventoryValuationReport();
    List<LowStockReportResponse> generateLowStockReport(int threshold);
    List<ExpiringItemsReportResponse> generateExpiringItemsReport(LocalDate cutoffDate);
    ProfitLossResponse generateProfitLossReport(LocalDate startDate, LocalDate endDate);
    CashFlowResponse generateCashFlowReport(LocalDate startDate, LocalDate endDate);
    List<SupplierPurchaseResponse> generateSupplierPurchaseReport(LocalDate startDate, LocalDate endDate);
    SupplierPerformanceResponse generateSupplierPerformanceReport(LocalDate startDate, LocalDate endDate, Long supplierId);
    BusinessPerformanceResponse generateBusinessPerformanceReport(LocalDate startDate, LocalDate endDate);
    DashboardResponse generateDashboardSummary(LocalDate startDate, LocalDate endDate);
    List<SalesTrendResponse> generateSalesTrendReport(LocalDate startDate, LocalDate endDate, String period);
    ResponseEntity<Resource> exportReport(ExportReportRequest request);
    ResponseEntity<Resource> exportDashboardAsPdf(LocalDate startDate, LocalDate endDate);
}