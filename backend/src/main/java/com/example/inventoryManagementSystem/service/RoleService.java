package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.RoleRequest;
import com.example.inventoryManagementSystem.dto.response.RoleResponse;
import com.example.inventoryManagementSystem.exception.DuplicateResourceException;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;

import java.util.List;

public interface RoleService {
    RoleResponse createRole(RoleRequest request) throws DuplicateResourceException;
    List<RoleResponse> getAllRoles();
    RoleResponse updateRole(Integer id, RoleRequest request) throws DuplicateResourceException, ResourceNotFoundException;
    void deleteRole(Integer id) throws ResourceNotFoundException;
}