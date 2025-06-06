package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.model.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
    boolean existsByName(ERole name);
    boolean existsByNameAndIdNot(ERole name, Integer id);
}