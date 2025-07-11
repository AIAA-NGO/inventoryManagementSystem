package com.example.inventoryManagementSystem.dto.request;

import com.example.inventoryManagementSystem.model.Role.ERole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleRequest {
    @NotNull(message = "Role name is required")
    private ERole name;
}