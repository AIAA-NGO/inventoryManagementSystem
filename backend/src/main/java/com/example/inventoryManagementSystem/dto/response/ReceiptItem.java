package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReceiptItem {
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
}