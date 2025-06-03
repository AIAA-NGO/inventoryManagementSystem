package com.example.inventoryManagementSystem.service;

import com.example.inventoryManagementSystem.dto.request.UnitRequest;
import com.example.inventoryManagementSystem.dto.response.UnitResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public interface UnitService {
    UnitResponse createUnit(UnitRequest request);
    Page<UnitResponse> getAllUnits(int page, int size);
    UnitResponse getUnitById(Long id);
    UnitResponse updateUnit(Long id, UnitRequest request);
    void deleteUnit(Long id);
    List<UnitResponse> searchUnits(String query);
}