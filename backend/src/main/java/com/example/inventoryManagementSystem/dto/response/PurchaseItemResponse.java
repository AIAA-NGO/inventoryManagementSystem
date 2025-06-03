package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;

@Data
public class PurchaseItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
}