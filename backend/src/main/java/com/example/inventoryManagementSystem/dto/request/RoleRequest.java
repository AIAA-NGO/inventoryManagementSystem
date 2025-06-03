package com.example.inventoryManagementSystem.dto.request;

import com.example.inventoryManagementSystem.model.Role.ERole;
import lombok.Data;

@Data
public class RoleRequest {
    private ERole name;
}