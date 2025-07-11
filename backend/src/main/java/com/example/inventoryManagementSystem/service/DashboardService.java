package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.response.*;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getDashboardSummary();
    List<SalesTrendResponse> getSalesTrend(String periodType);
    List<TopProductResponse> getTopSellingProducts(int limit);
    List<LowStockItemResponse> getCriticalLowStockItems();
    List<RecentSaleResponse> getRecentSales(int limit);
    List<ExpiringItemResponse> getSoonToExpireItems();

}