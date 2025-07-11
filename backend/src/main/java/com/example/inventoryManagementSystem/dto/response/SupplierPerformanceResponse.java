package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SupplierPerformanceResponse {
    private Long supplierId;
    private String supplierName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
    private BigDecimal onTimeDeliveryRate;
}