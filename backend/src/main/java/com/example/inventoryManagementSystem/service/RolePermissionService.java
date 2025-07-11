package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.RolePermissionRequest;
import com.example.inventoryManagementSystem.dto.response.PermissionDto;
import com.example.inventoryManagementSystem.dto.response.RoleWithPermissionsResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;

import java.util.List;

public interface RolePermissionService {
    List<PermissionDto> getAllPermissions();
    RoleWithPermissionsResponse assignPermissionsToRole(RolePermissionRequest request) throws ResourceNotFoundException;
    RoleWithPermissionsResponse getRoleWithPermissions(Integer roleId) throws ResourceNotFoundException;
    void initializeRolesAndPermissions();
    RoleWithPermissionsResponse removePermissionsFromRole(RolePermissionRequest request) throws ResourceNotFoundException;
    RoleWithPermissionsResponse updateRolePermissions(Integer roleId, RolePermissionRequest request) throws ResourceNotFoundException;
}