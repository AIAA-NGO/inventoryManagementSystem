package com.example.inventoryManagementSystem.model;

public class ProductPerformance {

    private Long productId;
    private String productName;
    private String categoryName;
    private Integer unitsSold = 0;
    private Double revenue = 0.0;
    private Double cost = 0.0;

    // Calculated fields
    public Double getProfit() {
        return revenue - cost;
    }

    public Double getProfitMargin() {
        return revenue > 0 ? (getProfit() / revenue) * 100 : 0;
    }

}
