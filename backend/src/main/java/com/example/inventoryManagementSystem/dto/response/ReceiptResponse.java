package com.example.inventoryManagementSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReceiptResponse {
    private String receiptNumber;
    private LocalDateTime date;
    private String customerName;
    private List<ReceiptItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String paymentMethod;
    private String appliedDiscountCode;
}