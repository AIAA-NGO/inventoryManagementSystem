package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
}