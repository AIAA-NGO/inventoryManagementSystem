package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierResponse {
    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private long productCount;
    private long purchaseCount;
}
