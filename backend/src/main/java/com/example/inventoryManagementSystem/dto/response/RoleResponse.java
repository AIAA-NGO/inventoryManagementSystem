package com.example.inventoryManagementSystem.dto.response;

import com.example.inventoryManagementSystem.model.Role.ERole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    private Integer id;
    private ERole name;
    private Set<String> permissions;
}