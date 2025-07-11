package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.RolePermissionRequest;
import com.example.inventoryManagementSystem.dto.response.PermissionDto;
import com.example.inventoryManagementSystem.dto.response.RoleWithPermissionsResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        try {
            log.info("Fetching all permissions");
            List<PermissionDto> permissions = rolePermissionService.getAllPermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            log.error("Error fetching permissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignPermissionsToRole(@RequestBody RolePermissionRequest request) {
        try {
            log.info("Assigning permissions to role ID: {}", request.getRoleId());
            RoleWithPermissionsResponse response = rolePermissionService.assignPermissionsToRole(request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error assigning permissions: " + e.getMessage());
        }
    }

    @GetMapping("/{roleId}/details")
    public ResponseEntity<?> getRoleWithPermissionsDetails(@PathVariable Integer roleId) {
        try {
            log.info("Fetching permissions for role ID: {}", roleId);
            RoleWithPermissionsResponse response = rolePermissionService.getRoleWithPermissions(roleId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Role not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching role permissions: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateRolePermissions(@RequestBody RolePermissionRequest request) {
        try {
            log.info("Updating permissions for role ID: {}", request.getRoleId());
            RoleWithPermissionsResponse response = rolePermissionService.updateRolePermissions(
                    request.getRoleId(),
                    request
            );
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating permissions: " + e.getMessage());
        }
    }
}