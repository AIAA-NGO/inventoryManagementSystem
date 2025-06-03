package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.PasswordUpdateRequest;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface userDetailsService extends UserDetailsService {
    User getCurrentUser();
    User getUserById(Long id) throws ResourceNotFoundException;
    User getUserByUsername(String username) throws ResourceNotFoundException;
    void updatePassword(Long userId, PasswordUpdateRequest request) throws ResourceNotFoundException;

}