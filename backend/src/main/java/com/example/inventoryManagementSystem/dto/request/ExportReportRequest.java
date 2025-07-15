package com.example.inventoryManagementSystem.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class ExportReportRequest {
    @NotNull(message = "Report type is required")
    private ReportType reportType;

    @NotNull(message = "Format type is required")
    private FormatType format;

    private LocalDate startDate;
    private LocalDate endDate;

    public enum ReportType {
        PROFIT_LOSS,
        SALES,
        SALES_SUMMARY,
        PRODUCTS,
        PRODUCT_SALES_TREND,
        INVENTORY,
        LOW_STOCK,
        EXPIRING_ITEMS,
        SUPPLIERS,
        SUPPLIER_PURCHASES,
        CASH_FLOW,
        BUSINESS_PERFORMANCE,
        SALES_TREND,
        DAILY_SUMMARY
    }

    public enum FormatType {
        PDF,
        EXCEL,
        CSV
    }
}