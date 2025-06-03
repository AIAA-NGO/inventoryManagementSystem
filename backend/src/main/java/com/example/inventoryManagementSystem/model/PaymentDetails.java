package com.example.inventoryManagementSystem.model;

import lombok.Data;

@Data
public class PaymentDetails {
    private String method;
    private String token;
}
