package com.example.inventoryManagementSystem.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseRequest {
    @NotNull
    private Long supplierId;

    @Valid
    private List<PurchaseItemRequest> items;
}