
package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalesReportResponse {
    private LocalDate date;
    private int orderCount;
    private BigDecimal totalSales;
    private BigDecimal totalTax;
    private BigDecimal totalProfit;
    private BigDecimal grossProfit;
    private BigDecimal netProfit;
}