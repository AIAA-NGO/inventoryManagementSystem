package com.example.inventoryManagementSystem.service.impl;


import com.example.inventoryManagementSystem.dto.TaxRateDTO;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.SystemSetting;
import com.example.inventoryManagementSystem.model.TaxRate;
import com.example.inventoryManagementSystem.repository.SystemSettingRepository;
import com.example.inventoryManagementSystem.repository.TaxRateRepository;
import com.example.inventoryManagementSystem.service.SystemSettingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {
    private final SystemSettingRepository systemSettingRepository;
    private final TaxRateRepository taxRateRepository;
    private final ModelMapper modelMapper;

    @Override
    public Map<String, String> getAllSettings() {
        return systemSettingRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        SystemSetting::getSettingKey,
                        SystemSetting::getSettingValue
                ));
    }

    @Override
    @Transactional
    public void updateSettings(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            SystemSetting setting = systemSettingRepository.findById(key)
                    .orElse(new SystemSetting());
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            systemSettingRepository.save(setting);
        });
    }

    @Override
    public List<TaxRateDTO> getAllTaxRates() {
        return taxRateRepository.findAll().stream()
                .map(taxRate -> modelMapper.map(taxRate, TaxRateDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaxRateDTO createTaxRate(TaxRateDTO taxRateDTO) {
        TaxRate taxRate = modelMapper.map(taxRateDTO, TaxRate.class);
        TaxRate saved = taxRateRepository.save(taxRate);
        return modelMapper.map(saved, TaxRateDTO.class);
    }

    @Override
    @Transactional
    public TaxRateDTO updateTaxRate(Long id, TaxRateDTO taxRateDTO) {
        TaxRate taxRate = taxRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax rate not found"));

        modelMapper.map(taxRateDTO, taxRate);
        TaxRate updated = taxRateRepository.save(taxRate);
        return modelMapper.map(updated, TaxRateDTO.class);
    }

    @Override
    @Transactional
    public void deleteTaxRate(Long id) {
        taxRateRepository.deleteById(id);
    }
}