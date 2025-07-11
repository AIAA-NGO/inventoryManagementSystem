package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.UnitRequest;
import com.example.inventoryManagementSystem.dto.response.UnitResponse;
import com.example.inventoryManagementSystem.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;


    @PostMapping
    public ResponseEntity<UnitResponse> createUnit(
            @Valid @RequestBody UnitRequest request) {
        return ResponseEntity.ok(unitService.createUnit(request));
    }


    @GetMapping
    public ResponseEntity<Page<UnitResponse>> getAllUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(unitService.getAllUnits(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitResponse> getUnitById(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitResponse> updateUnit(
            @PathVariable Long id,
            @Valid @RequestBody UnitRequest request) {
        return ResponseEntity.ok(unitService.updateUnit(id, request));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UnitResponse>> searchUnits(
            @RequestParam String query) {
        return ResponseEntity.ok(unitService.searchUnits(query));
    }
}