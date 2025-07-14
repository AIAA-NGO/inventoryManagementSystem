package com.example.inventoryManagementSystem.dto.response;

import com.example.inventoryManagementSystem.model.SaleSummary;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DailySummaryResponse {
    private LocalDate date;
    private int totalSales;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal totalProfit;
    private List<SaleSummary> sales;
}