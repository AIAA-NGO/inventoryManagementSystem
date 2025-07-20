package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private BigDecimal preTaxSubtotal;
    private BigDecimal productDiscountAmount;
    private BigDecimal cartDiscountAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String appliedDiscountCode;
}