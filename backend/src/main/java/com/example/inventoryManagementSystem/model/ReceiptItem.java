package com.example.inventoryManagementSystem.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReceiptItem {
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    private BigDecimal preTaxAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;


    private BigDecimal costAmount;
    private BigDecimal profitAmount;



}
