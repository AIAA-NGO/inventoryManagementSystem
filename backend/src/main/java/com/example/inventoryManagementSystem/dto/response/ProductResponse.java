package com.example.inventoryManagementSystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private String barcode;
    private Double price;
    private Double costPrice;
    private Integer quantityInStock;
    private Integer lowStockThreshold;
    private Long supplierId;
    private String supplierName;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long unitId;
    private String unitName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}