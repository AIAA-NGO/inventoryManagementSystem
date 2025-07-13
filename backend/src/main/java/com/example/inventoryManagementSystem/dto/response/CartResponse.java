package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;      // tax-exclusive amount
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;        // tax-inclusive amount
    private BigDecimal preTaxAmount; // same as subtotal (for internal use)
}