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

    private final Map<String, UserCart> userCarts = new ConcurrentHashMap<>();
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.16);

    private static class UserCart {
        Map<Long, CartItemResponse> items = new ConcurrentHashMap<>();
        String appliedDiscountCode;
        BigDecimal cartDiscountPercentage = BigDecimal.ZERO;
        BigDecimal cartDiscountAmount = BigDecimal.ZERO;
    }

    private UserCart getUserCart(String username) {
        return userCarts.computeIfAbsent(username, k -> new UserCart());
    }

    @Override
    public CartResponse getCart(String username) {
        UserCart userCart = getUserCart(username);
        List<CartItemResponse> items = new ArrayList<>(userCart.items.values());

        BigDecimal preTaxSubtotal = calculatePreTaxSubtotal(items);
        BigDecimal productDiscounts = calculateProductDiscounts(items);
        BigDecimal amountAfterProductDiscounts = preTaxSubtotal.subtract(productDiscounts);
        BigDecimal cartDiscountAmount = calculateCartDiscount(amountAfterProductDiscounts, userCart.cartDiscountPercentage);
        BigDecimal taxableAmount = amountAfterProductDiscounts.subtract(cartDiscountAmount);
        BigDecimal taxAmount = calculateTax(taxableAmount);
        BigDecimal total = taxableAmount.add(taxAmount);

        return CartResponse.builder()
                .items(items)
                .preTaxSubtotal(preTaxSubtotal)
                .productDiscountAmount(productDiscounts)
                .cartDiscountAmount(cartDiscountAmount)
                .totalDiscountAmount(productDiscounts.add(cartDiscountAmount))
                .taxableAmount(taxableAmount)
                .taxAmount(taxAmount)
                .total(total)
                .appliedDiscountCode(userCart.appliedDiscountCode)
                .build();
    }

    @Override
    public CartResponse addItemToCart(String username, CartItemRequest request) {
        return addItemsToCart(username, List.of(request));
    }

    @Override
    @Transactional
    public CartResponse addItemsToCart(String username, List<CartItemRequest> requests) {
        UserCart userCart = getUserCart(username);
        validateQuantitiesAgainstStock(username, requests);

        for (CartItemRequest request : requests) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            BigDecimal maxDiscountPercentage = getMaxDiscountPercentageForProduct(product);
            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());

            CartItemResponse existingItem = userCart.items.get(product.getId());
            if (existingItem != null) {
                updateExistingCartItem(existingItem, request.getQuantity(), maxDiscountPercentage);
            } else {
                addNewCartItem(userCart, product, request.getQuantity(), maxDiscountPercentage, unitPrice);
            }
        }

        return getCart(username);
    }

    @Override
    public CartResponse updateItemQuantity(String username, Long productId, int quantity) {
        updateCartItemQuantity(username, productId, quantity);
        return getCart(username);
    }

    @Override
    public CartItemResponse updateCartItemQuantity(String username, Long productId, int newQuantity) {
        UserCart userCart = getUserCart(username);
        CartItemResponse item = userCart.items.get(productId);
        if (item == null) {
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

        BigDecimal maxDiscountPercentage = getMaxDiscountPercentageForProduct(product);
        updateExistingCartItem(item, newQuantity - item.getQuantity(), maxDiscountPercentage);

        return item;
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
        if (discountCode == null || discountCode.isEmpty()) {
            return removeDiscount(username);
        }

        Discount discount = discountRepository.findByCode(discountCode)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        validateDiscount(discount);

        userCart.appliedDiscountCode = discountCode;
        userCart.cartDiscountPercentage = BigDecimal.valueOf(discount.getPercentage());
        return getCart(username);
    }

    @Override
    public CartResponse removeDiscount(String username) {
        UserCart userCart = getUserCart(username);
        userCart.appliedDiscountCode = null;
        userCart.cartDiscountPercentage = BigDecimal.ZERO;
        userCart.cartDiscountAmount = BigDecimal.ZERO;
        return getCart(username);
    }

    @Override
    public void clearCart(String username) {
        UserCart userCart = getUserCart(username);
        userCart.items.clear();
        userCart.appliedDiscountCode = null;
        userCart.cartDiscountPercentage = BigDecimal.ZERO;
        userCart.cartDiscountAmount = BigDecimal.ZERO;
    }

    private void validateQuantitiesAgainstStock(String username, List<CartItemRequest> requests) {
        UserCart userCart = getUserCart(username);

        for (CartItemRequest request : requests) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            CartItemResponse existingItem = userCart.items.get(request.getProductId());
            int totalQuantity = request.getQuantity() + (existingItem != null ? existingItem.getQuantity() : 0);

            if (product.getQuantityInStock() < totalQuantity) {
                throw new BusinessException(String.format(
                        "Not enough stock available for product '%s'. Only %d items available.",
                        product.getName(), product.getQuantityInStock()
                ));
            }
        }
    }

    private BigDecimal getMaxDiscountPercentageForProduct(Product product) {
        List<Discount> discounts = discountRepository.findActiveDiscountsForProduct(product);
        return discounts.stream()
                .map(Discount::getPercentage)
                .max(Comparator.naturalOrder())
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    private void updateExistingCartItem(CartItemResponse item, int quantityToAdd, BigDecimal maxDiscountPercentage) {
        item.setQuantity(item.getQuantity() + quantityToAdd);
        item.setDiscountPercentage(maxDiscountPercentage);
        item.setTotalPrice(calculateItemTotalPrice(
                item.getUnitPrice(),
                item.getQuantity(),
                maxDiscountPercentage
        ));
    }

    private void addNewCartItem(UserCart userCart, Product product, int quantity,
                                BigDecimal maxDiscountPercentage, BigDecimal unitPrice) {
        CartItemResponse newItem = CartItemResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .discountPercentage(maxDiscountPercentage)
                .totalPrice(calculateItemTotalPrice(unitPrice, quantity, maxDiscountPercentage))
                .build();
        userCart.items.put(product.getId(), newItem);
    }

    private BigDecimal calculateItemTotalPrice(BigDecimal unitPrice, int quantity, BigDecimal discountPercentage) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100))));
    }

    private BigDecimal calculatePreTaxSubtotal(List<CartItemResponse> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateProductDiscounts(List<CartItemResponse> items) {
        return items.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .multiply(item.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCartDiscount(BigDecimal amountAfterProductDiscounts, BigDecimal cartDiscountPercentage) {
        return amountAfterProductDiscounts
                .multiply(cartDiscountPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTax(BigDecimal taxableAmount) {
        return taxableAmount.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateDiscount(Discount discount) {
        if (!discount.isActive() ||
                (discount.getValidFrom() != null && discount.getValidFrom().isAfter(LocalDateTime.now())) ||
                (discount.getValidTo() != null && discount.getValidTo().isBefore(LocalDateTime.now()))) {
            throw new BusinessException("Discount is not valid");
        }

        if (discount.getType() != Discount.DiscountType.CART) {
            throw new BusinessException("This is a product discount, not a cart discount");
        }
    }
}