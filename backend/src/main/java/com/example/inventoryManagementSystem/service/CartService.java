package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.CartItemRequest;
import com.example.inventoryManagementSystem.dto.response.CartItemResponse;
import com.example.inventoryManagementSystem.dto.response.CartResponse;

import java.util.List;

public interface CartService {
    CartResponse getCart(String username);
    CartResponse addItemToCart(String username, CartItemRequest request);
    CartResponse addItemsToCart(String username, List<CartItemRequest> requests);
    CartResponse updateItemQuantity(String username, Long productId, int quantity);
    CartResponse removeItemFromCart(String username, Long productId);
    CartResponse applyDiscount(String username, String discountCode);
    void clearCart(String username);
    CartItemResponse updateCartItemQuantity(String username, Long productId, int quantity);
}