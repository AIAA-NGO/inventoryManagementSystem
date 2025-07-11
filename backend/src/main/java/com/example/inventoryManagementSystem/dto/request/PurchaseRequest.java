package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseRequest {
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotEmpty(message = "At least one purchase item is required")
    @Valid
    private List<PurchaseItemRequest> items;
}