package com.example.inventoryManagementSystem.config;

import com.example.inventoryManagementSystem.model.Role.ERole;
import com.example.inventoryManagementSystem.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleInitializer {

    @Bean
    public CommandLineRunner initializeRoles(RoleRepository roleRepository) {
        return args -> {
            // Check if standard roles already exist
            if (roleRepository.countStandardRoles() < 3) {
                // Insert roles if they don't exist
                roleRepository.insertRoleIfNotExists(ERole.ADMIN.name());
                roleRepository.insertRoleIfNotExists(ERole.CASHIER.name());
                roleRepository.insertRoleIfNotExists(ERole.MANAGER.name());
            }
        };
    }
}