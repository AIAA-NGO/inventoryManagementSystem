package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TopProductResponse {
    private Long productId;
    private String productName;
    private String productImage;
    private int unitsSold;
    private BigDecimal revenue;
}