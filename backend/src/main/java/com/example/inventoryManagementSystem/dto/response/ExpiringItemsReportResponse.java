package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ExpiringItemsReportResponse {
    private Long productId;
    private String productName;
    private int quantity;
    private LocalDate expiryDate;
    private int daysUntilExpiry;
}