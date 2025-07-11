package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TaxReportResponse {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal taxableSales;
    private BigDecimal taxCollected;
    private String taxRate;
    private BigDecimal taxLiability;
    private BigDecimal taxCredits;

    public BigDecimal getTaxCredits() {
        return taxCredits;
    }
}