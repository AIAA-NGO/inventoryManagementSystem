package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionDto {
    private Integer id;
    private String name;
    private String description;
}