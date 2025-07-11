package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Mpesarequest {

    private int amount;
    @NotNull(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:254|\\+254|0)?(7[0-9]{8}|1[0-9]{8})$",
            message = "Invalid phone number format"
    )
    private String phonenumber;
}
