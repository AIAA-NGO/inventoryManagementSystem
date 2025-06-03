package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SupplierRequest {
    @NotBlank
    private String name;

    private String contactPerson;
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s-]+$")
    private String phone;

    private String address;
}
