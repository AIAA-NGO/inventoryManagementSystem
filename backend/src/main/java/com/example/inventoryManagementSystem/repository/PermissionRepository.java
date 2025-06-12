package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    @Query("SELECT p FROM Permission p WHERE p.name IN :names")
    Set<Permission> findByNames(@Param("names") Set<String> names);

    @Query("SELECT p FROM Permission p WHERE p.id IN :ids")
    Set<Permission> findByIds(@Param("ids") Set<Integer> ids);
}