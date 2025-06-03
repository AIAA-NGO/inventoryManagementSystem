package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime orderDate;
    private LocalDateTime receivedDate;
    private String status;
    private Double totalAmount;
    private List<PurchaseItemResponse> items;
    private LocalDateTime createdAt;
}

