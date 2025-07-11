
package com.example.inventoryManagementSystem.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExportReportRequest {
    private ReportType reportType;
    private FormatType format;
    private LocalDate startDate;
    private LocalDate endDate;

    public enum ReportType {
        SALES, PRODUCTS, INVENTORY, PROFIT_LOSS, TAX, SUPPLIERS, PRODUCT_SALES_TREND, LOW_STOCK, SALES_TREND, CASH_FLOW, SUPPLIER_PURCHASES, EXPIRING_ITEMS, SALES_SUMMARY, BUSINESS_PERFORMANCE
    }

    public enum FormatType {
        PDF, EXCEL, CSV
    }
}
