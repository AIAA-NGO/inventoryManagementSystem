package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class DiscountResponse {
    private Long id;
    private String code;
    private double percentage;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String description;
    private boolean active;
    private Set<Long> productIds; // Only store IDs to avoid circular references
}