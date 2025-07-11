package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.RolePermissionRequest;
import com.example.inventoryManagementSystem.dto.request.RoleRequest;
import com.example.inventoryManagementSystem.dto.response.RoleResponse;
import com.example.inventoryManagementSystem.exception.DuplicateResourceException;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Set;

public interface RoleService {
    RoleResponse createRole(RoleRequest request) throws DuplicateResourceException;
    List<RoleResponse> getAllRoles();
    RoleResponse getRoleById(Integer id) throws ResourceNotFoundException;
    RoleResponse updateRole(Integer id, RoleRequest request) throws DuplicateResourceException, ResourceNotFoundException;
    void deleteRole(Integer id) throws ResourceNotFoundException;
    RoleResponse assignPermissions(Integer roleId, RolePermissionRequest request) throws ResourceNotFoundException;
    RoleResponse removePermissions(Integer roleId, RolePermissionRequest request) throws ResourceNotFoundException;
    Set<String> getRolePermissions(Integer roleId) throws ResourceNotFoundException;
}