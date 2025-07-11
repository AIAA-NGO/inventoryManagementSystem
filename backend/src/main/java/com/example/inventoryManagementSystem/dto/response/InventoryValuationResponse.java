
package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InventoryValuationResponse {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitCost;
    private BigDecimal totalValue;
    private BigDecimal inventoryTurnover;
}