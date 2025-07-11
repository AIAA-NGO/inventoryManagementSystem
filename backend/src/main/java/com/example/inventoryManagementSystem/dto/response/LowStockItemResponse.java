package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class LowStockItemResponse {
    private Long productId;
    private String productName;
    private String productImage;
    private int currentStock;
    private int lowStockThreshold;
    private String sku;
    private int threshold;
    private String category;
}