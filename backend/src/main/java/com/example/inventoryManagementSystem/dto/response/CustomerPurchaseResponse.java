package com.example.inventoryManagementSystem.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CustomerPurchaseResponse {
    private Long customerId;
    private String customerName;
    private List<PurchaseHistoryItem> purchases;

    @Data
    public static class PurchaseHistoryItem {
        private Long saleId;
        private String date;
        private Double totalAmount;
        private List<String> products;
    }
}