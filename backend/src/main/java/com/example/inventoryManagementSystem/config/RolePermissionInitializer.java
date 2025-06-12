package com.example.inventoryManagementSystem.config;

import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.model.Role.ERole;
import com.example.inventoryManagementSystem.repository.PermissionRepository;
import com.example.inventoryManagementSystem.repository.RoleRepository;
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
    public void initAdminPermissions() {
        // Ensure ADMIN role exists and has all permissions
        Role adminRole = roleRepository.findByName(ERole.ADMIN)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(ERole.ADMIN);
                    return roleRepository.save(role);
                });

        // Get all permissions
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

        // Assign all permissions to admin if not already assigned
        if (!adminRole.getPermissions().containsAll(allPermissions)) {
            adminRole.getPermissions().addAll(allPermissions);
            roleRepository.save(adminRole);
        }
    }
}