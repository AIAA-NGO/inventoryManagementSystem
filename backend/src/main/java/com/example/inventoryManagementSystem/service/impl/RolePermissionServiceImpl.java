package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.repository.PermissionRepository;
import com.example.inventoryManagementSystem.repository.RoleRepository;
import com.example.inventoryManagementSystem.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public Role assignPermissionsToRole(Integer roleId, Set<Integer> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Set<Permission> permissions = permissionRepository.findByIds(permissionIds);
        role.getPermissions().addAll(permissions);

        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role revokePermissionsFromRole(Integer roleId, Set<Integer> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.getPermissions().removeIf(
                permission -> permissionIds.contains(permission.getId())
        );

        return roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getPermissionsForRole(Integer roleId) {
        return roleRepository.findById(roleId)
                .map(role -> role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet()))
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Integer roleId, String permissionName) {
        return roleRepository.findById(roleId)
                .map(role -> role.getPermissions().stream()
                        .anyMatch(permission -> permission.getName().equals(permissionName)))
                .orElse(false);
    }
}