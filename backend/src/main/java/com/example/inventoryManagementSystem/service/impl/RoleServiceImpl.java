package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.RolePermissionRequest;
import com.example.inventoryManagementSystem.dto.request.RoleRequest;
import com.example.inventoryManagementSystem.dto.response.RoleResponse;
import com.example.inventoryManagementSystem.exception.DuplicateResourceException;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.model.Role.ERole;
import com.example.inventoryManagementSystem.repository.PermissionRepository;
import com.example.inventoryManagementSystem.repository.RoleRepository;
import com.example.inventoryManagementSystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) throws DuplicateResourceException {
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role " + request.getName() + " already exists");
        }

        Role role = new Role(request.getName());
        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Integer id) throws ResourceNotFoundException {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return mapToRoleResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Integer id, RoleRequest request)
            throws DuplicateResourceException, ResourceNotFoundException {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (!role.getName().equals(request.getName()) &&
                roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role " + request.getName() + " already exists");
        }

        role.setName(request.getName());
        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Integer id) throws ResourceNotFoundException {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public RoleResponse assignPermissions(Integer roleId, RolePermissionRequest request)
            throws ResourceNotFoundException {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
        if (permissions.size() != request.getPermissionIds().size()) {
            throw new ResourceNotFoundException("One or more permissions not found");
        }

        role.getPermissions().addAll(permissions);
        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Override
    @Transactional
    public RoleResponse removePermissions(Integer roleId, RolePermissionRequest request)
            throws ResourceNotFoundException {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Set<Permission> permissionsToRemove = role.getPermissions().stream()
                .filter(p -> request.getPermissionIds().contains(p.getId()))
                .collect(Collectors.toSet());

        role.getPermissions().removeAll(permissionsToRemove);
        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getRolePermissions(Integer roleId) throws ResourceNotFoundException {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        return role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}