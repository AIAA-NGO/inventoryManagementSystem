package com.example.inventoryManagementSystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private String supplierContactPerson;
    private String supplierEmail;
    private String supplierPhone;
    private String supplierAddress;
    private String supplierWebsite;

    private Long categoryId;
    private String categoryName;
    private CategoryResponse category;
    private Long brandId;
    private String brandName;

    private Long unitId;
    private String unitName;
    private String unitAbbreviation;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDate expiryDate;
    private String imageUrl;
}