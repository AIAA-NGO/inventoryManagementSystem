package com.example.inventoryManagementSystem.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpesaAuthorization {
    private Object access_token;
}
