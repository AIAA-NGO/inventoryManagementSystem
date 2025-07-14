package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.*;
import com.example.inventoryManagementSystem.dto.response.CartItemResponse;
import com.example.inventoryManagementSystem.dto.response.CartResponse;
import com.example.inventoryManagementSystem.dto.response.SaleResponse;
import com.example.inventoryManagementSystem.exception.BusinessException;
import com.example.inventoryManagementSystem.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.inventoryManagementSystem.service.SaleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final SaleService saleService;

    public CartController(CartService cartService, SaleService saleService) {
        this.cartService = cartService;
        this.saleService = saleService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(cartService.getCart(username));
    }

    @PostMapping
    public ResponseEntity<CartResponse> addItemsToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody List<CartItemRequest> requests) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(cartService.addItemsToCart(username, requests));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<CartItemResponse> updateCartItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestBody UpdateCartItemRequest request) {
        String username = userDetails.getUsername();
        CartItemResponse response = cartService.updateCartItemQuantity(username, productId, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(cartService.removeItemFromCart(username, productId));
    }

    @PostMapping("/apply-discount")
    public ResponseEntity<CartResponse> applyDiscount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ApplyDiscountRequest request) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(cartService.applyDiscount(username, request.getDiscountCode()));
    }

    @PostMapping("/checkout")
    public ResponseEntity<SaleResponse> checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) {
        String username = userDetails.getUsername();
        CartResponse cart = cartService.getCart(username);

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot checkout empty cart");
        }

        List<SaleItemRequest> saleItems = cart.getItems().stream()
                .map(item -> {
                    SaleItemRequest saleItem = new SaleItemRequest();
                    saleItem.setProductId(item.getProductId());
                    saleItem.setQuantity(item.getQuantity());
                    saleItem.setUnitPrice(item.getUnitPrice());
                    saleItem.setDiscountAmount(item.getDiscountAmount());
                    return saleItem;
                })
                .collect(Collectors.toList());

        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCustomerId(request.getCustomerId());
        saleRequest.setItems(saleItems);
        saleRequest.setSubtotal(cart.getSubtotal());
        saleRequest.setDiscountAmount(cart.getDiscountAmount());
        saleRequest.setTotal(cart.getTotal());

        SaleResponse response = saleService.createSale(saleRequest);
        cartService.clearCart(username);

        return ResponseEntity.ok(response);
    }
}