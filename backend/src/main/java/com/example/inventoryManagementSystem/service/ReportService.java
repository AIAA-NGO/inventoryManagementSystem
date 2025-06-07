package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.ExportReportRequest;
import com.example.inventoryManagementSystem.dto.response.*;
import com.example.inventoryManagementSystem.dto.response.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<SalesReportResponse> generateSalesReport(LocalDate startDate, LocalDate endDate);
    List<ProductPerformanceResponse> generateProductPerformanceReport(LocalDate startDate, LocalDate endDate);
    List<InventoryValuationResponse> generateInventoryValuationReport();
    ProfitLossResponse generateProfitLossReport(LocalDate startDate, LocalDate endDate);
    TaxReportResponse generateTaxReport(LocalDate startDate, LocalDate endDate);
    List<SupplierPurchaseResponse> generateSupplierPurchaseReport(LocalDate startDate, LocalDate endDate);
    ResponseEntity<Resource> exportReport(ExportReportRequest request);
}