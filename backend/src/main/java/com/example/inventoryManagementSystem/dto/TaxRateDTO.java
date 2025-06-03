package com.example.inventoryManagementSystem.dto;

import com.example.inventoryManagementSystem.validation.ValidTaxRate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ValidTaxRate
public class TaxRateDTO {
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private Double rate;

    private String description;
    private Boolean isActive;
}