package com.example.inventoryManagementSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitLossResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal grossProfit;
    private BigDecimal expenses;
    private BigDecimal otherIncome;
    private BigDecimal otherExpenses;
    private BigDecimal netProfitBeforeTax;
    private BigDecimal taxAmount;
    private BigDecimal netProfit;
    private BigDecimal grossMarginPercentage;
    private BigDecimal netProfitPercentage;
}