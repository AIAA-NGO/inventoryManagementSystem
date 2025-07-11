package com.example.inventoryManagementSystem.dto.request;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class UpdateUserRequest {
    @Size(max = 100)
    private String fullName;

    @Size(max = 50)
    @Email
    private String email;

    @Size(min = 6, max = 40)
    private String password;

    @Size(min = 3, max = 20)
    private String username;

    private boolean active;

    private String role;
}