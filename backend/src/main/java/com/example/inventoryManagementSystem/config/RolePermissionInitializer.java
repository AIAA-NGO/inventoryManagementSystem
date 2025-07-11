package com.example.inventoryManagementSystem.config;

import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.model.Role.ERole;
import com.example.inventoryManagementSystem.repository.PermissionRepository;
import com.example.inventoryManagementSystem.repository.RoleRepository;
import com.example.inventoryManagementSystem.security.AppPermissions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePermissionInitializer {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @PostConstruct
    @Transactional
    public void init() {
        // Ensure all permissions exist
        Set<String> allPermissionNames = AppPermissions.getAllPermissions();
        Set<Permission> allPermissions = new HashSet<>();

        for (String name : allPermissionNames) {
            Permission permission = permissionRepository.findByName(name)
                    .orElseGet(() -> {
                        Permission newPermission = new Permission();
                        newPermission.setName(name);
                        newPermission.setDescription("System permission: " + name);
                        return permissionRepository.save(newPermission);
                    });
            // Clear the roles collection to avoid lazy loading issues
            permission.setRoles(new HashSet<>());
            allPermissions.add(permission);
        }

        // Initialize ADMIN role with all permissions
        Role adminRole = roleRepository.findByName(ERole.ADMIN)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(ERole.ADMIN);
                    role.setPermissions(new HashSet<>());
                    return roleRepository.save(role);
                });
        adminRole.setPermissions(allPermissions);
        roleRepository.save(adminRole);

        // Initialize other roles with their default permissions
        initializeRoleWithPermissions(ERole.MANAGER, AppPermissions.getManagerPermissions());
        initializeRoleWithPermissions(ERole.CASHIER, AppPermissions.getCashierPermissions());
        initializeRoleWithPermissions(ERole.RECEIVING_CLERK, AppPermissions.getReceivingClerkPermissions());
    }

    private void initializeRoleWithPermissions(ERole roleName, Set<String> permissionNames) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    newRole.setPermissions(new HashSet<>());
                    return roleRepository.save(newRole);
                });

        Set<Permission> permissions = new HashSet<>();
        for (String name : permissionNames) {
            Permission permission = permissionRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("Permission not found: " + name));
            // Clear the roles collection to avoid lazy loading issues
            permission.setRoles(new HashSet<>());
            permissions.add(permission);
        }

        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}