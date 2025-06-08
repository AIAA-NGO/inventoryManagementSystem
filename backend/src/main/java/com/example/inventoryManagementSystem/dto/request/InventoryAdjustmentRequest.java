package com.example.inventoryManagementSystem.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class InventoryAdjustmentRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Adjustment amount is required")
    private Integer adjustmentAmount;

    private String reason;

    private Boolean createSupplierOrder;
    private Integer orderQuantity;
}