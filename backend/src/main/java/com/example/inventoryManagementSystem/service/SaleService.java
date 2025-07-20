package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.ApplyDiscountRequest;
import com.example.inventoryManagementSystem.dto.request.SaleRequest;
import com.example.inventoryManagementSystem.dto.response.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleService {
    SaleResponse createSale(SaleRequest request);
    List<SaleResponse> getAllSales(LocalDate startDate, LocalDate endDate);
    SaleResponse getSaleById(Long id);
    SaleResponse cancelSale(Long id);
    SaleResponse refundSale(Long saleId);
    SaleResponse updateSale(Long id, SaleRequest saleRequest);
    void deleteSale(Long id);

    DailySummaryResponse getDailySummary(LocalDate date);
    List<SalesTrendResponse> getSalesTrend(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String periodType
    );
    ReceiptResponse generateReceipt(Long saleId);
    List<SaleResponse> getSalesByCustomer(Long customerId);
    List<SaleResponse> getSalesByStatus(String status);
    List<SaleResponse> getSalesByDateRange(LocalDate startDate, LocalDate endDate);
    SaleResponse applyDiscount(Long saleId, ApplyDiscountRequest request);
}