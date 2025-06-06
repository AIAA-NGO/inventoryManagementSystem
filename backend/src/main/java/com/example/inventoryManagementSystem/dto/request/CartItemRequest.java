package com.example.inventoryManagementSystem.dto.request;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemRequest {
    private Long cartId;
    private Long productId;
    private int quantity;
}
