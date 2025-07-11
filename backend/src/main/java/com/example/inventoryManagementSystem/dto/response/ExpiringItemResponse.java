package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ExpiringItemResponse {
    private Long productId;
    private String productName;
    private String productImage;
    private LocalDate expiryDate;
    private int remainingDays;
    private int currentStock;
}