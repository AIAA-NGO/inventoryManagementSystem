package com.example.inventoryManagementSystem.dto.response;

import com.example.inventoryManagementSystem.model.ReceiptItem;
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
    private List<ReceiptItem> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private BigDecimal preTaxAmount;
    private BigDecimal totalProfit;
}