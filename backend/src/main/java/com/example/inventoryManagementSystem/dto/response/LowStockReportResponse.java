package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LowStockReportResponse {
    private Long productId;
    private String productName;
    private int currentStock;
    private int reorderLevel;
}