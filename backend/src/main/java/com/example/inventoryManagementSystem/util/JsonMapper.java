package com.example.inventoryManagementSystem.util;

import com.example.inventoryManagementSystem.dto.request.MpesaAuthorization;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static MpesaAuthorization toMpesaAuthorization(String json) {
        try {
            return mapper.readValue(json, MpesaAuthorization.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MpesaAuthorization JSON", e);
        }
    }
}
