
package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryStatusResponse {
    private Long id;
    private String name;
    private String sku;
    private String barcode;
    private Integer quantityInStock;
    private Integer lowStockThreshold;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String brandName;
    private String categoryName;
    private String unitName;
    private String stockStatus;
    private LocalDate expiryDate;
    private Boolean isExpired;

}