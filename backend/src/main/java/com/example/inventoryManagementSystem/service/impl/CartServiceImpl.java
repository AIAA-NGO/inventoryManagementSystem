package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.CartItemRequest;
import com.example.inventoryManagementSystem.dto.response.CartItemResponse;
import com.example.inventoryManagementSystem.dto.response.CartResponse;
import com.example.inventoryManagementSystem.exception.BusinessException;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Discount;
import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.repository.DiscountRepository;
import com.example.inventoryManagementSystem.repository.ProductRepository;
//import com.example.inventoryManagementSystem.repository.TaxRateRepository;
import com.example.inventoryManagementSystem.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@SessionScope
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;

    private final Map<Long, CartItemResponse> cartItems = new ConcurrentHashMap<>();
    private String appliedDiscountCode;
    private final Long sessionCartId = (long) System.identityHashCode(this);

    @Override
    public CartResponse getCart() {
        List<CartItemResponse> items = new ArrayList<>(cartItems.values());
        BigDecimal subtotal = calculateSubtotal(items);
        BigDecimal discountAmount = calculateDiscount(items);
        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = calculateTax(taxableAmount);
        BigDecimal total = taxableAmount.add(taxAmount);

        return CartResponse.builder()
                .cartId(sessionCartId)
                .items(items)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .total(total)
                .build();
    }

    @Override
    public CartResponse addItemToCart(CartItemRequest request) {
        return addItemsToCart(List.of(request));
    }

    @Override
    public CartResponse addItemsToCart(List<CartItemRequest> requests) {
        Map<Long, Integer> quantityMap = requests.stream()
                .collect(Collectors.toMap(
                        CartItemRequest::getProductId,
                        CartItemRequest::getQuantity,
                        Integer::sum
                ));

        quantityMap.forEach((productId, quantity) -> {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            List<Discount> discounts = discountRepository.findActiveDiscountsForProduct(product);
            BigDecimal itemDiscount = calculateMaxDiscount(BigDecimal.valueOf(product.getPrice()), discounts)
                    .multiply(BigDecimal.valueOf(quantity));

            CartItemResponse existingItem = cartItems.get(productId);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                existingItem.setTotalPrice(existingItem.getUnitPrice()
                        .multiply(BigDecimal.valueOf(existingItem.getQuantity())));
                existingItem.setDiscountAmount(
                        existingItem.getDiscountAmount().add(itemDiscount)
                );
            } else {
                CartItemResponse newItem = CartItemResponse.builder()
                        .productId(productId)
                        .productName(product.getName())
                        .quantity(quantity)
                        .unitPrice(BigDecimal.valueOf(product.getPrice()))
                        .totalPrice(BigDecimal.valueOf(product.getPrice())
                                .multiply(BigDecimal.valueOf(quantity)))
                        .discountAmount(itemDiscount)
                        .build();
                cartItems.put(productId, newItem);
            }
        });

        return getCart();
    }

    private BigDecimal calculateMaxDiscount(BigDecimal itemPrice, List<Discount> discounts) {
        return discounts.stream()
                .map(d -> itemPrice.multiply(BigDecimal.valueOf(d.getPercentage() / 100)))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public CartItemResponse updateCartItemQuantity(Long productId, int newQuantity) {
        if (!cartItems.containsKey(productId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getQuantityInStock() < newQuantity) {
            throw new BusinessException("Not enough stock available");
        }

        List<Discount> discounts = discountRepository.findActiveDiscountsForProduct(product);
        BigDecimal itemDiscount = calculateMaxDiscount(BigDecimal.valueOf(product.getPrice()), discounts)
                .multiply(BigDecimal.valueOf(newQuantity));

        CartItemResponse item = cartItems.get(productId);
        item.setQuantity(newQuantity);
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
        item.setDiscountAmount(itemDiscount);

        return item;
    }

    @Override
    public CartResponse updateItemQuantity(Long productId, int quantity) {
        updateCartItemQuantity(productId, quantity);
        return getCart();
    }

    @Override
    public CartResponse removeItemFromCart(Long productId) {
        cartItems.remove(productId);
        return getCart();
    }

    @Override
    public CartResponse applyDiscount(String discountCode) {
        Discount discount = discountRepository.findByCode(discountCode)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        if (!discount.isActive() ||
                (discount.getValidFrom() != null && discount.getValidFrom().isAfter(LocalDateTime.now())) ||
                (discount.getValidTo() != null && discount.getValidTo().isBefore(LocalDateTime.now()))) {
            throw new BusinessException("Discount is not valid");
        }

        this.appliedDiscountCode = discountCode;
        return getCart();
    }

    @Override
    public void clearCart() {
        cartItems.clear();
        appliedDiscountCode = null;
    }

    private BigDecimal calculateSubtotal(List<CartItemResponse> items) {
        return items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(List<CartItemResponse> items) {
        BigDecimal productDiscounts = items.stream()
                .map(CartItemResponse::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (appliedDiscountCode == null) return productDiscounts;

        Discount discount = discountRepository.findByCode(appliedDiscountCode)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        BigDecimal subtotal = calculateSubtotal(items);
        BigDecimal codeDiscount = subtotal.multiply(BigDecimal.valueOf(discount.getPercentage() / 100));

        return productDiscounts.add(codeDiscount);
    }

    private BigDecimal calculateTax(BigDecimal taxableAmount) {
        BigDecimal taxRate = BigDecimal.valueOf(0.16);
        return taxableAmount.multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
}