package com.example.inventoryManagementSystem.config;

import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.repository.PermissionRepository;
import com.example.inventoryManagementSystem.security.AppPermissions;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionDataLoader {
    private final PermissionRepository permissionRepository;

    @PostConstruct
    @Transactional
    public void initPermissions() {
        Set<String> allPermissions = AppPermissions.getAllPermissions();

        // Get existing permissions
        Set<String> existingPermissions = permissionRepository.findAll().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        // Create missing permissions
        allPermissions.stream()
                .filter(permission -> !existingPermissions.contains(permission))
                .forEach(permission -> {
                    Permission newPermission = new Permission();
                    newPermission.setName(permission);
                    permissionRepository.save(newPermission);
                });
    }
}