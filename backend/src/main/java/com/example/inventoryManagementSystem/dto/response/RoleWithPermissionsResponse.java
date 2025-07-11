package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleWithPermissionsResponse {
    private Integer id;
    private String name;
    private Set<PermissionDto> permissions;
}