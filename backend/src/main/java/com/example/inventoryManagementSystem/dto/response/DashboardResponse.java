package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int totalSalesCount;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private int topProductsCount;
    private int lowStockItemsCount;
    private BigDecimal inventoryValue;
    private List<ProductPerformanceResponse> topProducts;

}