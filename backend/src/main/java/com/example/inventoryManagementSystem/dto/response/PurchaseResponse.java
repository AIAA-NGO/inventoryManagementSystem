package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseResponse {

    private Long id;
    private SupplierResponse supplier;
    private String productCategory;
    private LocalDateTime orderDate;
    private LocalDateTime receivedDate;
    private String status;
    private List<PurchaseItemResponse> items;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime cancellationDate;
}