package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/roles/{roleId}/permissions")
@RequiredArgsConstructor
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('role_update')")
    public Role assignPermissions(
            @PathVariable Integer roleId,
            @RequestBody Set<Integer> permissionIds) {
        return rolePermissionService.assignPermissionsToRole(roleId, permissionIds);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('role_update')")
    public Role revokePermissions(
            @PathVariable Integer roleId,
            @RequestBody Set<Integer> permissionIds) {
        return rolePermissionService.revokePermissionsFromRole(roleId, permissionIds);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permission_view')")
    public Set<String> getRolePermissions(@PathVariable Integer roleId) {
        return rolePermissionService.getPermissionsForRole(roleId);
    }
}