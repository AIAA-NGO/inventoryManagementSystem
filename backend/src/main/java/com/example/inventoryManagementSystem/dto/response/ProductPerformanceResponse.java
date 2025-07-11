
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
    private BigDecimal totalCost;
    private BigDecimal grossProfit;
    private BigDecimal markupPercentage;

    private String categoryName;

    // Add getter and setter
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}