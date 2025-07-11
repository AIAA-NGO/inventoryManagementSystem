package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordUpdateRequest {
    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;


    public PasswordUpdateRequest() {
    }

    public PasswordUpdateRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }


    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }


    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}