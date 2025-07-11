package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SupplierPurchaseResponse {
    private Long supplierId;
    private String supplierName;
    private int purchaseCount;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
}
