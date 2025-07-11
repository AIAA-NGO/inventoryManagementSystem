package com.example.inventoryManagementSystem.controller.auth;

import com.example.inventoryManagementSystem.dto.request.LoginRequest;
import com.example.inventoryManagementSystem.dto.request.SignupRequest;
import com.example.inventoryManagementSystem.dto.response.JwtResponse;
import com.example.inventoryManagementSystem.dto.response.MessageResponse;
import com.example.inventoryManagementSystem.dto.response.UserResponse;
import com.example.inventoryManagementSystem.model.Role;
import com.example.inventoryManagementSystem.model.User;
import com.example.inventoryManagementSystem.repository.RoleRepository;
import com.example.inventoryManagementSystem.repository.UserRepository;
import com.example.inventoryManagementSystem.service.impl.UserDetailsImpl;
import com.example.inventoryManagementSystem.service.impl.UserDetailsServiceImpl;
import com.example.inventoryManagementSystem.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository,
                          UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        try {
            String token = jwtUtils.getJwtFromHeader(request);

            if (token != null) {
                jwtUtils.invalidateToken(token);
                SecurityContextHolder.clearContext();
                return ResponseEntity.ok(new MessageResponse("Logout successful"));
            }
            return ResponseEntity.badRequest().body(new MessageResponse("No token provided"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error during logout: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setFullName(signUpRequest.getFullName());
        user.setActive(true);

        Set<Role> roles = new HashSet<>();
        if (signUpRequest.getRole() == null || signUpRequest.getRole().isEmpty()) {
            Role userRole = roleRepository.findByName(Role.ERole.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found."));
            roles.add(userRole);
        } else {
            Role assignedRole = roleRepository.findByName(Role.ERole.valueOf(signUpRequest.getRole()))
                    .orElseThrow(() -> new RuntimeException("Error: Role not found: " + signUpRequest.getRole()));
            roles.add(assignedRole);
        }
        user.setRoles(roles);

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String token = jwtUtils.getJwtFromHeader(request);
        if (token != null && jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUserNameFromJwtToken(token);
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
            String newToken = jwtUtils.generateJwtTokenFromUsername(username);

            return ResponseEntity.ok(new JwtResponse(
                    newToken,
                    userDetails.getId(),
                    username,
                    userDetails.getEmail(),
                    userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList())
            ));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid refresh token"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList())
        ));
    }
}