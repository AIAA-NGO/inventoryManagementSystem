
package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.CartItemRequest;
import com.example.inventoryManagementSystem.dto.response.CartItemResponse;
import com.example.inventoryManagementSystem.dto.response.CartResponse;

import java.util.List;

public interface CartService {
    CartResponse getCart();
    CartResponse addItemToCart(CartItemRequest request);
    CartResponse updateItemQuantity(Long productId, int quantity);
    CartResponse removeItemFromCart(Long productId);
    CartResponse applyDiscount(String discountCode);
    void clearCart();
    CartItemResponse updateCartItemQuantity(Long cartItemId, int quantity);
    CartResponse addItemsToCart(List<CartItemRequest> requests);
    
}