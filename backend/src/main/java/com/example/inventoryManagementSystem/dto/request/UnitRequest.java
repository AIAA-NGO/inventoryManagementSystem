package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnitRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Abbreviation is required")
    private String abbreviation;
}