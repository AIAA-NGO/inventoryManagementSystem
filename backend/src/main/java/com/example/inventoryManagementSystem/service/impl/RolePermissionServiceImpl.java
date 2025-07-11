package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.RolePermissionRequest;
import com.example.inventoryManagementSystem.dto.response.PermissionDto;
import com.example.inventoryManagementSystem.dto.response.RoleWithPermissionsResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.repository.PermissionRepository;
import com.example.inventoryManagementSystem.repository.RoleRepository;
import com.example.inventoryManagementSystem.security.AppPermissions;
import com.example.inventoryManagementSystem.service.RolePermissionService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void initializeRolesAndPermissions() {
        log.info("Initializing roles and permissions...");

        // Ensure all permissions exist in database
        Set<String> allPermissionNames = AppPermissions.getAllPermissions();
        allPermissionNames.forEach(name -> {
            if (!permissionRepository.existsByName(name)) {
                Permission permission = new Permission();
                permission.setName(name);
                permission.setDescription("System permission: " + name);
                permissionRepository.save(permission);
                log.info("Created permission: {}", name);
            }
        });

        // Initialize roles with their permissions
        initializeRoleWithPermissions(Role.ERole.ADMIN, allPermissionNames, "ADMIN role initialized");
        initializeRoleWithPermissions(Role.ERole.CASHIER, AppPermissions.getCashierPermissions(), "CASHIER role initialized");
        initializeRoleWithPermissions(Role.ERole.MANAGER, AppPermissions.getManagerPermissions(), "MANAGER role initialized");
        initializeRoleWithPermissions(Role.ERole.RECEIVING_CLERK, AppPermissions.getReceivingClerkPermissions(), "RECEIVING_CLERK role initialized");
    }

    private void initializeRoleWithPermissions(Role.ERole roleName, Set<String> permissionNames, String logMessage) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        Set<Permission> requiredPermissions = permissionNames.stream()
                .map(name -> permissionRepository.findByName(name)
                        .orElseThrow(() -> new IllegalStateException("Permission not found: " + name)))
                .collect(Collectors.toSet());

        // Get current permissions with fresh database state
        Set<Permission> currentPermissions = new HashSet<>(
                roleRepository.findWithPermissionsById(role.getId())
                        .orElse(role)
                        .getPermissions()
        );

        if (!currentPermissions.containsAll(requiredPermissions)) {
            // Clear and set new permissions
            role.getPermissions().clear();
            role.getPermissions().addAll(requiredPermissions);
            roleRepository.saveAndFlush(role);
            log.info("{} - Now has {} permissions", logMessage, role.getPermissions().size());
        }
    }

    @Override
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToPermissionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleWithPermissionsResponse assignPermissionsToRole(RolePermissionRequest request)
            throws ResourceNotFoundException {
        Role role = roleRepository.findWithPermissionsById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        validateNotAdminRole(role);

        // Get fresh permission entities and convert to Set
        Set<Permission> permissionsToAdd = new HashSet<>(
                permissionRepository.findAllById(request.getPermissionIds())
        );

        // Clear existing permissions (managing both sides of relationship)
        role.getPermissions().forEach(p -> p.getRoles().remove(role));
        role.getPermissions().clear();

        // Add new permissions (managing both sides)
        permissionsToAdd.forEach(p -> {
            role.getPermissions().add(p);
            p.getRoles().add(role);
        });

        // Save and refresh
        Role savedRole = roleRepository.saveAndFlush(role);
        entityManager.refresh(savedRole);

        log.debug("Assigned {} permissions to role {}", permissionsToAdd.size(), role.getName());
        return mapToResponse(savedRole);
    }

    @Override
    @Transactional
    public RoleWithPermissionsResponse removePermissionsFromRole(RolePermissionRequest request)
            throws ResourceNotFoundException {
        Role role = roleRepository.findWithPermissionsById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        validateNotAdminRole(role);

        Set<Permission> permissionsToRemove = getValidPermissions(request.getPermissionIds());

        Set<Permission> updatedPermissions = new HashSet<>(role.getPermissions());
        updatedPermissions.removeAll(permissionsToRemove);

        role.setPermissions(updatedPermissions);
        Role savedRole = roleRepository.saveAndFlush(role);

        log.debug("Removed {} permissions from role {}", permissionsToRemove.size(), role.getName());
        return mapToResponse(savedRole);
    }


    @Override
    @Transactional
    public RoleWithPermissionsResponse updateRolePermissions(Integer roleId, RolePermissionRequest request)
            throws ResourceNotFoundException {
        log.info("Updating permissions for role ID: {}", roleId);

        // Fetch the role and permissions in the same transaction
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        validateNotAdminRole(role);

        // Get all requested permissions
        Set<Permission> newPermissions = new HashSet<>(
                permissionRepository.findAllById(request.getPermissionIds())
        );

        log.info("Found {} permissions to assign to role {}", newPermissions.size(), role.getName());

        // Clear existing permissions (managing both sides of relationship)
        role.clearPermissions();

        // Add new permissions (managing both sides)
        newPermissions.forEach(role::addPermission);

        // Save and flush
        Role savedRole = roleRepository.saveAndFlush(role);

        // Ensure changes are persisted
        entityManager.flush();
        entityManager.refresh(savedRole);

        log.info("Successfully updated permissions for role {}", role.getName());
        return mapToResponse(savedRole);
    }

    @Override
    public RoleWithPermissionsResponse getRoleWithPermissions(Integer roleId) throws ResourceNotFoundException {
        Role role = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return mapToResponse(role);
    }

    private Set<Permission> getValidPermissions(Set<Integer> permissionIds) throws ResourceNotFoundException {
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));

        if (permissions.size() != permissionIds.size()) {
            Set<Integer> foundIds = permissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());

            Set<Integer> missingIds = permissionIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());

            throw new ResourceNotFoundException("Permissions not found with IDs: " + missingIds);
        }

        return permissions;
    }

    private void validateNotAdminRole(Role role) {
        if (role.getName() == Role.ERole.ADMIN) {
            throw new IllegalArgumentException("Cannot modify permissions for ADMIN role");
        }
    }

    private PermissionDto mapToPermissionDto(Permission permission) {
        return PermissionDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }

    private RoleWithPermissionsResponse mapToResponse(Role role) {
        return RoleWithPermissionsResponse.builder()
                .id(role.getId())
                .name(role.getName().name())
                .permissions(role.getPermissions().stream()
                        .map(this::mapToPermissionDto)
                        .collect(Collectors.toSet()))
                .build();
    }
}