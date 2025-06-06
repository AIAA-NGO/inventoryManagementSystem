package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.model.Discount;
import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;

    public boolean isDiscountValid(Discount discount) {
        LocalDateTime now = LocalDateTime.now();
        return discount.isActive() &&
                (discount.getValidFrom() == null || !now.isBefore(discount.getValidFrom())) &&
                (discount.getValidTo() == null || !now.isAfter(discount.getValidTo()));
    }

    public List<Discount> findValidDiscountsForProduct(Product product) {
        return discountRepository.findByApplicableProductsContaining(product)
                .stream()
                .filter(this::isDiscountValid)
                .collect(Collectors.toList());
    }
}
