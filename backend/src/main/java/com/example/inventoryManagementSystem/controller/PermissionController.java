package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.model.Permission;
import com.example.inventoryManagementSystem.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestParam String name) {
        Permission permission = permissionService.createPermission(name);
        return ResponseEntity.ok(permission);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Integer id) {
        Permission permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Permission> getPermissionByName(@PathVariable String name) {
        Permission permission = permissionService.getPermissionByName(name);
        return ResponseEntity.ok(permission);
    }

    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }


    @GetMapping("/by-ids")
    public ResponseEntity<List<Permission>> getPermissionsByIds(@RequestParam List<Integer> ids) {
        List<Permission> permissions = permissionService.getPermissionsByIds(ids);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/by-names")
    public ResponseEntity<List<Permission>> getPermissionsByNames(@RequestParam List<String> names) {
        List<Permission> permissions = permissionService.getPermissionsByNames(names);
        return ResponseEntity.ok(permissions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Integer id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}