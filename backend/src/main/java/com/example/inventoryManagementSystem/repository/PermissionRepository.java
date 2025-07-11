package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
    List<Permission> findByIdIn(List<Integer> ids);
    List<Permission> findByNameIn(List<String> names);

    // Add this new method
    @Query("SELECT p FROM Permission p WHERE p.name IN :names")
    List<Permission> findByNames(@Param("names") List<String> names);
}