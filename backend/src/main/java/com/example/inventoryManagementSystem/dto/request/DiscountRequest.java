package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DiscountRequest {
    @NotBlank
    private String discountCode;
}
