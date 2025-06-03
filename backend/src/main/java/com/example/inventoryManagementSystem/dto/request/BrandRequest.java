package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}