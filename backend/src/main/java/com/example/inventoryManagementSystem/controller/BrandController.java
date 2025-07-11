package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.BrandRequest;
import com.example.inventoryManagementSystem.dto.response.BrandResponse;
import com.example.inventoryManagementSystem.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;


    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.createBrand(request));
    }


    @GetMapping
    public ResponseEntity<Page<BrandResponse>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(brandService.getAllBrands(page, size));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<BrandResponse>> searchBrands(
            @RequestParam String query) {
        return ResponseEntity.ok(brandService.searchBrands(query));
    }
}