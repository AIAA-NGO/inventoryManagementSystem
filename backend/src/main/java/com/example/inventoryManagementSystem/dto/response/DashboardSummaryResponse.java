package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private int totalSales;
    private int totalProducts;
    private int lowStockItems;
    private int pendingPurchases;
}

