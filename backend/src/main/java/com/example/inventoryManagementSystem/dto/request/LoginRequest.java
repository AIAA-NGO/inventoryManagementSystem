package com.example.inventoryManagementSystem.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}