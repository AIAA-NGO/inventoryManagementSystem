
package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductPerformanceResponse {
    private Long productId;
    private String productName;
    private int unitsSold;
    private BigDecimal totalRevenue;
    private BigDecimal profitMargin;
}