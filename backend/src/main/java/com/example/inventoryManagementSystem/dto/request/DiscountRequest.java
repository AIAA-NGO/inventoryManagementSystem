package com.example.inventoryManagementSystem.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiscountRequest {
    private String code;
    private double percentage;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String description;
    private List<Long> productIds;
}
