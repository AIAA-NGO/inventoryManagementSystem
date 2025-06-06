package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.SupplierRequest;
import com.example.inventoryManagementSystem.dto.response.SupplierResponse;
import com.example.inventoryManagementSystem.model.Category;
import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.model.Purchase;
import com.example.inventoryManagementSystem.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.createSupplier(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<Product>> getProductsBySupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getProductsBySupplier(id));
    }

    @GetMapping("/{id}/purchases")
    public ResponseEntity<List<Purchase>> getPurchasesBySupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getPurchasesBySupplier(id));
    }

    @GetMapping("/{id}/categories")
    public ResponseEntity<List<Category>> getCategoriesBySupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getCategoriesBySupplier(id));
    }

    @PostMapping("/{id}/categories")
    public ResponseEntity<Void> addCategoryToSupplier(
            @PathVariable Long id,
            @RequestParam Long categoryId) {
        supplierService.addCategoryToSupplier(id, categoryId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/categories/{categoryId}")
    public ResponseEntity<Void> removeCategoryFromSupplier(
            @PathVariable Long id,
            @PathVariable Long categoryId) {
        supplierService.removeCategoryFromSupplier(id, categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>> searchSuppliers(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(supplierService.searchSuppliers(companyName, contactPerson, category));
    }
}