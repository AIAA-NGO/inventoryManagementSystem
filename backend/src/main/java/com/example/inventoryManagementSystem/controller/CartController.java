package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.*;
import com.example.inventoryManagementSystem.dto.response.CartItemResponse;
import com.example.inventoryManagementSystem.dto.response.CartResponse;
import com.example.inventoryManagementSystem.dto.response.SaleResponse;
import com.example.inventoryManagementSystem.exception.BusinessException;
import com.example.inventoryManagementSystem.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.inventoryManagementSystem.service.SaleService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final SaleService saleService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping
    public ResponseEntity<CartResponse> addItemsToCart(@Valid @RequestBody List<CartItemRequest> requests) {
        return ResponseEntity.ok(cartService.addItemsToCart(requests));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<CartItemResponse> updateCartItemQuantity(
            @PathVariable Long productId,
            @RequestBody UpdateCartItemRequest request) {
        CartItemResponse response = cartService.updateCartItemQuantity(productId, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(productId));
    }

    @PostMapping("/apply-discount")
    public ResponseEntity<CartResponse> applyDiscount(@Valid @RequestBody ApplyDiscountRequest request) {
        return ResponseEntity.ok(cartService.applyDiscount(request.getDiscountCode()));
    }

    @PostMapping("/checkout")
    public ResponseEntity<SaleResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        CartResponse cart = cartService.getCart();

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot checkout empty cart");
        }

        BigDecimal taxableAmount = cart.getSubtotal().subtract(cart.getDiscountAmount());
        BigDecimal expectedTax = taxableAmount.multiply(BigDecimal.valueOf(0.16))
                .setScale(2, RoundingMode.HALF_UP);

        if (cart.getTaxAmount().compareTo(expectedTax) != 0) {
            throw new BusinessException("Invalid tax calculation in cart. Expected: " +
                    expectedTax + " but got: " + cart.getTaxAmount());
        }

        SaleRequest saleRequest = SaleRequest.builder()
                .customerId(request.getCustomerId())
                .items(cart.getItems().stream()
                        .map(item -> SaleItemRequest.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        SaleResponse response = saleService.createSale(saleRequest);
        cartService.clearCart();

        return ResponseEntity.ok(response);
    }
}