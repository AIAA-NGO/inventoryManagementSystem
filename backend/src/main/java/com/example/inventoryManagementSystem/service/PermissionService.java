package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.model.Permission;
import java.util.List;
import java.util.Set;

public interface PermissionService {
    Permission createPermission(String name);

    Permission getPermissionById(Integer id);

    Permission getPermissionByName(String name);

    List<Permission> getAllPermissions();

    List<Permission> getPermissionsByIds(List<Integer> ids);

    List<Permission> getPermissionsByNames(List<String> names);

    void deletePermission(Integer id);
}