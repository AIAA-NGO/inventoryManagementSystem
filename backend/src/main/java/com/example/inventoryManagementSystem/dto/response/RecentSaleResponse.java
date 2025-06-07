package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RecentSaleResponse {
    private Long saleId;
    private String customerName;
    private LocalDateTime saleDate;
    private BigDecimal amount;
    private String status;
}
