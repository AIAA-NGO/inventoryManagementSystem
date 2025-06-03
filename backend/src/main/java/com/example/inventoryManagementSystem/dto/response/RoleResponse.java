package com.example.inventoryManagementSystem.dto.response;

import com.example.inventoryManagementSystem.model.Role.ERole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private Integer id;
    private ERole name;
}