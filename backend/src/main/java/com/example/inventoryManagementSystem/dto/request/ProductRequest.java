package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String barcode;

    @PositiveOrZero(message = "Price must be positive or zero")
    private Double price;

    @PositiveOrZero(message = "Cost price must be positive or zero")
    private Double costPrice;

    @PositiveOrZero(message = "Quantity must be positive or zero")
    private Integer quantityInStock;

    @PositiveOrZero(message = "Low stock threshold must be positive or zero")
    private Integer lowStockThreshold;

    private Long categoryId;
    private Long brandId;
    private Long unitId;

    private Long supplierId;
}