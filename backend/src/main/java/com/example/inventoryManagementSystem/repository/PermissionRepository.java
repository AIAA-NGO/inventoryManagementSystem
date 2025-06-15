package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByName(String name);

    List<Permission> findByNameIn(List<String> names);

    List<Permission> findByNameNotIn(List<String> names);

    List<Permission> findByIdIn(List<Integer> ids);  // Changed from Set to List
}