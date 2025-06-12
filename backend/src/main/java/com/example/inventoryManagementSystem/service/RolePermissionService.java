package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.model.Role;

import java.util.Set;

public interface RolePermissionService {
    Role assignPermissionsToRole(Integer roleId, Set<Integer> permissionIds);
    Role revokePermissionsFromRole(Integer roleId, Set<Integer> permissionIds);
    Set<String> getPermissionsForRole(Integer roleId);
    boolean hasPermission(Integer roleId, String permissionName);

}