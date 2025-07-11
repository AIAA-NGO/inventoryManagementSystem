package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolePermissionRequest {
    @NotNull(message = "Role ID cannot be null")
    private Integer roleId;

    @NotEmpty(message = "Permission IDs cannot be empty")
    private Set<Integer> permissionIds;
}