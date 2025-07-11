package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.*;
import com.example.inventoryManagementSystem.dto.response.UserResponse;
import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.model.Role.ERole;
import com.example.inventoryManagementSystem.model.User;
import com.example.inventoryManagementSystem.repository.RoleRepository;
import com.example.inventoryManagementSystem.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setFullName(registrationRequest.getFullName());
        user.setActive(true);

        Set<Role> roles = new HashSet<>();
        String roleName = registrationRequest.getRole() != null ?
                registrationRequest.getRole().toUpperCase() :
                "CASHIER";

        try {
            ERole roleEnum = ERole.valueOf(roleName);
            Role userRole = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Error: Role '" + roleName + "' not found."));
            roles.add(userRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: Invalid role specified");
        }

        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(convertToResponse(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(convertToResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest updateRequest) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        user.setActive(updateRequest.isActive());

        if (updateRequest.getUsername() != null &&
                !updateRequest.getUsername().isEmpty() &&
                !updateRequest.getUsername().equals(user.getUsername())) {

            if (userRepository.existsByUsername(updateRequest.getUsername())) {
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }
            user.setUsername(updateRequest.getUsername());
        }

        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        if (updateRequest.getRole() != null && !updateRequest.getRole().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            try {
                ERole roleEnum = ERole.valueOf(updateRequest.getRole().toUpperCase());
                Role userRole = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                roles.add(userRole);
                user.setRoles(roles);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Error: Invalid role specified");
            }
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest changeRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(changeRequest.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(changeRequest.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList())
        );
    }
}