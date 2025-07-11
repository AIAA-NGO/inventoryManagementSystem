package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.PurchaseRequest;
import com.example.inventoryManagementSystem.dto.response.PurchaseResponse;
import com.example.inventoryManagementSystem.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @GetMapping
    public ResponseEntity<List<PurchaseResponse>> getAllPurchases() {
        return ResponseEntity.ok(purchaseService.getAllPurchases());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PurchaseResponse>> getPendingPurchases() {
        return ResponseEntity.ok(purchaseService.getPendingPurchases());
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(purchaseService.createPurchase(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponse> receivePurchase(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.receivePurchase(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PurchaseResponse> cancelPurchase(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.cancelPurchase(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseResponse> updatePurchase(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(purchaseService.updatePurchase(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }
}