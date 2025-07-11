package com.example.inventoryManagementSystem.config;

import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.repository.PermissionRepository;
import com.example.inventoryManagementSystem.security.AppPermissions;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionInitializer {
    private final PermissionRepository permissionRepository;

    @PostConstruct
    @Transactional
    public void init() {
        Set<String> allPermissions = AppPermissions.getAllPermissions();

        for (String permissionName : allPermissions) {
            if (!permissionRepository.existsByName(permissionName)) {
                Permission permission = new Permission();
                permission.setName(permissionName);
                permission.setDescription("System permission: " + permissionName);
                permissionRepository.save(permission);
            }
        }
    }
}