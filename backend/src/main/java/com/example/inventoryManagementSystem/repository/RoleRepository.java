package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.model.Role.ERole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
    boolean existsByName(ERole name);
    boolean existsByNameAndIdNot(ERole name, Integer id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO roles (name) SELECT :roleName WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = :roleName)",
            nativeQuery = true)
    void insertRoleIfNotExists(@Param("roleName") String roleName);

    @Query(value = "SELECT COUNT(*) FROM roles WHERE name IN ('ADMIN', 'CASHIER', 'MANAGER', 'RECEIVING_CLERK')",
            nativeQuery = true)
    int countStandardRoles();

;

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findWithPermissionsById(Integer id);
}