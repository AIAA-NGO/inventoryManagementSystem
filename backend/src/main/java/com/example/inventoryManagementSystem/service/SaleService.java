package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.AddToCartRequest;
import com.example.inventoryManagementSystem.dto.request.ApplyDiscountRequest;
import com.example.inventoryManagementSystem.dto.request.SaleRequest;
import com.example.inventoryManagementSystem.dto.response.CartResponse;
import com.example.inventoryManagementSystem.dto.response.DailySummaryResponse;
import com.example.inventoryManagementSystem.dto.response.ReceiptResponse;
import com.example.inventoryManagementSystem.dto.response.SaleResponse;

import java.time.LocalDate;
import java.util.List;

public interface SaleService {
    SaleResponse createSale(SaleRequest request);
    List<SaleResponse> getAllSales(LocalDate startDate, LocalDate endDate);

    SaleResponse getSaleById(Long id);
    SaleResponse cancelSale(Long id);
    DailySummaryResponse getDailySummary(LocalDate date);
    SaleResponse applyDiscount(Long saleId, ApplyDiscountRequest request);
    CartResponse addToCart(AddToCartRequest request);
    ReceiptResponse generateReceipt(Long saleId);
    SaleResponse refundSale(Long saleId);
    SaleResponse updateSale(Long id, SaleRequest saleRequest);
    void deleteSale(Long id);
    List<SaleResponse> getSalesByCustomer(Long customerId);
    List<SaleResponse> getSalesByStatus(String status);
    List<SaleResponse> getSalesByDateRange(LocalDate startDate, LocalDate endDate);
}