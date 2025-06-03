package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.TaxRateDTO;
import com.example.inventoryManagementSystem.service.SystemSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SystemSettingController {
    private final SystemSettingService systemSettingService;

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }

    @PutMapping
    public ResponseEntity<Void> updateSettings(@RequestBody Map<String, String> settings) {
        systemSettingService.updateSettings(settings);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tax-rates")
    public ResponseEntity<List<TaxRateDTO>> getAllTaxRates() {
        return ResponseEntity.ok(systemSettingService.getAllTaxRates());
    }

    @PostMapping("/tax-rates")
    public ResponseEntity<TaxRateDTO> createTaxRate(@Valid @RequestBody TaxRateDTO taxRateDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(systemSettingService.createTaxRate(taxRateDTO));
    }

    @PutMapping("/tax-rates/{id}")
    public ResponseEntity<TaxRateDTO> updateTaxRate(
            @PathVariable Long id,
            @Valid @RequestBody TaxRateDTO taxRateDTO) {
        return ResponseEntity.ok(systemSettingService.updateTaxRate(id, taxRateDTO));
    }

    @DeleteMapping("/tax-rates/{id}")
    public ResponseEntity<Void> deleteTaxRate(@PathVariable Long id) {
        systemSettingService.deleteTaxRate(id);
        return ResponseEntity.noContent().build();
    }
}
