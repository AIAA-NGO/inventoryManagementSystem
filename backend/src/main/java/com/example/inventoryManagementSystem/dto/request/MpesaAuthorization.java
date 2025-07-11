package com.example.inventoryManagementSystem.dto.request;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class MpesaAuthorization {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private String expiresIn;
}
