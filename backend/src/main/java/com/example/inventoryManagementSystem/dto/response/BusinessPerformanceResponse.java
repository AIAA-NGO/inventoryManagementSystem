package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BusinessPerformanceResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalRevenue;
    private BigDecimal totalCostOfGoodsSold;
    private BigDecimal grossProfit;
    private BigDecimal operatingExpenses;
    private BigDecimal netProfitBeforeTax;
    private BigDecimal taxExpense;
    private BigDecimal netProfit;
    private BigDecimal grossMarginPercentage;
    private BigDecimal netProfitPercentage;
    private BigDecimal operatingExpenseRatio;
    private BigDecimal inventoryTurnover;




}