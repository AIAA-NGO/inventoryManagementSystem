package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LowStockSuggestionResponse {
    private Long productId;
    private String productName;
    private String sku;
    private Integer currentStock;
    private Integer lowStockThreshold;
    private Integer suggestedOrderQuantity;
    private String supplierName;
    private Long supplierId;
    private BigDecimal costPrice;
    private BigDecimal estimatedTotal;
}