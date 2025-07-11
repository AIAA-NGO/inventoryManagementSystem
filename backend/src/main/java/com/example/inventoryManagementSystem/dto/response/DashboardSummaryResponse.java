package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryResponse {
    private BigDecimal totalSalesAmount;
    private BigDecimal totalDiscounts;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private long totalSalesCount;
    private long totalInventoryItems;
    private long totalCustomers;
    private long expiredItemsCount;
    private long lowStockItemsCount;
}