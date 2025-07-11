package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be less than 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be less than 50 characters")
    private String sku;

    @Size(max = 50, message = "Barcode must be less than 50 characters")
    private String barcode;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive or zero")
    private Double price;

    private String imageUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost price must be positive or zero")
    private Double costPrice;

    @Min(value = 0, message = "Quantity must be positive or zero")
    private Integer quantityInStock;

    @Min(value = 0, message = "Low stock threshold must be positive or zero")
    private Integer lowStockThreshold;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long brandId;

    @NotNull(message = "Unit ID is required")
    private Long unitId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;
}