package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SaleRequest {
    @NotNull
    private Long customerId;

    @NotEmpty
    private List<CartItemRequest> items;

    private String discountCode;

    @NotBlank
    private String paymentMethod;

    private BigDecimal discountAmount;
    private LocalDateTime saleDate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal total;
}
