package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.DiscountRequest;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.Discount;
import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.repository.DiscountRepository;
import com.example.inventoryManagementSystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {
    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<Discount> createDiscount(@RequestBody DiscountRequest request) {
        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setPercentage(request.getPercentage());
        discount.setValidFrom(request.getValidFrom());
        discount.setValidTo(request.getValidTo());
        discount.setDescription(request.getDescription());

        Set<Product> products = request.getProductIds().stream()
                .map(id -> productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)))
                .collect(Collectors.toSet());

        discount.setApplicableProducts(products);
        return ResponseEntity.ok(discountRepository.save(discount));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Discount>> getActiveDiscounts() {
        return ResponseEntity.ok(discountRepository.findActiveDiscounts());
    }
}