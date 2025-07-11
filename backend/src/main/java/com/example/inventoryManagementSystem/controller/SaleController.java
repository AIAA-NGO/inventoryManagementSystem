package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.ApplyDiscountRequest;
import com.example.inventoryManagementSystem.dto.request.SaleRequest;
import com.example.inventoryManagementSystem.dto.response.DailySummaryResponse;
import com.example.inventoryManagementSystem.dto.response.ReceiptResponse;
import com.example.inventoryManagementSystem.dto.response.SaleResponse;
import com.example.inventoryManagementSystem.dto.response.SalesTrendResponse;
import com.example.inventoryManagementSystem.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @GetMapping
    public ResponseEntity<List<SaleResponse>> getAllSales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(saleService.getAllSales(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@RequestBody SaleRequest saleRequest) {
        return ResponseEntity.ok(saleService.createSale(saleRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponse> updateSale(
            @PathVariable Long id,
            @RequestBody SaleRequest saleRequest) {
        return ResponseEntity.ok(saleService.updateSale(id, saleRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return ResponseEntity.ok(saleService.getDailySummary(date));
    }

    @PostMapping("/apply-discount")
    public ResponseEntity<SaleResponse> applyDiscount(
            @RequestParam Long saleId,
            @RequestBody ApplyDiscountRequest request) {
        return ResponseEntity.ok(saleService.applyDiscount(saleId, request));
    }

    @GetMapping("/receipt/{id}")
    public ResponseEntity<ReceiptResponse> generateReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.generateReceipt(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SaleResponse>> getSalesByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(saleService.getSalesByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SaleResponse>> getSalesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(saleService.getSalesByStatus(status));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SaleResponse>> getSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(saleService.getSalesByDateRange(startDate, endDate));
    }

    @GetMapping("/trend")
    public ResponseEntity<List<SalesTrendResponse>> getSalesTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "day") String periodType) {
        return ResponseEntity.ok(saleService.getSalesTrend(startDate, endDate, periodType));
    }
}