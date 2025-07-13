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
import com.example.inventoryManagementSystem.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;


    // Store carts in memory with username as key
    private final Map<String, UserCart> userCarts = new ConcurrentHashMap<>();

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.16);

    private static class UserCart {
        Map<Long, CartItemResponse> items = new ConcurrentHashMap<>();
        String appliedDiscountCode;
    }

    private UserCart getUserCart(String username) {
        return userCarts.computeIfAbsent(username, k -> new UserCart());
    }

    //private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.16);

    @Override
    public CartResponse getCart(String username) {
        UserCart userCart = getUserCart(username);
        List<CartItemResponse> items = new ArrayList<>(userCart.items.values());

        // Subtotal is tax-inclusive (what customers see as prices)
        BigDecimal subtotal = calculateSubtotal(items);

        // Calculate the tax amount (16% of the pre-tax value)
        BigDecimal preTaxAmount = subtotal.divide(BigDecimal.ONE.add(TAX_RATE), 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.subtract(preTaxAmount);

        // Apply discounts to pre-tax amount
        BigDecimal discountAmount = calculateDiscount(userCart, items);

        // Total remains the same as subtotal (tax-inclusive)
        BigDecimal total = subtotal;

        return CartResponse.builder()
                .items(items)
                .subtotal(subtotal)          // shows as "Subtotal (tax inclusive)"
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)         // shows the tax component
                .total(total)                 // same as subtotal
                .preTaxAmount(preTaxAmount)   // for internal calculations
                .build();
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(String username, CartItemRequest request) {
        return addItemsToCart(username, Collections.singletonList(request));
    }

    @Override
    @Transactional
    public CartResponse addItemsToCart(String username, List<CartItemRequest> requests) {
        UserCart userCart = getUserCart(username);
        validateQuantitiesAgainstStock(username, requests);

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

            CartItemResponse existingItem = userCart.items.get(productId);

            if (existingItem != null) {
                int newQuantity = existingItem.getQuantity() + quantity;
                existingItem.setQuantity(newQuantity);
                existingItem.setTotalPrice(BigDecimal.valueOf(product.getPrice())
                        .multiply(BigDecimal.valueOf(newQuantity)));
                existingItem.setDiscountAmount(
                        existingItem.getDiscountAmount().add(itemDiscount)
                );
            } else {
                CartItemResponse newItem = CartItemResponse.builder()
                        .productId(productId)
                        .productName(product.getName())
                        .quantity(quantity)
                        .unitPrice(BigDecimal.valueOf(product.getPrice()))  // tax-exclusive price
                        .totalPrice(BigDecimal.valueOf(product.getPrice())
                                .multiply(BigDecimal.valueOf(quantity)))  // tax-exclusive total
                        .discountAmount(itemDiscount)
                        .build();
                userCart.items.put(productId, newItem);
            }
        });

        return getCart(username);
    }

    private void validateQuantitiesAgainstStock(String username, List<CartItemRequest> requests) {
        UserCart userCart = getUserCart(username);
        Map<Long, Integer> requestedQuantities = requests.stream()
                .collect(Collectors.groupingBy(
                        CartItemRequest::getProductId,
                        Collectors.summingInt(CartItemRequest::getQuantity)
                ));

        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Long productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            int currentQuantityInCart = userCart.items.containsKey(productId) ?
                    userCart.items.get(productId).getQuantity() : 0;
            int totalQuantityAfterAddition = currentQuantityInCart + requestedQuantity;

            if (totalQuantityAfterAddition > product.getQuantityInStock()) {
                throw new BusinessException(String.format(
                        "Cannot add %d items of product '%s' to cart. Only %d items available in stock. You already have %d in your cart.",
                        requestedQuantity, product.getName(), product.getQuantityInStock(), currentQuantityInCart
                ));
            }
        }
    }

    private BigDecimal calculateMaxDiscount(BigDecimal itemPrice, List<Discount> discounts) {
        return discounts.stream()
                .map(d -> itemPrice.multiply(BigDecimal.valueOf(d.getPercentage() / 100)))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public CartItemResponse updateCartItemQuantity(String username, Long productId, int newQuantity) {
        UserCart userCart = getUserCart(username);
        if (!userCart.items.containsKey(productId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getQuantityInStock() < newQuantity) {
            throw new BusinessException(String.format(
                    "Not enough stock available for product '%s'. Only %d items available.",
                    product.getName(), product.getQuantityInStock()
            ));
        }

        List<Discount> discounts = discountRepository.findActiveDiscountsForProduct(product);
        BigDecimal itemDiscount = calculateMaxDiscount(BigDecimal.valueOf(product.getPrice()), discounts)
                .multiply(BigDecimal.valueOf(newQuantity));

        CartItemResponse item = userCart.items.get(productId);
        item.setQuantity(newQuantity);
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
        item.setDiscountAmount(itemDiscount);

        return item;
    }

    @Override
    public CartResponse updateItemQuantity(String username, Long productId, int quantity) {
        updateCartItemQuantity(username, productId, quantity);
        return getCart(username);
    }

    @Override
    public CartResponse removeItemFromCart(String username, Long productId) {
        UserCart userCart = getUserCart(username);
        userCart.items.remove(productId);
        return getCart(username);
    }

    @Override
    public CartResponse applyDiscount(String username, String discountCode) {
        UserCart userCart = getUserCart(username);
        Discount discount = discountRepository.findByCode(discountCode)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        if (!discount.isActive() ||
                (discount.getValidFrom() != null && discount.getValidFrom().isAfter(LocalDateTime.now())) ||
                (discount.getValidTo() != null && discount.getValidTo().isBefore(LocalDateTime.now()))) {
            throw new BusinessException("Discount is not valid");
        }

        userCart.appliedDiscountCode = discountCode;
        return getCart(username);
    }

    @Override
    public void clearCart(String username) {
        UserCart userCart = getUserCart(username);
        userCart.items.clear();
        userCart.appliedDiscountCode = null;
    }

    private BigDecimal calculateSubtotal(List<CartItemResponse> items) {
        return items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(UserCart userCart, List<CartItemResponse> items) {
        BigDecimal productDiscounts = items.stream()
                .map(CartItemResponse::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (userCart.appliedDiscountCode == null) return productDiscounts;

        Discount discount = discountRepository.findByCode(userCart.appliedDiscountCode)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        BigDecimal subtotal = calculateSubtotal(items);
        BigDecimal codeDiscount = subtotal.multiply(BigDecimal.valueOf(discount.getPercentage() / 100));

        return productDiscounts.add(codeDiscount);
    }

    private BigDecimal calculateTax(BigDecimal taxableAmount) {
        return taxableAmount.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}