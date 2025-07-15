package com.example.inventoryManagementSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object (DTO) for representing a dashboard summary.
 * Contains key financial and inventory metrics for a specified period.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    /**
     * The start date of the reporting period.
     */
    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    /**
     * The end date of the reporting period.
     */
    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    /**
     * Total number of completed sales in the period.
     */
    @NotNull(message = "Total sales count is required")
    private Integer totalSalesCount;

    /**
     * Total revenue from sales in the period.
     */
    @NotNull(message = "Total revenue is required")
    private BigDecimal totalRevenue;

    /**
     * Total operating expenses incurred in the period.
     */
    @NotNull(message = "Total expenses are required")
    private BigDecimal totalExpenses;

    /**
     * Net profit, calculated as gross profit minus expenses.
     */
    @NotNull(message = "Net profit is required")
    private BigDecimal netProfit;

    /**
     * Number of top-performing products included in the report.
     */
    @NotNull(message = "Top products count is required")
    private Integer topProductsCount;

    /**
     * Number of products with stock below reorder level.
     */
    @NotNull(message = "Low stock items count is required")
    private Integer lowStockItemsCount;

    /**
     * Total value of inventory at the end of the period.
     */
    @NotNull(message = "Inventory value is required")
    private BigDecimal inventoryValue;

    /**
     * List of top-performing products in the period.
     */
    @NotNull(message = "Top products list is required")
    private List<ProductPerformanceResponse> topProducts;
}