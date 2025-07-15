package com.example.inventoryManagementSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for representing a profit and loss report.
 * Contains financial metrics for a specified period, including revenue, costs, and profit calculations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitLossResponse {
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
     * Total revenue from sales (gross sales).
     */
    @NotNull(message = "Total revenue is required")
    private BigDecimal totalRevenue;

    /**
     * Total cost of goods sold (COGS), calculated as beginning inventory + purchases - ending inventory.
     */
    @NotNull(message = "Total cost is required")
    private BigDecimal totalCost;

    /**
     * Inventory value at the start of the period.
     */
    @NotNull(message = "Beginning inventory is required")
    private BigDecimal beginningInventory;

    /**
     * Total purchases made during the period.
     */
    @NotNull(message = "Purchases are required")
    private BigDecimal purchases;

    /**
     * Inventory value at the end of the period.
     */
    @NotNull(message = "Ending inventory is required")
    private BigDecimal endingInventory;

    /**
     * Gross profit, calculated as total revenue - total cost.
     */
    @NotNull(message = "Gross profit is required")
    private BigDecimal grossProfit;

    /**
     * Total operating expenses incurred during the period.
     */
    @NotNull(message = "Expenses are required")
    private BigDecimal expenses;

    /**
     * Other income (e.g., interest, rentals) during the period.
     */
    @NotNull(message = "Other income is required")
    private BigDecimal otherIncome;

    /**
     * Other non-operating expenses during the period.
     */
    @NotNull(message = "Other expenses are required")
    private BigDecimal otherExpenses;

    /**
     * Taxes incurred during the period.
     */
    @NotNull(message = "Taxes are required")
    private BigDecimal taxes;

    /**
     * Net profit, calculated as gross profit + other income - expenses - other expenses - taxes.
     */
    @NotNull(message = "Net profit is required")
    private BigDecimal netProfit;

    /**
     * Gross margin percentage, calculated as (gross profit / total revenue) * 100.
     */
    @NotNull(message = "Gross margin percentage is required")
    private BigDecimal grossMarginPercentage;

    /**
     * Net profit percentage, calculated as (net profit / total revenue) * 100.
     */
    @NotNull(message = "Net profit percentage is required")
    private BigDecimal netProfitPercentage;
}