package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.TaxRateDTO;

import java.util.List;
import java.util.Map;

public interface SystemSettingService {
    Map<String, String> getAllSettings();
    void updateSettings(Map<String, String> settings);
    List<TaxRateDTO> getAllTaxRates();
    TaxRateDTO createTaxRate(TaxRateDTO taxRateDTO);
    TaxRateDTO updateTaxRate(Long id, TaxRateDTO taxRateDTO);
    void deleteTaxRate(Long id);
}
