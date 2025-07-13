package com.example.inventoryManagementSystem.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private Long id;
    private LocalDateTime saleDate;
    private String customerName;
    private List<SaleItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String status;
    private BigDecimal preTaxAmount;
    private BigDecimal profit;
    private String paymentMethod;
    private String transactionReference;
}