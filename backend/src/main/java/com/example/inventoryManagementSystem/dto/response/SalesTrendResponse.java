package com.example.inventoryManagementSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTrendResponse {
    private String period;
    private BigDecimal totalSales;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodLabel;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int saleCount;
    private BigDecimal amount;
    private Long salesCount;




    // Constructor for database results
    public SalesTrendResponse(String period, BigDecimal totalSales, LocalDate startDate, LocalDate endDate) {
        this.period = period;
        this.totalSales = totalSales;
        this.startDate = startDate;
        this.endDate = endDate;
        this.periodLabel = period; // Default to period if not set separately
        this.periodStart = startDate;
        this.periodEnd = endDate;
    }

    // Builder pattern
    public static SalesTrendResponseBuilder builder() {
        return new SalesTrendResponseBuilder();
    }

    // Getters and setters
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public int getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(int saleCount) {
        this.saleCount = saleCount;
    }

    public String getFormattedAmount() {
        return String.format("KSH %,.2f", amount);
    }

    // Builder class
    public static class SalesTrendResponseBuilder {
        private String periodLabel;
        private BigDecimal totalSales;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private int saleCount;

        public SalesTrendResponseBuilder periodLabel(String periodLabel) {
            this.periodLabel = periodLabel;
            return this;
        }

        public SalesTrendResponseBuilder totalSales(BigDecimal totalSales) {
            this.totalSales = totalSales;
            return this;
        }

        public SalesTrendResponseBuilder periodStart(LocalDate periodStart) {
            this.periodStart = periodStart;
            return this;
        }

        public SalesTrendResponseBuilder periodEnd(LocalDate periodEnd) {
            this.periodEnd = periodEnd;
            return this;
        }

        public SalesTrendResponseBuilder saleCount(int saleCount) {
            this.saleCount = saleCount;
            return this;
        }

        public SalesTrendResponse build() {
            SalesTrendResponse response = new SalesTrendResponse(
                    periodLabel,
                    totalSales,
                    periodStart,
                    periodEnd
            );
            response.setPeriodLabel(periodLabel);
            response.setPeriodStart(periodStart);
            response.setPeriodEnd(periodEnd);
            response.setSaleCount(saleCount);
            return response;
        }
    }
}