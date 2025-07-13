package com.example.inventoryManagementSystem.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String barcode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal preTaxAmount;
    private BigDecimal taxAmount;
    private BigDecimal costAmount;
    private BigDecimal profitAmount;
    private BigDecimal marginPercentage;
    private String imageUrl;
}