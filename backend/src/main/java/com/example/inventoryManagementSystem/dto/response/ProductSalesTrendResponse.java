package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProductSalesTrendResponse {
    private LocalDate date;
    private Long productId;
    private String productName;
    private int unitsSold;
}