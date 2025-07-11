package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutRequest {
    @NotNull
    private Long customerId;

    @NotBlank
    private String paymentMethod;
}