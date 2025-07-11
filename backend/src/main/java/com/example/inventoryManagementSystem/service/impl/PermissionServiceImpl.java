package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.exception.ResourceAlreadyExistsException;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.repository.PermissionRepository;
import com.example.inventoryManagementSystem.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public Permission createPermission(String name) {
        if (permissionRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Permission with name '" + name + "' already exists");
        }

        Permission permission = new Permission();
        permission.setName(name);
        return permissionRepository.save(permission);
    }

    @Override
    public Permission getPermissionById(Integer id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
    }

    @Override
    public Permission getPermissionByName(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with name: " + name));
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public List<Permission> getPermissionsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return permissionRepository.findByIdIn(ids);
    }

    @Override
    public List<Permission> getPermissionsByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }
        return permissionRepository.findByNameIn(names);
    }

    @Override
    public void deletePermission(Integer id) {
        Permission permission = getPermissionById(id);
        // Remove permission from all roles before deleting
        permission.getRoles().forEach(role -> role.getPermissions().remove(permission));
        permissionRepository.delete(permission);
    }
}