package com.example.inventoryManagementSystem.dto.request;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
public class SaleRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotEmpty(message = "At least one item is required")
    private List<SaleItemRequest> items;
}