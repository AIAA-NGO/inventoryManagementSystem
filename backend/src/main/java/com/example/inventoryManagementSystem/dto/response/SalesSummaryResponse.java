package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SalesSummaryResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int totalOrders;
    private int newCustomers;
    private BigDecimal totalRevenue;
    private BigDecimal totalTax;
    private BigDecimal totalProfit;
    private BigDecimal grossProfit;
    private BigDecimal netProfit;
    private BigDecimal averageOrderValue;
    private Long saleId;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal preTaxAmount;
    private BigDecimal taxAmount;
    private LocalDateTime saleTime;
    private BigDecimal profit;
}