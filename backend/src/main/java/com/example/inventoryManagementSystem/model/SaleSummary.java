package com.example.inventoryManagementSystem.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SaleSummary {
    private Long saleId;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private LocalDateTime saleTime;
    private BigDecimal preTaxAmount;
    private BigDecimal taxAmount;
    private BigDecimal profit;
}