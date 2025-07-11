package com.example.inventoryManagementSystem.config;

import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.model.Role.ERole;
import com.example.inventoryManagementSystem.model.User;
import com.example.inventoryManagementSystem.repository.RoleRepository;
import com.example.inventoryManagementSystem.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class UserInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initAdminUser() {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            // Create or get ADMIN role
            Role adminRole = roleRepository.findByName(ERole.ADMIN)
                    .orElseGet(() -> {
                        Role role = new Role(ERole.ADMIN);
                        return roleRepository.save(role);
                    });

            // Create admin user
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123")) // Encode password
                    .fullName("System Administrator")
                    .email("admin@sme.com")
                    .active(true)
                    .build();

            // Initialize the roles set
            admin.setRoles(new HashSet<>());

            // Assign ADMIN role
            admin.getRoles().add(adminRole);

            // Save the user
            userRepository.save(admin);

            System.out.println("Created default admin user");
        }
    }
}