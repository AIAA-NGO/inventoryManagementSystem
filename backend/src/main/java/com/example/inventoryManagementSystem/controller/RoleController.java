package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.RolePermissionRequest;
import com.example.inventoryManagementSystem.dto.request.RoleRequest;
import com.example.inventoryManagementSystem.dto.response.RoleResponse;
import com.example.inventoryManagementSystem.exception.DuplicateResourceException;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody RoleRequest request) {
        try {
            RoleResponse response = roleService.createRole(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating role: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        try {
            List<RoleResponse> roles = roleService.getAllRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching roles: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Integer id) {
        try {
            RoleResponse response = roleService.getRoleById(id);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching role: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(
            @PathVariable Integer id,
            @RequestBody RoleRequest request) {
        try {
            RoleResponse response = roleService.updateRole(id, request);
            return ResponseEntity.ok(response);
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating role: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Integer id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting role: " + e.getMessage());
        }
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<?> getRolePermissions(@PathVariable Integer roleId) {
        try {
            Set<String> permissions = roleService.getRolePermissions(roleId);
            return ResponseEntity.ok(permissions);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching role permissions: " + e.getMessage());
        }
    }
}