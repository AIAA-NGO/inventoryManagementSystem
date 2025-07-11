package com.example.inventoryManagementSystem.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MpesaAuthorization {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private String expiresIn;
}
